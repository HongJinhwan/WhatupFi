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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/*
   [Get Wifi]
   서버에서 현재 강좌의 와이파이 세기정보를 가져와 출결상태 체크, DB에 등록
 */

public class GetWiFi extends AsyncTask<Context, Void, Integer>{
    WifiManager wifi;
    List apList;
    ScanResult scanResult;
    ArrayList<WiFi> profWifiList = new ArrayList<>();
    ArrayList<WiFi> myWifiList = new ArrayList<>();

    String token;
    String studentNum;
    String profNum;
    String classNum;
    String class_url = "http://10.70.4.104:8000/Class/";
    String time_url = "http://10.70.4.104:8000/Check/timeLimit/";

    Context context;
    ProgressDialog progressCyle;

    int time_responcecode;
    int  coincidence = 0;


    public GetWiFi(WifiManager wifi, String token, String studentNum, String classNum, ProgressDialog progressCyle, String profNum ) {
        this.wifi = wifi;
        this.token = token;
        this.studentNum = studentNum;
        this.classNum = classNum;
        this.progressCyle = progressCyle;
        this.profNum = profNum;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


    //쓰레드로 내 와이파이 목록을 가져오고
    //서버에서 교수의 와이파이 목록을 가져온 다음
    //비교해서 일치하는 갯수 리턴
    @Override
    protected Integer doInBackground(Context... context) {
        this.context = context[0];
        getMyWifiList();
        getProfWifi();
        int a = compareWifi(profWifiList, myWifiList);
        return a;
    }


    //일치하는 갯수를 이용해 학생의 출석 여부 확인
    //서버와 통신하여 출석여부 확인
    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        Log.i("abcde","포스트 "+String.valueOf(integer) + " " + String.valueOf(profWifiList.size()));
        if(integer>3 && time_responcecode == 201){
            Toast.makeText(context,"출석에 성공했습니다.",Toast.LENGTH_SHORT).show();
        }
        else if(time_responcecode == 207){
            Toast.makeText(context,"이미 출석하셨습니다.",Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(context,"출석에 실패했습니다.",Toast.LENGTH_SHORT).show();
        }
        progressCyle.dismiss();
    }


    //내 와이파이 목록 스캔
    public void getMyWifiList(){
        apList = wifi.getScanResults();
        if (wifi.getScanResults() != null) {
            for (int i = 0; i < apList.size(); i++) {
                scanResult = (ScanResult) apList.get(i);
                Log.i("abcde",scanResult.SSID+scanResult.BSSID+scanResult.level);
                myWifiList.add(new WiFi(scanResult.SSID,scanResult.BSSID,scanResult.level));
            }
        }
    }


    //서버에서 교수의 와이파이를 가져옴
    //서버에서 현재 교수가 출석중인지 확인
    public void getProfWifi(){
        String readline ="";

        //서버에서 교수의 와이파이를 가져옴
        try{
            URL url = new URL(class_url+classNum+"/");
            Log.d("abcde", "서비스트라이 "+url.toString());

            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setReadTimeout(5000);
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setRequestMethod("GET");// do not use "GET" in your case
            httpURLConnection.setRequestProperty("Authorization", "JWT "+token);//whatever you want

            httpURLConnection.connect();
            Log.d("abcde", "Main response code - " + httpURLConnection.getResponseCode() + " " + httpURLConnection.getResponseMessage());

            InputStream inputStream= httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            readline = bufferedReader.readLine();
            Log.d("abcde",readline);
            bufferedReader.close();

            httpURLConnection.disconnect();
        }
        catch(Exception e) {
            Log.d("abcde", "PostException3");
        }

        try {
            JSONObject jsonObject = new JSONObject(readline);
            readline = jsonObject.get("wifi").toString();
            JSONArray jsonArray = new JSONArray(readline);
            for(int i=0; i<jsonArray.length(); i++) {
                JSONObject jsonObject2 = (JSONObject) jsonArray.get(i);
                profWifiList.add(new WiFi(jsonObject2.get("BSSID").toString(),jsonObject2.get("BSSID").toString(),(int)jsonObject2.get("LEVEL")));
            }
            Log.d("abcde","교수 : "+jsonArray.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //서버에서 현재 교수가 출석중인지 확인
        try {
            URL url = new URL(time_url+classNum+"/"+studentNum+"/");
            Log.d("abcde",url.toString());
            Log.d("abcde", "타임트라이");
            String dataToSend =
                    "{\"classNum\" : \""+classNum+"\"," +
                            "\"username\" : \""+studentNum+"\"," +
                            "\"attendCheck\" : \"1\"}";
            Log.d("abcde",dataToSend);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setReadTimeout(2000);
            httpURLConnection.setConnectTimeout(2000);
            httpURLConnection.setDoOutput(true);//enable to write data to output stream
            httpURLConnection.setRequestMethod("POST");// do not use "GET" in your case
            httpURLConnection.setRequestProperty("Authorization", "JWT "+token);//whatever you want
            httpURLConnection.setRequestProperty("Content-Type", "application/json");//whatever you want

            OutputStream os = httpURLConnection.getOutputStream();
            os.write(dataToSend.getBytes());
            os.flush();
            os.close();
            httpURLConnection.connect();
            Log.d("abcde", "Main response code - " + httpURLConnection.getResponseCode() + " " + httpURLConnection.getResponseMessage());
            time_responcecode = httpURLConnection.getResponseCode();

            httpURLConnection.disconnect();
        } catch (Exception e) {
            Log.d("abcde", "time Exception");
            e.printStackTrace();
        }
    }


    //교수의 wifi세기와 학생의 wifi세기 비교
    //BSSID가 같고 와이파이 세기가 유사한 값들의 갯수 리턴
    public int compareWifi(List<WiFi> profWifiList, List<WiFi> myWifiList){
        Log.i("abcde",String.valueOf(profWifiList.size())+" "+String.valueOf(profWifiList.size()));
        for(int i=0; i<profWifiList.size(); i++){
            for(int j=0; j<myWifiList.size(); j++){
                if(myWifiList.get(j).BSSID.equals(profWifiList.get(i).BSSID)) {
                    if (myWifiList.get(j).level < profWifiList.get(i).level + 50 && myWifiList.get(j).level > profWifiList.get(i).level - 50) {
                        Log.i("abcde","맞는것 발견!");
                        coincidence++;
                        break;
                    }
                }
            }
        }
        return coincidence;
    }
}
