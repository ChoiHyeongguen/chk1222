package com.example.user.androidprojectwitharduino;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class BulletinBoardContentActivity extends AppCompatActivity {
    private String ipAddress;
    private Button backBtn;
    private TextView mTitle, mDate, mContent;
    private theAsyncThread backgroundThread;
    final Handler handler = new Handler()

    {

        public void handleMessage(Message msg)
        {
            Bundle b=msg.getData();
            mContent.setText(b.getString("content"));

        }

    };
    /*
        이전에 게시판 리스트 뷰 액티비티에서 리스트 항목 클릭시
        게시판의 제목과 날짜를 인텐트에 넣어서 이 액티비티를 실행합니다.
        그리고 게시판의 내용조회를 위해 게시판의 번호도 같이 받아옵니다.
        여기서의 역활은 서버로 부터 게시판의 내용만 조회해서 가져오면 됩니다.
        보여주는 것과 상세 버튼 누르는 것의 역활을 나눠서 실행하려고 게시판은 2개의 액티비티를 사용합니다.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bulletinboard_content);
        ipAddress = getString(R.string.ip_address);
        backBtn = (Button) findViewById(R.id.back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mTitle = (TextView) findViewById(R.id.title);
        mDate = (TextView) findViewById(R.id.date);
        mContent = (TextView) findViewById(R.id.content);
        /*
            이전 액티비티로부터 받아오는 값을 넣어줍니다.
            그리고 콘텐트를 받아오기 위해 AsyncTask 쓰레드를 실행 시킵니다.
            AsyncTask에 추가인자로 번호를 String화 한것을 넘겨줍니다.(데이터 조회를 위해 사용)
         */
        Intent intent =getIntent();
        int no =intent.getIntExtra("no",0);
        String state=intent.getStringExtra("icon");
        if(state.equals("importance"))

        Log.d("b", no+"번게시판");
        mTitle.setText(intent.getStringExtra("title"));
        mDate.setText(intent.getStringExtra("date"));
        theAsyncThread backgroundThread = new theAsyncThread();
        backgroundThread.execute("content", no+"");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try
        {
            if (backgroundThread.getStatus() == AsyncTask.Status.RUNNING)
            {
                backgroundThread.cancel(true);
            }
            else
            {
            }
        }
        catch (Exception e)
        {
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
            if(args[0].equals("content")) {
                try {
                    URL url = null;
                    HttpURLConnection conn = null;
                    url  = new URL("http://"+ipAddress+":8080/ArduinoServerProject/AndroidContent.jsp");
                    String param = URLEncoder.encode("no", "utf-8") + "=" + URLEncoder.encode(args[1], "utf-8");
                    InputStream is = null;
                    OutputStream os = null;

                    ByteArrayOutputStream baos = null;
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Cache-Control", "no-cache");
                    conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                    /*
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json");*/
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    Log.d("coded", "jsp");
                    os = conn.getOutputStream();
                    os.write(param.getBytes());
                    os.flush();
                    if(conn.getResponseMessage().equals("OK")){

                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Log.d("coded", "들어옴");
                        BufferedReader bufreader = new BufferedReader(
                                new InputStreamReader(conn.getInputStream(), "UTF-8"));
                        String line = null;
                        String page = "";

                        // 버퍼의 웹문서 소스를 줄 단위로 읽어(line), page에 저장함
                        while ((line = bufreader.readLine()) != null) {
                            page += line;
                        }
                        // 읽어들인 내용을 json 객체에 담아 그 중 dataSend로 정의 된 내용을
                        // 불어온다. 그럼 json 중 원하는 내용을 하나의 json 배열에 담게 된다.
                        JSONObject json = new JSONObject(page);
                        JSONArray jArr = json.getJSONArray("dataSend");

                        Message msg = handler.obtainMessage();
                        Bundle b = new Bundle();
                        for (int i = 0; i < jArr.length(); i++) {
                            json = jArr.getJSONObject(i);
                            String s = json.getString("content");
                            Log.d("tag", s);
                            b.putString("content", s);
                            msg.setData(b);
                        }
                        handler.sendMessage(msg);
                        bufreader.close();
                        conn.disconnect();
                    }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            return "OK";
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
}
