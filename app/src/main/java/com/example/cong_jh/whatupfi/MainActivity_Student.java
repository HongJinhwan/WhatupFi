package com.example.cong_jh.whatupfi;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.net.HttpURLConnection;
import java.net.URL;

/*
   [Main Activity - Student]
   학생 메인 화면
    - Login창에서 정보를 받아와 화면에 출력
    - 스마트 출석 버튼 클릭 시 wifi를 스캔하고 서버에서 wifi 세기정보를 받는 thread 실행
 */

public class MainActivity_Student extends AppCompatActivity {
    Button btnSmart;
    String token;
    TextView textClassNum;
    TextView textClassName;
    TextView textStudentNum;
    TextView textStudentName;
    TextView textProfName;
    int timecode;
    String profNum;

    WifiManager wifi;
    ProgressDialog progressCycle;
    String classStudent_url = "http://10.70.4.104:8000/Class/now/student/";

    private CloseProcess backPressCloseHandler;
    ActionBar actionBar;
    SharedPreferences appData;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_student);

        appData = getSharedPreferences("appData", MODE_PRIVATE);
        backPressCloseHandler = new CloseProcess(this);
        actionBar = this.getSupportActionBar();
        actionBar.setTitle("Wasseo Fi");

        btnSmart = (Button)findViewById(R.id.btnSmart2);
        textClassName = (TextView)findViewById(R.id.textClassName2);
        textClassNum = (TextView)findViewById(R.id.textClassNum2);
        textProfName = (TextView)findViewById(R.id.textProfName2);
        textStudentNum = (TextView)findViewById(R.id.textStudentID);
        textStudentName = (TextView)findViewById(R.id.textStudentName);

        //wifi scan 종료 리시버 등록
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(wifiReceiver, filter);

        //이전 Activity에서 전달된 값들
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            token = bundle.getString("token");
            textClassName.setText(bundle.getString("className"));
            textClassNum.setText(bundle.getString("classNum"));
            textProfName.setText(bundle.getString("profName"));
            textStudentNum.setText(bundle.getString("studentNum"));
            textStudentName.setText(bundle.getString("studentName"));
            timecode = bundle.getInt("timecode");
            profNum = bundle.getString("profNum");
        }

        //교수가 출석체크 중이 아니면 스마트 출석체크 버튼 비활성화
        if(timecode != 200){
            btnSmart.setEnabled(false);
            Drawable background = getResources().getDrawable(R.drawable.button_gray);
            btnSmart.setBackground(background);
        }

        //스마트 출석체크 버튼 클릭시 wifi scan 쓰레드 작동
        btnSmart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!AccessRequester.isLocationEnabled(getApplicationContext())) {
                    AccessRequester.requestLocationAccess(MainActivity_Student.this);
                    return;
                }
                if (AccessRequester.isLocationEnabled(getApplicationContext())) {
                    WiFiScan thread = new WiFiScan();
                    thread.execute();
                }
            }
        });
    }


    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed_exit();
    }


    //Action Bar 커스터마이징
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu2, menu);
        return true;
    }


    //Action Bar 버튼 기능 저의 - 로그아웃, 새로고침
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int curId = item.getItemId();
        switch (curId) {
            case R.id.menu_logout:
                backPressCloseHandler.logOut();
                return true;
            case R.id.menu_refresh:
                Refresh thread = new Refresh();
                thread.execute();
                if(timecode == 200){
                    btnSmart.setEnabled(true);
                    Drawable background = getResources().getDrawable(R.drawable.button);
                    btnSmart.setBackground(background);
                }
                else{
                    btnSmart.setEnabled(false);
                    Drawable background = getResources().getDrawable(R.drawable.button_gray);
                    btnSmart.setBackground(background);
                }
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    //wifi scan 종료 리시버 - 스캔이 종료되면 서버로 wifi 정보를 GET하여 비교한 후 출결을 체크하는 쓰레드 실행
    BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                GetWiFi thread = new GetWiFi(wifi,token,textStudentNum.getText().toString(),textClassNum.getText().toString(),progressCycle,profNum);
                thread.execute(context);
            }
        }
    };


    //wifi scan 쓰레드
    public class WiFiScan extends AsyncTask<String, Void, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressCycle = new ProgressDialog(MainActivity_Student.this);
            progressCycle.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressCycle.setMessage("출석체크 중입니다.");
            progressCycle.setCanceledOnTouchOutside(false);
            progressCycle.setCancelable(false);
            progressCycle.show();
        }

        @Override
        protected Integer doInBackground(String... strings) {
            wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifi.startScan();
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
        }
    }

    //새로고침 쓰레드 (교수가 출결중인지 확인하여 가지고옴)
    public class Refresh extends AsyncTask<String, Void, Integer> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Integer doInBackground(String... strings) {
            try{
                URL url = new URL(classStudent_url +profNum+"/");
                Log.d("abcde", "네번째트라이 "+url.toString());

                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setReadTimeout(2000);
                httpURLConnection.setConnectTimeout(2000);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setRequestMethod("GET");// do not use "GET" in your case
                httpURLConnection.setRequestProperty("Authorization", "JWT "+token);//whatever you want

                httpURLConnection.connect();
                Log.d("abcde", "Main response code - " + httpURLConnection.getResponseCode() + " " + httpURLConnection.getResponseMessage());
                timecode = httpURLConnection.getResponseCode();
                httpURLConnection.disconnect();
            }
            catch(Exception e) {
                Log.d("abcde", "3rd Exception");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
        }
    }
}
