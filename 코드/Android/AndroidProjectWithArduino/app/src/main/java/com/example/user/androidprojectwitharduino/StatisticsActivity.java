package com.example.user.androidprojectwitharduino;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Date;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
/*
    전체적인 통계를 보여주는 액티비티 입니다.
    파이차트로 사고가 일어난 것을 월별로 한눈에 보여주고
    각 요소를 클릭하면 그 월에 해당하는 사고건수를 자세하게 보여주는 액티비티를 실행해줍니다.
 */
public class StatisticsActivity extends AppCompatActivity {
    private String ipAddress;
    private PieChart mChart;
    private PieData data=null;
    private float[] yData;      // 데이터를 저장하는 배열
    private String[] xData;     // 데이터의 이름을 저장하는 배열
    private theAsyncThread setDataThread=null;
    /*
        여기서 핸들러를 사용하는 이유는
        파이차트에 데이터를 넣고 그것을 그리게 하는 코드가 UI작업이라서
        핸들러의 내장변수인 obj도 같이 사용해서 데이터를 받아와
        데이터를 넣고 차트를 그립니다.
     */
    final Handler handler = new Handler()
    {
        public void handleMessage(final Message msg)
        {
            /*
                여기서 msg의 번들 데이터는
                상태 정보를 받아오는데 사용합니다.
                그리고 obj에는 각각에 다르게 사용되는 데이터를 담는 역할을 합니다.
                setListener의 상태면 파이차트의 상세부분을 클릭할 때
                나타나야하는 이벤트를 지정해줍니다.
                drawChart의 상태면 차트에 데이터를 넣고 그리게 합니다.
             */
            Bundle b=msg.getData();
            String state = b.getString("state");
            if(state==null)
                return;
            if(state.equals("setListener")) {
                final HashMap<String, ArrayList<Date>> saveData =  (HashMap<String, ArrayList<Date>>)msg.obj;
                mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {     // 차트의 각요소가 클릭 되면
                    @Override
                    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                        // display msg when value selected
                        if (e == null)
                            return;
                        ArrayList<Date> list = (ArrayList<Date>)saveData.get(e.getXIndex()+"");     // 가져온 데이터들을 (사건발생 날짜 데이터)
                        Intent intent = new Intent(getApplicationContext(), DetailStatisticsActivity.class);    // 상세 차트를 보여주는 액티비티로 넘겨서
                        intent.putExtra("data",list);
                        startActivity(intent);  // 실행시킵니다.
                        /*
                        (HashMap<String, ArrayList<Date>>)

                                Toast.makeText(StatisticsActivity.this,
                                xData[e.getXIndex()] + " = " + e.getVal() + "%", Toast.LENGTH_SHORT).show();*/
                    }

                    @Override
                    public void onNothingSelected() {
                    }
                });
            }
            else if(state.equals("drawChart")) {
                mChart.setData((PieData) msg.obj);      // 데이터 셋팅
                mChart.highlightValues(null);   // 값을 보여주는 셋팅값을 없앱니다.
                mChart.setUsePercentValues(false);      // %가 아닌 건수로 표시하게 설정
                mChart.invalidate();        // 차트를 다시 그립니다.
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        ipAddress = getString(R.string.ip_address);
        mChart = (PieChart) findViewById(R.id.chart);
        // add pie chart to main layout

        // configure pie chart
        mChart.setUsePercentValues(true);
        // enable hole and configure
        mChart.setDrawHoleEnabled(true);
        mChart.setHoleRadius(7);
        mChart.setTransparentCircleRadius(10);

        // enable rotation of the chart by touch
        mChart.setRotationAngle(0);
        mChart.setRotationEnabled(true);

        // add data
        //addData();
        setDataThread = new theAsyncThread();
        setDataThread.execute("init");
        // customize legends
        Legend l = mChart.getLegend();
        l.setTextColor(Color.WHITE);
        l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
        l.setXEntrySpace(7);
        l.setYEntrySpace(5);
    }

    private void addData() {
        ArrayList<Entry> yVals1 = new ArrayList<Entry>();

        for (int i = 0; i < yData.length; i++)
            yVals1.add(new Entry(yData[i], i));

        ArrayList<String> xVals = new ArrayList<String>();

        for (int i = 0; i < xData.length; i++)
            xVals.add(xData[i]);

        // create pie data set
        PieDataSet dataSet = new PieDataSet(yVals1, "Market Share");
        dataSet.setSliceSpace(3);
        dataSet.setSelectionShift(5);

        // add many colors
        ArrayList<Integer> colors = new ArrayList<Integer>();

        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());
        dataSet.setColors(colors);

