package com.example.user.androidprojectwitharduino;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashSet;

public class DetailStatisticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LineChart mChart;
        TextView tx;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_statistics);
        ArrayList<Date> list  = (ArrayList<Date>) getIntent().getSerializableExtra("data");
        mChart = (LineChart) findViewById(R.id.chart);
        tx=(TextView)findViewById(R.id.textView);
        tx.setText("이번 달의 사고 건 수는 총 " +list.size()+ "건 입니다.");
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<String>();
        String date ="";
        int overlabCnt=0;
        int j=0;

        for(int i=0; i<list.size(); i++) {
            Log.d("if", list.get(i)+"");
            if(i==0){
                date = list.get(i)+"";
                labels.add(date);
                overlabCnt++;
                if(list.size()==1)
                    entries.add(new Entry(1, 0));
            }else {
                if (!date.equals(list.get(i) + "")) {
                    date = list.get(i) + "";
                    labels.add(date);
                    Log.d("if", overlabCnt+"");
                    entries.add(new Entry(overlabCnt, j));
                    overlabCnt = 1;
                    j++;
                } else {
                    overlabCnt++;
                }
            }
        }/*
        date = list.get(list.size()-1) + "";
        labels.add(date);*/
        Log.d("if", overlabCnt+"");
        entries.add(new Entry(overlabCnt, j+1));
        /*
            중복 제거한 걸  따로 만들어서 데이터를 연결
         */

        LineDataSet dataset = new LineDataSet(entries,"events");
        dataset.setAxisDependency(YAxis.AxisDependency.LEFT);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataset);
       // dataset.setDrawFilled(true);
        Log.d("cnt", labels.size()+"");
        LineData data = new LineData(labels, dataset);

        YAxis y = mChart.getAxisLeft();
        y.setTextColor(Color.WHITE);

        XAxis x = mChart.getXAxis();
        x.setTextColor(Color.WHITE);

        Legend legend = mChart.getLegend();
        legend.setTextColor(Color.WHITE);

        mChart.setData(data);
        mChart.invalidate();
    }


}
