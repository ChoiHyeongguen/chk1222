package com.example.user.androidprojectwitharduino;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MadeByActivity extends AppCompatActivity {
    TextView tx;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_made_by);
        tx = (TextView) findViewById(R.id.textViewName1);
        tx.setText("\n\n성공회대 글로컬 IT 학과\n\n"+
                "200934013 서동형\n"+"201134031 최형근\n\n" + "SmartSafetyZone\n 트위터 이메일 \n"
        +"safty_zone_skhu@naver.com\n");
    }
}