        // instantiate pie data object now
        PieData data = new PieData(xVals, dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.GRAY);

        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);

        // update pie chart
        mChart.invalidate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (setDataThread.getStatus() == AsyncTask.Status.RUNNING) {
                setDataThread.cancel(true);
            } else {
            }

        } catch (Exception e) {
        }
    }
    public class theAsyncThread extends AsyncTask<String, String, String> {

        //--- Thread를 시작하기 전에 호출되는 함수
        protected void onPreExecute() {
            super.onPreExecute();

        }
        //--- Thread의 주요 작업을 처리 하는 함수

        //--- Thread를 실행하기 위해 excute(~)에서 전달한 값을 인자로 받습니다.

        protected String doInBackground(String... args) {
            String responseState="";
            try {
                URL url = null;
                HttpURLConnection conn = null;
                String urlStr = "";
                String param = null;
                param="";
                urlStr = "http://"+ipAddress+":8080/ArduinoServerProject/requestStatisticsData";
                url = new URL(urlStr);
                Log.d("test", urlStr);
                OutputStream os = null;
                InputStream is = null;
                ByteArrayOutputStream baos = null;
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Cache-Control", "no-cache");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                Log.d("coded", conn.getResponseMessage());
                if (args[0].equals("init")) {
                    String response = "";

                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Log.d("coded", "들어옴");
                        is = conn.getInputStream();
                        ObjectInputStream ois = new ObjectInputStream(is);
                        HashMap<String, ArrayList<Date>> dataMap = (HashMap<String, ArrayList<Date>>) ois.readObject();
                        HashMap<String, ArrayList<Date>> saveDataMap = new HashMap<String, ArrayList<Date>>();
                        Calendar cal = Calendar.getInstance();
                        Log.d("coded", "다 읽었음"+dataMap.size() +cal.get(Calendar.MONTH)+1);
                        ArrayList<Date> list=null;
                        ArrayList<Entry> yVals = new ArrayList<Entry>();
                        ArrayList<String> xVals = new ArrayList<String>();
                        int i=0;
                        for(int k=dataMap.size(); k>0;  ){
                            list=dataMap.get((cal.get(Calendar.MONTH)+1-i)+"month");

                            if(list==null) {
                                Log.d("test",cal.get(Calendar.MONTH)+1-i+"월");
                                i++;
                                continue;
                            }
                            yVals.add(new Entry(list.size(), dataMap.size()-k));
                            Log.d("dd", list.size()+"");
                            xVals.add((cal.get(Calendar.MONTH)+1-i)+"월");
                            saveDataMap.put((dataMap.size()-k)+"", list);
                            Log.d("test"," dd"+i);
                            for(int j=0; j<list.size(); j++){
                                Date data= list.get(j);
                            }
                            i++;
                            k--;
                        }
                        // progressDialog.dismiss();
                        is.close();
                        Message msg = handler.obtainMessage();
                        Bundle b = new Bundle();
                        b.putString("state", "setListener");
                        msg.setData(b);
                        msg.obj = (HashMap<String, ArrayList<Date>>)  saveDataMap;
                        handler.sendMessage(msg);
                        // create pie data set
                        PieDataSet dataSet = new PieDataSet(yVals, "");
                        dataSet.setSliceSpace(3);
                        dataSet.setSelectionShift(5);

                        // add many colors
                        ArrayList<Integer> colors = new ArrayList<Integer>();

                        for (int c : ColorTemplate.VORDIPLOM_COLORS)
                            colors.add(c);

                        for (int c : ColorTemplate.JOYFUL_COLORS)
                            colors.add(c);

                        for (int c : ColorTemplate.COLORFUL_COLORS)
                            colors.add(c);

                        for (int c : ColorTemplate.LIBERTY_COLORS)
                            colors.add(c);

                        for (int c : ColorTemplate.PASTEL_COLORS)
                            colors.add(c);

                        colors.add(ColorTemplate.getHoloBlue());
                        dataSet.setColors(colors);

                        // instantiate pie data object now
                        data = new PieData(xVals, dataSet);
                        data.setValueFormatter(new MyValueFormatter());
                        data.setValueTextSize(11f);
                        data.setValueTextColor(Color.GRAY);
                        msg = handler.obtainMessage();
                        b = new Bundle();
                        b.putString("state", "drawChart");
                        msg.setData(b);
                        msg.obj = (PieData) data;
                        handler.sendMessage(msg);
                    }
                    conn.disconnect();
                    return "READ_OK";
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return "";
        }
        //--- doInBackground(~)에서 호출되어 주로 UI 관련 작업을 하는 함수


        protected void onProgressUpdate(String... progress) {
        }

        //--- Thread를 처리한 후에 호출되는 함수

        //--- doInBackground(~)의 리턴값을 인자로 받습니다.

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
        //--- AsyncTask.cancel(true) 호출시 실행되어 thread를 취소 합니다.

        protected void onCancelled() {
            super.onCancelled();
        }
    }
    class MyValueFormatter implements ValueFormatter {

        private DecimalFormat mFormat;

        public MyValueFormatter() {
            mFormat = new DecimalFormat("###,###,###,##건"); // use one decimal if needed
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            // write your logic here
            return mFormat.format(value) + ""; // e.g. append a dollar-sign
        }

    }
}