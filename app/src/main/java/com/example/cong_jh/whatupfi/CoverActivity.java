package com.example.cong_jh.whatupfi;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/*
   [Cover Activity]
   프로그램 시작 시 가장 먼저 보이는 액티비티
   1. 표지 출력
   2. 권한 확인
    - 권한이 설정되어있는지 확인 후 설정되지 않는 권한에 대하여 권한허가창 출력
    - 권한이 설정되어있지 않으면 어플리케이션에 접근하지 못하게
    - 쓰레드로 권한이 설정되어있는지 확인 -> 사용자가 권한 설정을 완료하면 바로 다음 액티비티로 넘어가게
 */

public class CoverActivity extends AppCompatActivity implements Runnable{
    ActionBar actionBar;
    Thread myThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cover);

        actionBar = this.getSupportActionBar();
        actionBar.hide();

        //권한이 설정되어있는지 확인 후 설정되지 않는 권한에 대하여 권한허가창 출력
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_DENIED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_DENIED
            || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED){
            TextView textView = (TextView)findViewById(R.id.textView8);
            textView.setText("권한승인이 필요합니다.\n뒤로가기 버튼으로\n어플을 다시 실행시켜 주십시오");
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE}, 1);
            myThread = new Thread(this);
            myThread.start();
        }
        //쓰레드로 권한이 설정되어있는지 확인 -> 사용자가 권한 설정을 완료하면 바로 다음 액티비티로 넘어가게
        else {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    Intent intent = new Intent(CoverActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 500);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }


    //권한이 설정되어있지 않으면 어플리케이션에 접근하지 못하게
    //쓰레드로 권한이 설정되어있는지 확인
    @Override
    public void run() {
        boolean run= true;
        while(run){
            if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_DENIED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_DENIED
            ||ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED
            || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED))
            {
                run = false;
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        }
    }
}
