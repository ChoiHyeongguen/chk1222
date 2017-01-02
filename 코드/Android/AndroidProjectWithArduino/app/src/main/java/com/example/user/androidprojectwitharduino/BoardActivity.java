package com.example.user.androidprojectwitharduino;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

/*
    간단한 게시판 액티비티입니다.
    관리자 전용 어플리케이션이서 관리자 끼리 소통할 수 있는 게시글을 작성할 수 있게 했습니다.
    그리고 간단한 소통만 하는 것이므로 삭제는 안되게끔 구성했습니다.
 */
public class BoardActivity extends AppCompatActivity {
    private String ipAddress;
    private Switch sw;
    private Button addBtn, backBtn;
    private EditText editTitle;
    private EditText editContent;
    private View dlgView;
    private ListView mListView = null;
    private ListViewAdapter mAdapter = null;
    private String content = "";
    private String sw_state="";
    private theAsyncThread backgroundInThread, backgroundWriteThread;
    /*
        UI나 AsyncThread로 못하는 작업을 하기위한 방법으로
         핸들러를 생성합니다.
        이 핸들러의 주목적은 서버로 받은 데이터나 추가되는 데이터들을
        갱신하여 보여주기 위해 사용합니다.
     */
    final Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            Bundle b=msg.getData();
            final int version = Build.VERSION.SDK_INT;
            int i=0;
            Drawable icon=null;

