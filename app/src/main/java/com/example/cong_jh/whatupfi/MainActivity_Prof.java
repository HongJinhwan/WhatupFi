package com.example.cong_jh.whatupfi;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/*
   [Main Activity - Professor]
   교수 메인 화면
    - Login창에서 정보를 받아와 화면에 출력
    - 스마트 출석 버튼 클릭 시 wifi를 스캔하고 서버에 wifi 세기정보를 보내는 thread 실행
 */

public class MainActivity_Prof extends AppCompatActivity {

    Button btnSmart;
    Button btnPassive;
    String token;
    TextView textClassNum;
    TextView textClassName;
    TextView textProfName;
    WifiManager wifi;
    ProgressDialog progressCycle;

    private CloseProcess backPressCloseHandler;
    ActionBar actionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_prof);

        backPressCloseHandler = new CloseProcess(this);
        actionBar = this.getSupportActionBar();
        actionBar.setTitle("Wasseo Fi");

        btnSmart = (Button)findViewById(R.id.btnSmart);
        textClassName = (TextView)findViewById(R.id.textClassName);
        textClassNum = (TextView)findViewById(R.id.textClassNum);
        textProfName = (TextView)findViewById(R.id.textProfName);

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
        }

        //스마트 출석체크 버튼 클릭시 wifi scan 쓰레드 작동
        btnSmart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!AccessRequester.isLocationEnabled(getApplicationContext())) {
                    AccessRequester.requestLocationAccess(MainActivity_Prof.this);
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
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    //Action Bar 버튼 기능 정의
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int curId = item.getItemId();
        switch (curId) {
            case R.id.menu_logout:
                backPressCloseHandler.logOut();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //wifi scan 종료 리시버 - 스캔이 종료되면 서버로 wifi 정보를 POST하는 쓰레드 실행
    BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                PostWiFi thread = new PostWiFi(wifi,token,textClassNum.getText().toString(),progressCycle);
                thread.execute(context);
            }
        }
    };

    //wifi scan 쓰레드
    public class WiFiScan extends AsyncTask<String, Void, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressCycle = new ProgressDialog(MainActivity_Prof.this);
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
}
