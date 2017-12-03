package com.example.cong_jh.whatupfi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;

/*
 * [Access Requester]
 * GPS가 꺼져있을 때 GPS를 권한 허용에 대한 창 출력
 */

public class AccessRequester {

    //GPS가 켜져있는지 확인
    public static boolean isLocationEnabled(final Context context) {
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsProviderEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkProviderEnabled = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return isGpsProviderEnabled || isNetworkProviderEnabled;
    }


    //GPS가 권한요청 메세지 출력
    public static AlertDialog.Builder buildLocationAccessDialog(final Activity activity,
                                                                final DialogInterface.OnClickListener onOkClickListener) {
        final String title = "GPS 권한 요청";
        final String message = "GPS를 실행시켜 주십시오.";
        return buildLocationAccessDialog(activity, onOkClickListener, title, message);
    }


    //GPS 설정 창 출력
    public static void requestLocationAccess(final Activity activity) {
        buildLocationAccessDialog(activity, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                activity.startActivity(intent);
            }
        }).show();
    }


    //다이얼로그 창 만드는 함수
   public static AlertDialog.Builder buildLocationAccessDialog(Activity activity,
                                                                DialogInterface.OnClickListener onOkClickListener, String title, String message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok, onOkClickListener);
        builder.setNegativeButton(android.R.string.no, null);
        builder.setCancelable(true);
        return builder;
    }
}