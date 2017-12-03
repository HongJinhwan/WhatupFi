package com.example.cong_jh.whatupfi;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/*
   [Post Wifi]
   서버에서 현재 강좌의 와이파이 세기정보를 보내 DB에 등록
 */

public class PostWiFi extends AsyncTask<Context, Void, Context> {
    WifiManager wifi;
    List apList;
    ScanResult scanResult;
    ProgressDialog progressCycle;

    String token;
    String classNum;
    String class_url = "http://10.70.4.104:8000/Class/";

    public PostWiFi(WifiManager wifi, String token, String classNum, ProgressDialog progressCycle) {
        this.wifi = wifi;
        this.token = token;
        this.classNum = classNum;
        this.progressCycle = progressCycle;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Context context) {
        super.onPostExecute(context);
        progressCycle.dismiss();
        Toast.makeText(context,"출석 시작",Toast.LENGTH_SHORT).show();
    }

    //내 와이파이 목록을 JSON으로 파싱해 서버에 전송
    @Override
    protected Context doInBackground(Context... contexts) {
        apList = wifi.getScanResults();
        JSONObject postJSON = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        if (wifi.getScanResults() != null) {
            try {
                for (int i = 0; i < apList.size(); i++) {
                    scanResult = (ScanResult) apList.get(i);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("SSID", scanResult.SSID);
                    jsonObject.put("BSSID", scanResult.BSSID);
                    jsonObject.put("LEVEL", scanResult.level);
                    jsonArray.put(jsonObject);
                }
                postJSON.put("wifi",jsonArray.toString());
            } catch(JSONException e){}
        }

        try{
            Log.i("abcde",postJSON.toString());
            URL url = new URL(class_url+classNum+"/");
            Log.d("abcde", "서비스트라이 "+url.toString());

            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setReadTimeout(5000);
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setRequestMethod("PUT");// do not use "GET" in your case
            httpURLConnection.setRequestProperty("Authorization", "JWT "+token);//whatever you want
            httpURLConnection.setRequestProperty("Content-Type", "application/json");//whatever you want

            OutputStream os = httpURLConnection.getOutputStream();
            os.write(postJSON.toString().getBytes());
            os.flush();
            os.close();
            httpURLConnection.connect();
            Log.d("abcde", "Main response code - " + httpURLConnection.getResponseCode() + " " + httpURLConnection.getResponseMessage());
            httpURLConnection.disconnect();
        }
        catch(Exception e) {
            Log.d("abcde", "PostException3");
        }
        return contexts[0];
    }
}
