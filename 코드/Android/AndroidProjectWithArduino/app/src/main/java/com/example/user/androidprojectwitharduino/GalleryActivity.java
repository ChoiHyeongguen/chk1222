package com.example.user.androidprojectwitharduino;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class GalleryActivity extends AppCompatActivity {
//    public int inSampleSize = 1;

    private static String basePath;

    private ImageView resultView;
    private Gallery customGallery;
    private CustomGalleryAdapter customGalAdapter;

    private String[] imgs;
    private String[] temp;
    private String tmpPath;

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        tmpPath = Environment.getExternalStorageDirectory() + File.separator + "SmartSafetyZone" + File.separator;
        File tmp = new File(tmpPath);
        temp = tmp.list();

        textView = (TextView)findViewById(R.id.textView);
        textView.setTextColor(Color.WHITE);

        if (temp.length <= 0) {
            Toast.makeText(GalleryActivity.this, "현재 열 수 있는 이미지 파일이 없습니다.", Toast.LENGTH_SHORT).show();
        } else {
            Spinner spinner = (Spinner)findViewById(R.id.spinner);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, temp);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String month = temp[position];
                    String path = tmpPath + month;

                    makeGallery(path);
                }
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            spinner.setAdapter(adapter);

            String month = temp[0];
            String path = tmpPath + month;

            makeGallery(path);
        }
    }

    private void makeGallery(String path) {
        // App.을 실행하자 마자 지정한 경로의 생성 및 접근에 용이하도록 아래와 같이 생성
        File mediaStorageDir = new File(path);
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MAIN", "failed to create directory");
            }
        }

        basePath = mediaStorageDir.getPath() + File.separator;
        resultView = (ImageView) findViewById(R.id.resultview);

        File file = new File(basePath);
        imgs = file.list();

        if (imgs.length > 0) {
            Bitmap bm = BitmapFactory.decodeFile(basePath + File.separator + imgs[0]);
            Bitmap bm2 = ThumbnailUtils.extractThumbnail(bm, bm.getWidth(), bm.getHeight());
            bm2 = Bitmap.createScaledBitmap(bm2, getLcdSIzeWidth(), getLcdSIzeWidth(), true);
            resultView.setImageBitmap(bm2);
            textView.setText(imgs[0]);
        }

        customGallery = (Gallery) findViewById(R.id.customgallery);                     // activity_main.xml에서 선언한 Gallery를 연결
        customGalAdapter = new CustomGalleryAdapter(getApplicationContext(), basePath); // 위 Gallery에 대한 Adapter를 선언
        customGallery.setAdapter(customGalAdapter);                                     // Gallery에 위 Adapter를 연결
        // Gallery의 Item을 Click할 경우 ImageView에 보여주도록 함
        customGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    Bitmap bm = BitmapFactory.decodeFile(basePath + File.separator + imgs[position]);
                    Bitmap bm2 = ThumbnailUtils.extractThumbnail(bm, bm.getWidth(), bm.getHeight());
                    bm2 = Bitmap.createScaledBitmap(bm2, getLcdSIzeWidth(), getLcdSIzeWidth(), true);
                    resultView.setImageBitmap(bm2);
                    textView.setText(imgs[position]);
                } catch (Exception e ) {
                    Toast.makeText(GalleryActivity.this, "현재 이미지 파일에 문제가 있습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public int getLcdSIzeWidth() { // 스마트폰 가로 크기
        return ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
    }
}