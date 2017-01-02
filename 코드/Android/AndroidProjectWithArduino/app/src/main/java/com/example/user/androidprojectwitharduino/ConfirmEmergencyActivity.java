package com.example.user.androidprojectwitharduino;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class ConfirmEmergencyActivity extends AppCompatActivity {
    private Button confirmBtn, cancelBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_emergency);
        confirmBtn = (Button) findViewById(R.id.confirm_btn);
        cancelBtn = (Button) findViewById(R.id.cancel_btn);
        Intent intent = getIntent();
        String fileDes = intent.getStringExtra("file_des");
        File imgFile = new File (fileDes);
        if(imgFile.exists()){
            Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            ImageView imageView = (ImageView) findViewById(R.id.EmergencyImageView);
            imageView.setImageBitmap(bitmap);
        }
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(Intent.ACTION_CALL);
                Context hostActivity = ConfirmEmergencyActivity.this;
                SharedPreferences sf = PreferenceManager.getDefaultSharedPreferences(hostActivity);
                SharedPreferences.Editor editor = sf.edit();
                String phone = sf.getString("phone_number", "");
                if(phone.equals("")) {
                    intent.setData(Uri.parse("tel:"+ getString(R.string.one_call_number)));
                    return;
                }

                intent.setData(Uri.parse("tel:"+ phone));
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