            /*
                "title"와 "title"+i의 차이는
                title은 그냥 추가이고 (write)
                title + i는 서버로부터 받은 데이터를 차례대로 추가 및 갱신하기위해 사용하는 것입니다.(init)
             */
            if(b.getString("title") !=null) {
                if(b.getString("importance").equals("importance")) {

                    if (version >= 21)
                        icon = ContextCompat.getDrawable(BoardActivity.this, R.drawable.importance);
                    else
                        icon = getResources().getDrawable(R.drawable.importance);
                }

                if (version >= 21) {

                    mAdapter.addItem(icon, b.getString("title"), b.getString("date"));
                } else {
                    mAdapter.addItem(icon, b.getString("title"), b.getString("date"));
                }
                mAdapter.sort();

                mAdapter.dataChange();

                return;
            }
            while(b.getString("title"+i)!=null) {
                icon=null;
                if(b.getString("importance"+i).equals("importance")) {

                    if (version >= 21)
                        icon = ContextCompat.getDrawable(BoardActivity.this, R.drawable.importance);
                    else
                        icon = getResources().getDrawable(R.drawable.importance);
                }
                if (version >= 21) {
                    mAdapter.addItem(icon, b.getString("title"+i), b.getString("date"+i));
                } else {
                    mAdapter.addItem(icon, b.getString("title"+i), b.getString("date"+i));
                }
                mAdapter.dataChange();
                i++;
            }
        }
    };
    /*
    ViewHolder란, 이름 그대로 뷰들을 홀더에 꼽아놓듯이 보관하는 객체를 말합니다.
    각각의 Row를 그려낼 때 그 안의 위젯들의 속성을 변경하기 위해 findViewById를 호출하는데
    이것의 비용이 큰것을 줄이기 위해 사용합니다.
    여기서 게시판의 정보들을 뷰홀더를 이용해 삽입합니다.
     */
    private class ViewHolder {
        public ImageView mIcon;
        public TextView mText;
        public TextView mDate;
    }
    /*
        화면에 게시판 리스트를 띄우는 리스트뷰를 만드는 클래스
     */
    private class ListViewAdapter extends BaseAdapter {
        private Context mContext = null;
        private ArrayList<ListData> mListData = new ArrayList<ListData>();

        public ListViewAdapter(Context context) {
            super();
            this.mContext = context;
        }

        @Override
        public int getCount() {
            return mListData.size();
        }

        @Override
        public Object getItem(int position) {
            return mListData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();

                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.board_item, null);
                /*
                    viewHolder를 이용하면 이처럼
                    findViewById를 딱한번만 사용하면 됩니다.
                 */
                holder.mIcon = (ImageView) convertView.findViewById(R.id.imageIcon);
                holder.mText = (TextView) convertView.findViewById(R.id.text);
                holder.mDate = (TextView) convertView.findViewById(R.id.date);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            /*
                데이터를 이용해 리스트에 넣고
                완성된 뷰를 반환해줍니다.
             */
            ListData mData = mListData.get(position);
            if (mData.mIcon != null) {
                holder.mIcon.setVisibility(View.VISIBLE);
                holder.mIcon.setImageDrawable(mData.mIcon);

            } else {
                holder.mIcon.setVisibility(View.GONE);
            }

            holder.mText.setText(mData.mTitle);
            holder.mDate.setText(mData.mDate);
            return convertView;
        }
     /*
        확장성을 위해 추가적으로
        데이터의 추가 외에 삭제, 정렬등에 사용할 함수를 만들어놨습니다.
      */
        public void addItem(Drawable icon, String mTitle, String mDate) {
            ListData addInfo = null;
            addInfo = new ListData();
            addInfo.mIcon = icon;
            addInfo.mTitle = mTitle;
            addInfo.mDate = mDate;
            mListData.add(addInfo);
        }

        public void remove(int position) {
            mListData.remove(position);
            dataChange();

        }

        public void sort() {
            Collections.sort(mListData, ListData.ALPHA_COMPARATOR);
            Collections.reverse(mListData);
        }

        public void dataChange() {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);
        ipAddress = getString(R.string.ip_address);
        /*
            추가 버튼을 누르면 다이얼로그가 뜨고 여기에서
            글을 작성하고 확인을 누르면 리스트에 추가됨과 동시에
            서버로 전송되어 데이터베이스에 같이 넣어지게 됩니다.(AsyncTask 쓰레드 이용)
         */
        addBtn = (Button) findViewById(R.id.add_btn);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlgView= View.inflate(BoardActivity.this, R.layout.bulletinboard_add_dialog, null);
                AlertDialog dlg = new AlertDialog.Builder(BoardActivity.this)
                .setTitle("글 작성")
                .setIcon(R.mipmap.ic_launcher)
                .setView(dlgView)
                .setPositiveButton("작성", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editTitle = (EditText) dlgView.findViewById(R.id.edt_title);
                        editContent = (EditText) dlgView.findViewById(R.id.edt_content);

                        sw = (Switch) dlgView.findViewById(R.id.switch_importance);
                        String dateStr, titleStr, contentStr;
                        dateStr = (new Date(Calendar.getInstance().getTimeInMillis())).toString();
                        titleStr = editTitle.getText().toString();
                        contentStr = editContent.getText().toString();
                        if(sw.isChecked())
                            sw_state="importance";
                        else
                            sw_state="";
                        theAsyncThread backgroundOutThread = new theAsyncThread();
                        backgroundOutThread.execute("write", titleStr, contentStr, dateStr,sw_state);

                    }
                })
                .setNegativeButton("취소", null)
                .show();
                /*
                    글 작성 다이얼로그의 크기를 적절하게 변경합니다.

                 */
                WindowManager.LayoutParams params = dlg.getWindow().getAttributes();
                params.width = WindowManager.LayoutParams.MATCH_PARENT;
                params.height = WindowManager.LayoutParams.MATCH_PARENT;
                dlg.getWindow().setAttributes(params);

            }
        });
        backBtn = (Button) findViewById(R.id.back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new ListViewAdapter(this);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new ListViewExampleClickListener());
        theAsyncThread backgroundInThread = new theAsyncThread();
        backgroundInThread.execute("init");
    }

    @Override
    protected void onDestroy() {        // 어플리케이션 종료시 쓰레드의 종료를 요청
        super.onDestroy();
        try {
            if (backgroundInThread.getStatus() == AsyncTask.Status.RUNNING) {
                backgroundInThread.cancel(true);
            } else {
            }

            if (backgroundWriteThread.getStatus() == AsyncTask.Status.RUNNING) {
                backgroundWriteThread.cancel(true);
            } else {
            }
        } catch (Exception e) {
        }
    }
    /*
        클릭시 상세하게 게시판 내용을 보여주는 액티비티를 실행시킵니다.
        총 4개의 데이터 게시판 번호, 중요도, 제목, 날짜를 인자로 같이 넣습니다.
     */
    private class ListViewExampleClickListener implements AdapterView.OnItemClickListener {
        public void onItemClick(AdapterView<?> parentView, View clickedView, int position, long id) {
            Intent intent = new Intent(getApplicationContext(), BulletinBoardContentActivity.class);
            ListData data = mAdapter.mListData.get(position);
            if (data == null)
                return;
            intent.putExtra("no",mAdapter.mListData.size()-position-1);
            intent.putExtra("icon", sw_state);
            intent.putExtra("title", data.mTitle);
            intent.putExtra("date", data.mDate);
            startActivity(intent);
        }

    }
    /*
        여기서 사용하는 AsyncTask는 첫번째의 인자의 값에 따라
        다른 동작을 수행합니다.
        write면 리스트에 값을 넣고 서버에 데이터를 넣어주는 코드를 작성하고
        init을 하면 리스트를 보여주기 위한 데이터들을 다운 받습니다.
     */
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

                if (args[0].equals("write")) {
                    param=URLEncoder.encode("title", "utf-8") + "=" + URLEncoder.encode(args[1], "utf-8") +
                                    "&" + URLEncoder.encode("content", "utf-8") + "=" + URLEncoder.encode(args[2], "utf-8") + "&" +
                                    URLEncoder.encode("date", "utf-8") + "=" + URLEncoder.encode(args[3], "utf-8")+ "&" +
                            URLEncoder.encode("importance", "utf-8") + "=" + URLEncoder.encode(args[4], "utf-8");
                    urlStr = "http://"+ipAddress+":8080/ArduinoServerProject/WriteData.jsp";
                } else {
                    param="";
                    urlStr = "http://"+ipAddress+":8080/ArduinoServerProject/requestInitInfo";

                }
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
                if (args[0].equals("write")) {
                    conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                } else {
                    conn.setRequestProperty("Content-Type", "application/json");
                }
                conn.setDoInput(true);
                conn.setDoOutput(true);

                if (args[0].equals("write")) {      // 게시판 글작성이면 중요도, 내용 등을 서버로 보낸다.
                    os = conn.getOutputStream();
                    os.write(param.getBytes());
                    os.flush();
                    Log.d("test", "write");
                    os.close();

                    if(conn.getResponseMessage().equals("OK")){ // 서버가 받았다면

                        Message msg = handler.obtainMessage();  // 어플내에서도 갱신작업을 handler를 통해 수행
                        Bundle b = new Bundle();
                        b.putString("title",args[1] );
                        b.putString("date",args[3]);
                        Log.d("TT", sw_state);
                        b.putString("importance",args[4]);
                        msg.setData(b);
                        handler.sendMessage(msg);
                        conn.disconnect();
                        return "WRITE_OK";
                    }
                }

                Log.d("coded", conn.getResponseMessage());
                /*
                    초기화시에는 서버에서 데이터를 받아서
                    리스트뷰를 구성해 화면에 보여줍니다.
                 */

                if (args[0].equals("init")) {
                    String response = "";
                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Log.d("coded", "들어옴");
                        BufferedReader bufreader = new BufferedReader(
                                new InputStreamReader(conn.getInputStream(), "utf-8"));
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
                        // JSON이 가진 크기만큼 데이터를 받아옴
                        Log.d("tt", jArr.length() + "");
                        for (int i = 0; i < jArr.length(); i++) {
                            json = jArr.getJSONObject(i);
                            b.putString("title" + i, json.getString("title"));
                            b.putString("date" + i, json.getString("date"));
                            b.putString("importance" + i, json.getString("importance"));
                        }
                        msg.setData(b);
                        handler.sendMessage(msg);
                        bufreader.close();
                    }
                    conn.disconnect();
                    return "OK";
                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
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
}
