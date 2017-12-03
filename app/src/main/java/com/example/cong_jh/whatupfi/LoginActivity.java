package com.example.cong_jh.whatupfi;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/*
   [Login Activity]
   로그인 창 출력 액티비티
    - 로그인 기능 (서버로 id, passwd를 보낸 후 token값 받아옴)
    - 자동로그인 기능 (id, passwd를 sharedpreference에 저장 후 추후 어플리케이션 실행 시 자동으로 로그인)
 */
public class LoginActivity extends AppCompatActivity{
    Button btnLogin;
    CheckBox checkAutoLogin;
    EditText editID;
    EditText editPasswd;

    String id;
    String passwd;
    boolean check;

    String responceMessage = "";
    String login_url = "http://10.70.4.104:8000/api-token-auth/";
    String classProf_url = "http://10.70.4.104:8000/Class/now/";
    String classStudent_url = "http://10.70.4.104:8000/Class/now/student/";
    String member_url = "http://10.70.4.104:8000/Members/";
    String token;

    SharedPreferences appData;
    private CloseProcess backPressCloseHandler;
    ActionBar actionBar;
    JSONObject jsonObject;


    String classNum = "없음";
    String className;
    String profName;
    String userName;
    String phoneNumber;
    String profNum;
    int isProf;
    int code;
    int timelimit_returncode;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        actionBar = this.getSupportActionBar();
        actionBar.hide();
        backPressCloseHandler = new CloseProcess(this);

        btnLogin = (Button) findViewById(R.id.btnLogin);
        checkAutoLogin = (CheckBox) findViewById(R.id.checkAutoLogin);
        editID = (EditText) findViewById(R.id.editID);
        editPasswd = (EditText) findViewById(R.id.editPassword);



        //Sharedpreference에 값이 남아있고 자동로그인 기능이 활성화 되어 있으면
        //저장된 id, passwd를 이용하여 로그인 진행
        appData = getSharedPreferences("appData", MODE_PRIVATE);
        id = appData.getString("ID", "");
        passwd = appData.getString("PASSWORD","");
        check = appData.getBoolean("CHECK",false);
        checkAutoLogin.setChecked(check);
        if(!id.equals("") && !passwd.equals("") && checkAutoLogin.isChecked()){
            try {
                editID.setText(id);
                editPasswd.setText(passwd);
                jsonObject = new JSONObject();
                jsonObject.put("username", editID.getText().toString());
                jsonObject.put("password", editPasswd.getText().toString());
            } catch(JSONException e){
            }
            PostLoginData thread = new PostLoginData();
            thread.execute(jsonObject.toString());
        }
        else{
            editID.setText(id);
        }


