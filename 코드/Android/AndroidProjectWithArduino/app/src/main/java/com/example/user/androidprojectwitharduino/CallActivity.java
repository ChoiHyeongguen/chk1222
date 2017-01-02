package com.example.user.androidprojectwitharduino;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class CallActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
    }

    public void calling_Emergency(View view) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        try {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_NO_USER_ACTION);
            switch (view.getId()) {
                case R.id.btn_119 : intent.setData(Uri.parse("tel:119".trim())); break;
                case R.id.btn_112 : intent.setData(Uri.parse("tel:112".trim())); break;
            }
            startActivity(intent);
        } catch (Exception e){
            Toast.makeText(CallActivity.this, "ERR : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    public void calling(View view){
        Intent intent= new Intent(Intent.ACTION_CALL);
        try {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
            intent.setData(Uri.parse("tel:0226104743".trim()));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(CallActivity.this, "ERR : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void ConnectionInternet(View view) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://m.119.go.kr/".trim())));
        } catch (Exception e) {
            Toast.makeText(CallActivity.this, "ERR : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
