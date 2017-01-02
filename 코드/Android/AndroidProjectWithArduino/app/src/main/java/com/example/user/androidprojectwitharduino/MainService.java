package com.example.user.androidprojectwitharduino;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.Socket;
import java.net.URI;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.StringTokenizer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/*
    안드로이드의 서비스를 백그라운드로 돌려서
    소켓서버와 계속 연결상태를 유지합니다.
    그리고 서버가 보낼 데이터가 있으면 전송하고
    연결된 소켓 클라이언트의 리시브 쓰레드를 통해 사진을 받습니다.
    사진을 받고 다시 재연결을 시도해 연결상태를 유지합니다.
 */
public class MainService extends Service {
    private String save_path="";
    private static int port = 5001;
    private String ipText; // IP지정으로 사용시에 쓸 코드
    private SocketClient client;
    private ReceiveThread receive;
    private Socket socket;
    private LinkedList<SocketClient> threadList;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("서비스 테스트", "onStartCommand 시작");
/*
        Save_folder = "/SmartSafetyZone";

        // 다운로드 경로를 외장메모리 사용자 지정 폴더로 함.
        String ext = Environment.getExternalStorageState();

        if(ext.equals(Environment.MEDIA_MOUNTED)) {
            Save_Path = Environment.getExternalStorageDirectory().getAbsolutePath() + Save_folder;
        }*/
        threadList = new LinkedList<MainService.SocketClient>();
        client = new SocketClient(ipText, port+"");
        threadList.add(client);
        client.start();
        Log.i("서비스 테스트", "onStartCommand");
        return super.onStartCommand(intent, flags,startId);
    }
    @Override
    public void onCreate(){
        ipText=getString(R.string.ip_address);
    }
    @Override
    public void onDestroy() {
        Log.i("서비스 테스트", "삭제됬다 ㅜㅜ");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Bundle b = msg.getData();
            String fileDes = b.getString("file_des");
//               switch(msg.what) {}
//               Toast.makeText(getApplicationContext(), "알림!", 0).show();
            NotificationManager manager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(MainService.this);
            builder.setSmallIcon(R.drawable.emergency)
                    .setContentTitle("긴급! 안전 사고 발생!!!")
                    .setContentText(" 긴급상황입니다!!! 빠르게 클릭하셔서 빠른 대처하세요!")
                    .setAutoCancel(true) // 알림바에서 자동 삭제
                    .setVibrate(new long[]{1000,2000,1000,3000,1000,4000});
            // vibrate : 쉬고, 울리고, 쉬고, 울리고... 밀리세컨
            // 진동이 되려면 AndroidManifest.xml에 진동 권한을 줘야 한다.

            // 알람 클릭시 MainActivity를 화면에 띄운다.
            Intent intent = new Intent(getApplicationContext(),ConfirmEmergencyActivity.class);
            intent.putExtra("file_des",fileDes);
            if (intent == null) {
                intent = new Intent();
            }
            PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext()
                    , 0
                    , intent
                    , PendingIntent.FLAG_UPDATE_CURRENT
            );
            builder.setContentIntent(pIntent);

            Context hostActivity = MainService.this;
            SharedPreferences sf = PreferenceManager.getDefaultSharedPreferences(hostActivity);
            String ring = sf.getString("phone_ring", "");
            if(!ring.equals("")) {
                if(!ring.equals("no")) {
                    builder.setSound( Uri.parse(ring));
                }
            }else {
                Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                builder.setSound(alarmSound);
            }
            manager.notify(1, builder.build());
        };
    };
    class SocketClient extends Thread {
        boolean threadAlive;
        String ip;
        String port;
        public SocketClient(String ip, String port) {
            threadAlive = true;
            this.ip = ip;
            this.port = port;
            Log.i("서비스 테스트", "onStartCommand 연결준비 ");
        }
        @Override
        public void run() {

            Log.i("서비스 테스트", "onStartCommand 연결ㄱㄱ ");
            try {
                // 연결후 바로 ReceiveThread 시작

                socket = new Socket(ip, Integer.parseInt(port));
                Log.i("서비스 테스트", "onStartCommand 연결완료 ");
                receive = new ReceiveThread(socket);
                receive.start();
                if(receive.isAlive())
                    return ;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class ReceiveThread extends Thread {        // 서버로 부터 보내는 사진을 수신하는 쓰레드
        private Socket socket = null;
        DataInputStream input;
        public ReceiveThread(Socket socket) {
            this.socket = socket;
            try {
                input = new DataInputStream(socket.getInputStream());       // 소켓의 inputsteam을 따로 변수에 저장

            } catch (Exception e) {
            }
        }
        public void run(){
            Log.d("go", "시작");

            String ext = Environment.getExternalStorageState();     // 외부 SD카드가 있는지 상태정보를 읽고
            if(ext.equals(Environment.MEDIA_MOUNTED)) {     // SD카드가 장착되있다면 이것을 이용해 최종저장경로를 만듭니다.
                save_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SmartSafetyZone";
            }
            String fileName=null;
            if(input!=null && socket.isConnected()) {
                try {
                    fileName = input.readUTF();     // 서버가 보내는 스트링을 읽는다.
                    /*
                        BufferedInputStream의 readLine으로 받으면 데이터 경계때문인지
                        write의 에러가 많이나서 readUTF로 바꿨습니다.
                     */
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (fileName != null &&socket.isConnected()) {      // 파일이름을 받았고 연결 중이라면
                    Log.d("go", fileName);
                    try { // 데이터 전송 시작
                        InputStream is = socket.getInputStream();
                        ObjectInputStream ois = new ObjectInputStream(is);  // Object로
                        byte[] data = (byte[]) ois.readObject();        // byte를 한번에 전송 받습니다.

                        Calendar cal = Calendar.getInstance();
                        File monthPath = new File(save_path+"/"+(cal.get(Calendar.MONTH)+1)+"월");     // 월별 폴더 생성
                        if(!monthPath.exists()) {
                            monthPath.mkdir();
                        }
                        File saveFile = new File(monthPath + "/" + fileName);     // 파일의 이름은 서버의 파일이름을 그대로 사용합니다.
                       // saveFile.createNewFile();       // 빈파일생성
                        Log.d("receive", "받은 파일 데이터 크기 " + data.length);
                        OutputStream out = new FileOutputStream(saveFile.getPath());

                                /*
                                    Byte를 Bitmap으로 저장하고 다시 JPEG파일로 변환합니다.
                                 *//*
                                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                                if(bitmap==null)
                                    continue;
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);*/
                        out.write(data);
                        out.flush();
                        out.close();
                        /*
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);    // 바이트를 비트맵으로 변환하고
                        if (bitmap != null)
                        {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);      // JPEG로 빈파일에 스트림작성해서 JPG파일로 만듭니다.
                        }else {
                            return;
                        }
                        */
                        /*
                        여기는 테스트 후에 작성
                         */
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(saveFile)));

                        Message msg = mHandler.obtainMessage();
                        Bundle b = new Bundle();
                        b.putString("file_des", monthPath + "/" + fileName);
                        msg.setData(b);
                        mHandler.sendMessage(msg);
                        Log.d("receive", "받은 파일 데이터 크기 " + data.length);
                        Log.i("서비스 테스트", "데이터를 받았두다.");
                        //showDownloadFile();
                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();
                    } catch (IOException e1) {

                        e1.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    /*
                    전송되면 2초후 소켓을 닫습니다.
                     */
                    try {
                        Thread.sleep(2000);
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.i("서비스 테스트", "재시작");
                    /*
                    다시 소켓쓰레드를 생성해 서버와의 재연결을 시도합니다.
                     */
                    if(socket.isClosed()) {
                        client = new SocketClient(ipText, port + "");
                        threadList.add(client); // 쓰레드 리스트에 저장
                        client.start(); //쓰레드 시작
                        if (client.isAlive())   // 쓰레드가 활성화되면
                            return; // 현재 쓰레드를 종료합니다.
                    }
                }
            }
        }
    }
}