        //로그인 버튼을 누르면 로그인 진행
        btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    jsonObject = new JSONObject();
                    id = editID.getText().toString();
                    passwd = editPasswd.getText().toString();
                    jsonObject.put("username", id);
                    jsonObject.put("password", passwd);
                } catch(JSONException e){
                }
                PostLoginData thread = new PostLoginData();
                thread.execute(jsonObject.toString());
            }
        });
    }


    //뒤로가기 키 두번누르면 꺼지게
    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed_finish();
    }


    //쓰레드로 서버와 통신해 로그인 과정 처리
    //id,passwd POST -> token GET -> 사용자정보 GET -> 현재수업정보 GET
    public class PostLoginData extends AsyncTask<String, Void, Integer> {
        ProgressDialog progressCyle;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressCyle = new ProgressDialog(LoginActivity.this);
            progressCyle.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressCyle.setMessage("로그인 중입니다.");
            progressCyle.setCanceledOnTouchOutside(false);
            progressCyle.setCancelable(false);
            progressCyle.show();
        }
        @Override
        protected Integer doInBackground(String... strings) {
            //id, passwd를 POST해 token을 가져옴
            try {
                URL url = new URL(login_url);
                Log.d("abcde", "첫번째트라이");
                String dataToSend = strings[0];
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setReadTimeout(2000);
                httpURLConnection.setConnectTimeout(2000);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);//enable to write data to output stream
                httpURLConnection.setRequestMethod("POST");// do not use "GET" in your case
                httpURLConnection.setRequestProperty("Content-Type", "application/json");//whatever you want

                OutputStream os = httpURLConnection.getOutputStream();
                os.write(dataToSend.getBytes());
                os.flush();
                os.close();
                httpURLConnection.connect();
                Log.d("abcde", "Main response code - " + httpURLConnection.getResponseCode() + " " + httpURLConnection.getResponseMessage());
                responceMessage = httpURLConnection.getResponseMessage();

                InputStream inputStream= httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                token = bufferedReader.readLine();
                bufferedReader.close();

                try {
                    jsonObject = new JSONObject(token);
                    token = jsonObject.get("token").toString();
                    Log.d("abcde", token);
                } catch (JSONException e) {
                    token = null;
                    e.printStackTrace();
                }
                responceMessage = httpURLConnection.getResponseMessage();
                httpURLConnection.disconnect();
            } catch (Exception e) {
                Log.d("abcde", "1st Exception");
                progressCyle.dismiss();
                e.printStackTrace();
                return 0;
            }

            //token으로 사용자의 이름, 교수/학생여부, 전화번호를 GET함
            try{
                URL url = new URL(member_url+id+"/");
                //+profNum+"/"
                Log.d("abcde", "두번째트라이");
                Log.d("abcde", member_url);

                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setReadTimeout(2000);
                httpURLConnection.setConnectTimeout(2000);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setRequestMethod("GET");// do not use "GET" in your case
                httpURLConnection.setRequestProperty("Authorization", "JWT "+token);//whatever you want

                httpURLConnection.connect();
                Log.d("abcde", "Main response code - " + httpURLConnection.getResponseCode() + " " + httpURLConnection.getResponseMessage());

                InputStream inputStream= httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String readline = bufferedReader.readLine();
                Log.d("abcde",readline);
                Log.d("abcde",httpURLConnection.getResponseMessage());
                bufferedReader.close();

                try {
                    jsonObject = new JSONObject(readline);
                    userName = jsonObject.get("name").toString();
                    isProf = (int)jsonObject.get("distinction");
                    phoneNumber = jsonObject.get("phoneNumber").toString();
                } catch (JSONException e) {
                    Log.d("abcde", "JSONException3");
                    e.printStackTrace();
                }

                code = httpURLConnection.getResponseCode();
                httpURLConnection.disconnect();
            }
            catch(Exception e){
                progressCyle.dismiss();
                Log.d("abcde", "2nd Exception");
                return 0;
            }

            //token으로 현재 수업의 학수번호, 수업이름, 교수이름, 담당 교수 학번을 GET함
            try{
                URL url = new URL(classProf_url +id+"/");
                Log.d("abcde", "세번째트라이 "+url.toString());

                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setReadTimeout(2000);
                httpURLConnection.setConnectTimeout(2000);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setRequestMethod("GET");// do not use "GET" in your case
                httpURLConnection.setRequestProperty("Authorization", "JWT "+token);//whatever you want

                httpURLConnection.connect();
                Log.d("abcde", "Main response code - " + httpURLConnection.getResponseCode() + " " + httpURLConnection.getResponseMessage());

                InputStream inputStream= httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String readline = bufferedReader.readLine();
                Log.d("abcde",readline);
                Log.d("abcde",httpURLConnection.getResponseMessage());
                bufferedReader.close();

                try {
                    jsonObject = new JSONObject(readline);
                    classNum = jsonObject.get("classNum").toString();
                    className = jsonObject.get("className").toString();
                    profName = jsonObject.get("profName").toString();
                    profNum = jsonObject.get("username").toString();
                } catch (JSONException e) {
                    Log.d("abcde", "JSONException3");
                    e.printStackTrace();
                }

                httpURLConnection.disconnect();
            }
            catch(Exception e) {
                progressCyle.dismiss();
                Log.d("abcde", "3rd Exception");
            }

            //(학생일 때 추가적으로) token으로 현재 교수가 출석체크 중인지 여부를 responce code를 통해 알아냄
            if (isProf==0){
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
                    timelimit_returncode = httpURLConnection.getResponseCode();
                    httpURLConnection.disconnect();
                }
                catch(Exception e) {
                    progressCyle.dismiss();
                    Log.d("abcde", "3rd Exception");
                }
            }
            return code;
        }


        //서버와의 통신 과정에서 Responce code로 200(OK)가 왔을 때 로그인 진행
        //중복 로그인 방지를 위해 전화번호 DB와 일치여부 확인
        @Override
        protected void onPostExecute(Integer responceCode) {
            Log.d("abcde", "post "+String.valueOf(responceCode));
            super.onPostExecute(responceCode);
            Log.i("abcde",phoneNumber + " " + ReadPhoneNumber());

            //서버와의 통신 과정에서 Responce code로 200(OK)가 왔을 때 로그인 진행
            //중복 로그인 방지를 위해 전화번호 DB와 일치여부 확인
            if(responceCode == 200 && ReadPhoneNumber().equals(phoneNumber)) {
                Toast.makeText(getApplicationContext(), "로그인 되었습니다", Toast.LENGTH_SHORT).show();
                SharedPreferences.Editor editor = appData.edit();
                editor.putString("ID", editID.getText().toString()); //로그인 성공 시 아이디 저장

                //자동로그인 기능 활성화 시 아이디, 비밀번호 모두 저장
                if(checkAutoLogin.isChecked()){
                    editor.putString("PASSWORD",editPasswd.getText().toString());
                    editor.putBoolean("CHECK",true);
                }
                editor.apply();

                //교수/학생 여부에 따라 다른 Activity 실행 (넘겨주는 값에도 차이)
                if(isProf == 1){
                    Intent intent = new Intent(LoginActivity.this, MainActivity_Prof.class);
                    intent.putExtra("token",token);
                    intent.putExtra("classNum",classNum);
                    intent.putExtra("className",className);
                    intent.putExtra("profNum",id);
                    intent.putExtra("profName",userName);
                    startActivity(intent);
                    finish();
                }
                else{
                    Intent intent = new Intent(LoginActivity.this, MainActivity_Student.class);
                    intent.putExtra("token",token);
                    intent.putExtra("classNum",classNum);
                    intent.putExtra("className",className);
                    intent.putExtra("studentNum",id);
                    intent.putExtra("studentName",userName);
                    intent.putExtra("profName",profName);
                    intent.putExtra("timecode",timelimit_returncode);
                    intent.putExtra("profNum",profNum);
                    startActivity(intent);
                    finish();
                }
            }
            //catch문, 다른 responce code 일 때 로그인 실패 안내문 출력
            else{
                Toast.makeText(getApplicationContext(), "아이디와 비밀번호를 확인하여 주십시오", Toast.LENGTH_SHORT).show();
                progressCyle.dismiss();
            }
        }
    }


    //기기의 휴대폰번호 확인
    String ReadPhoneNumber(){
        String myNumber = null;
        TelephonyManager mgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        try{
            myNumber = mgr.getLine1Number();
            myNumber = myNumber.replace("+82", "0");

        }catch(Exception e){
        }
        return myNumber;
    }

}
