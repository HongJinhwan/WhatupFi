package com.example.cong_jh.whatupfi;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

/*
 * [Close Process]
 * back button 눌렀을 때의 기능 정의 (2초안에 2번눌러야 프로그램 종료되게)
 * 로그아웃 기능 정의
 */
public class CloseProcess extends AppCompatActivity{
    private long backKeyPressedTime = 0;
    private Toast toast;
    private Activity activity;
    SharedPreferences appData;

    public CloseProcess(Activity context) {
        this.activity = context;
    }


    //back button 두번눌럿을 때 Activity만 종료
    public void onBackPressed_finish() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            showGuide();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            activity.finish();
            toast.cancel();
        }
    }


    //back button 두번눌럿을 때 어플리케이션 프로세스 전체 종료
    public void onBackPressed_exit() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            showGuide();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            activity.moveTaskToBack(true);
            activity.finish();
            android.os.Process.killProcess(android.os.Process.myPid());

            toast.cancel();
        }
    }


    //back button 한번 눌렀을 때 안내창
    public void showGuide() {
        toast = Toast.makeText(activity, "뒤로 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
        toast.show();
    }


    //로그아웃기능
    //로그인 정보를 모두 제거하고 로그인창 출력
    public void logOut(){
        appData = activity.getSharedPreferences("appData", MODE_PRIVATE);
        SharedPreferences.Editor editor = appData.edit();
        editor.putString("ID", "");
        editor.putString("PASSWORD","");
        editor.putBoolean("CHECK",false);
        editor.apply();
        activity.finish();
        Intent intent = new Intent(activity, LoginActivity.class);
        activity.startActivity(intent);
        Log.i("abcde","로그아웃");
    }
}
