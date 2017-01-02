package com.example.user.androidprojectwitharduino;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
/*
    메인화면 액티비티
    초기화작업 및 서비스 시작을 해주는 최초 액티비티
    관리자인증을 받으면 AsyncTask 쓰레드가 서버에서 사진들을 다운로드 받아 데이터를
    구축해서 초기화를 진행하고 각 기능들을 이 화면의 버튼으로 묶어주는 역할을 한다.
 */
public class MainActivity extends AppCompatActivity {
    private String ipAddress;
    private String readMsg ="안전 사고에 대한 사진들 및 정보를 가져오기 위해서는 쓰기 권한이 필요합니다.";
    private String callMsg ="안전 사고에 발생 시 긴급 전화를 바로 연결하기 위해서는 전화 권한이 필요합니다.";
    private final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 2;
    private Button announceBtn, statisticsBtn, callBtn, galleryBtn, settingBtn;       // 각각의 기능버튼
    private Intent serviceIntent;
    private String sfVersion = "ver";
    private View dlgView;         // 초기화시 사용할 다이얼로그 (커스텀 다이얼로그 만드는데 사용)
    private theAsyncThread backgroundInThread;      // 초기화 작업을 위한 쓰레드
    private ProgressDialog progressDialog;     // 프로그레스 다이얼로그 변수 선언
    private String save_folder = "/SmartSafetyZone";   // 저장폴더 이름
    private String save_path="";    // 최종 저장 경로를 저장하는 문자열 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ipAddress = getString(R.string.ip_address);
        String ext = Environment.getExternalStorageState();     // 외부 SD카드가 있는지 상태정보를 읽고
        if (ext.equals(Environment.MEDIA_MOUNTED)) {     // SD카드가 장착되있다면 이것을 이용해 최종저장경로를 만듭니다.
            save_path = Environment.getExternalStorageDirectory().getAbsolutePath() + save_folder;
        }
        File dir = new File(save_path);
        if (!dir.exists()) {     // 최종 경로가 없다면
            dir.mkdir();    // 최종경로 생성
        }
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Toast.makeText(MainActivity.this, "권한 허가", Toast.LENGTH_SHORT).show();
                initProgram();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(MainActivity.this, "권한 거부\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
                finish();
            }
        };
        new TedPermission(this)
                .setPermissionListener(permissionlistener)
                .setRationaleMessage("서버로 부터 안전 사고 관련 데이터를 다운받기 위해서는 저장공간에 대한 권한이 있어야 합니다. "
                +"또한 사고 발생시 긴급전화 사용을 위해 전화 권한도 허용해야합니다.")
                .setRationaleConfirmText("허락")
                .setDeniedCloseButtonText("거절")
                .setDeniedMessage("거부하시면 어플리케이션을 정상사용이 불가능합니다.\n하지만 [설정] > [권한] 에서 권한을 허용할 수 있어요.")
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CALL_PHONE )
                .check();

        serviceIntent = new Intent(MainActivity.this, MainService.class);
        startService(serviceIntent);    // 서비스 시작
        setContentView(R.layout.activity_main);
        /*
            각 버튼들 초기화 및 버튼 클릭 시 액티비티 실행되게
            연결하는  작업들입니다.
         */
        announceBtn = (Button) findViewById(R.id.announce_btn);
        galleryBtn = (Button) findViewById(R.id.gallery_btn);
        statisticsBtn = (Button) findViewById(R.id.statistics_btn);
        callBtn = (Button) findViewById(R.id.call_btn);
        settingBtn = (Button) findViewById(R.id.setting_btn);
        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
                startActivity(intent);
            }
        });
        statisticsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), StatisticsActivity.class);
                startActivity(intent);
            }
        });
        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CallActivity.class);
                startActivity(intent);
            }
        });
        announceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), BoardActivity.class);
                startActivity(intent);
            }
        });

        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GalleryActivity.class);
                startActivity(intent);
            }
        });
    }
    @Override
    protected void onDestroy() {        // 액티비티 종료시
        super.onDestroy();
        if(progressDialog!=null) {
            Log.d("coded", "프로그레스바 종료되었음");
            progressDialog.dismiss();
        }
        try {
            /*
                실행되고 있는 쓰레드가 있다면 종료를 요청합니다.
             */
            if (backgroundInThread.getStatus() == AsyncTask.Status.RUNNING) {
                backgroundInThread.cancel(true);
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
            String ext = Environment.getExternalStorageState();     // 외부 SD카드가 있는지 상태정보를 읽고
            if (ext.equals(Environment.MEDIA_MOUNTED)) {     // SD카드가 장착되있다면 이것을 이용해 최종저장경로를 만듭니다.
                save_path = Environment.getExternalStorageDirectory().getAbsolutePath() + save_folder;
            }
            File dir = new File(save_path);
            if (!dir.exists()) {     // 최종 경로가 없다면
                dir.mkdir();    // 최종경로 생성
            }
            try {
                /*
                    웹서버의 연결요청을 위한 초기화 작업
                    주소, 타입, 타임아웃 시간, 방식 등을 지정한다.
                 */
                URL url =new URL("http://"+ipAddress+":8080/ArduinoServerProject/download");
                HttpURLConnection conn = null;
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Cache-Control", "no-cache");
                conn.setRequestProperty("Content-type", "application/octet-stream");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                Log.d("coded", conn.getResponseMessage());

                if (args[0].equals("init")) {       // 초기화 작업이라면
                    int responseCode = conn.getResponseCode();      // 웹서버의 응답을 체크하여
                    if (responseCode == HttpURLConnection.HTTP_OK) {    // 송수신이 잘되면 - 데이터를 받은 것입니다.
                        Log.d("coded", "들어옴");
                        InputStream is = conn.getInputStream();
                        /*
                            Object스트림 함수를 통해 데이터를 받습니다.
                            HashMap 타입으로 받고
                            dataMap은 사진들의 데이터를 저장해놓은 것이고
                            fileNameDataMap은 서버의 사진이름을 받기위해 사용합니다.
                            이 두가지를 1:1 매칭하여 웹서버의 파일이름 사용 하여 다시 만들어 저장합니다.
                            (웹서버의 파일이름에는 많은 정보들이 포함되서 꼭 이름도 가져와야 합니다.)
                         */
                        ObjectInputStream ois = new ObjectInputStream(is);
                        HashMap<String, ArrayList<String>> fileNameDataMap = (HashMap<String, ArrayList<String>>) ois.readObject();
                        HashMap<String, ArrayList<byte[]>> dataMap = (HashMap<String, ArrayList<byte[]>>) ois.readObject();
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        Calendar cal = Calendar.getInstance();
                        Log.d("coded", "다 읽었음"+dataMap.size() +cal.get(Calendar.MONTH)+1);
                        /*
                            각각의 데이터를 받기위한
                            ArrayList 변수들 초기화
                         */
                        ArrayList<byte[]> list=null;
                        ArrayList<String> fileNameList = null;
                        int i=0;        // 각 월별로 검색하기 위한 변수
                        /*
                            k는 전체 개수로서 돌아가는 변수이고
                            i는 그중에 건너뛰는 부분을 계산하기위해 사용하는 변수입니다.
                            예를 들면 서버에 7월 9월 만 존재하는 경우 k는 2가 됩니다.
                            근데 이대로 돌려버리면 중간에 7월까지 for문이 돌수 없습니다.
                            그래서 조회용 변수를 따로 만들어서 사용합니다.
                         */
                        for(int k=dataMap.size(); k>0;  ){
                            list=dataMap.get((cal.get(Calendar.MONTH)+1-i)+"month");    // HashMap으로 조회하여 데이터를 받아서 저장합니다.
                            fileNameList=fileNameDataMap.get((cal.get(Calendar.MONTH)+1-i)+"month_name");       //  HashMap으로 조회하여 파일이름을 받아서 저장합니다.
                            if(fileNameList==null) {
                                Log.d("test",cal.get(Calendar.MONTH)+1-i+"월");
                                i++;
                                continue;
                            }
                            File monthPath = new File(save_path+"/"+((cal.get(Calendar.MONTH)+1-i)+"월"));     // 월별 폴더 생성
                            if(!monthPath.exists()) {
                                monthPath.mkdir();
                            }
                            /*
                                서버로부터 받은 바이트 와 파일 이름을 매칭 시켜 저장합니다.
                                HashMap -> ArrayList -> Byte -> Bitmap -> JPEG파일로 풀어서 재저장합니다.
                             */
                            for(int j=0; j<list.size(); j++){
                                String fileName = fileNameList.get(j);
                                byte[] data= list.get(j);
                                File saveFile = new File(save_path +"/"+(cal.get(Calendar.MONTH)+1-i) +"월/" +fileName);
                               //saveFile.createNewFile();
                                OutputStream out = new FileOutputStream(saveFile.getPath());
                                Log.d("receive", "받은 파일 데이터 크기 " + data.length);
                                out.write(data);
                                out.flush();
                                //sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(saveFile)));   // 데이터 저장 후 갱신 작업을 합니다.
                                out.close();
                            }
                            i++;
                            // 파일을 저장했으므로 k(실제 개수를 계사하는 변수)를 감소시킵니다.
                            k--;
                        }
                           // progressDialog.dismiss();
                        is.close();
                        if(progressDialog!=null) {
                            Log.d("coded", "프로그레스바 종료되었음");
                            progressDialog.dismiss();
                        }
                        Log.d("coded", "다 다운됨");
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
    /*
         처음 프로그램 시작 시 관리자인지 암호로(간단한 테스트)
         확인하고 이전 자료 다운로드를 한다.
         인증이 되지 않으면 앱사용이 되지 않는다.
     */
    public void initProgram() {
        /*
            SharedPreferences를 사용해 1회 인증 후 다운로드 받을 수 있게 합니다.
            SharedPreferences의 "version"이라는 이름은 처음에 빈 문자열이고
            데이터 다운을 받으면 다른 문자열이 들어가 새로운 버전을 받기전까지 인증하지 않아도 됩니다.
         */
        SharedPreferences sf = this.getSharedPreferences(sfVersion, 0);
        String ver = sf.getString("version", "");
    /*       SharedPreferences.Editor editor = sf.edit();
        editor.clear();
        editor.commit();*/
        if(ver.equals("")) {        // 빈 문자열 이면 인증과정을 거쳐야합니다.
            /*
                인증하라는 다이얼로그 창을 띄어주고
                확인 버튼을 누르면 SharedPreferences의 "version"을 "1.0"으로 바꾸로
                백그라운드 쓰레드를 동작시킵니다.
                그외에는 어플사용을 못하게 합니다.
             */
            dlgView = View.inflate(MainActivity.this, R.layout.update_confirm_dialog, null);
            AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
            dlg.setTitle("초기 업데이트 내역");
            dlg.setView(dlgView);
            dlg.setCancelable(false);
            dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    EditText editManagerNo = (EditText) dlgView.findViewById(R.id.edit_manager_no);
                    if(editManagerNo.getText().toString().equals("1234")){      // 시험용이라 비밀번호는 1234로 했습니다.
                        /*
                            사용자에게 프로그램이 정상작동한다고
                            ProgressDialog를 띄어주고 사진을 모두 다운받으면 종료시킵니다.
                         */

                        progressDialog=ProgressDialog.show(MainActivity.this, "", "잠시만 기다리십시오.", true);
                        //업데이트
                        SharedPreferences sf = MainActivity.this.getSharedPreferences(sfVersion, 0);
                        SharedPreferences.Editor editor = sf.edit();
                        editor.putString("version", "1.0");
                        editor.commit();
                        theAsyncThread backgroundInThread = new theAsyncThread();
                        backgroundInThread.execute("init");

                    }else {
                        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                        alert.setTitle("경고");
                        alert.setPositiveButton("확인", new DialogInterface.OnClickListener(){

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        });
                        alert.setMessage("이 어플리케이션은 관리자 전용 어플입니다.\n관리자 외의 사용자는 이용할 수 없습니다.\n"+
                                "잘못입력했다면 다시 시작하여 인증해주십시오.");
                        alert.show();
                    }
                }
            });
            dlg.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            dlg.show();
        }
    }
}
