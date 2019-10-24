package com.sonagi.android.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class LoginActivity extends Activity {

    private String API_URL = "http://13.125.196.191/auth/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void loginBtn(View view) {
        EditText user_id = (EditText)findViewById(R.id.user_id);
        EditText password = (EditText)findViewById(R.id.password);

        final String s_id = user_id.getText().toString();
        final String s_password = password.getText().toString();

        try {
            class Post extends AsyncTask<String, Void, Boolean> {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                }

                @Override
                protected Boolean doInBackground(String... strings) {
                    try {
                        URL url = new URL(strings[0]);
                        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                        httpURLConnection.setReadTimeout(3000);
                        httpURLConnection.setConnectTimeout(3000);
                        httpURLConnection.setDoOutput(true);
                        httpURLConnection.setDoInput(true);
                        httpURLConnection.setRequestProperty("Content-Type","application/json");
                        httpURLConnection.setRequestProperty("Accept","application/json");
                        httpURLConnection.setRequestMethod("POST");
                        httpURLConnection.setUseCaches(false);

                        // JSON 키-값 설정
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("username", s_id);
                        jsonObject.put("password", s_password);

                        // body에 json을 실어줌
                        OutputStream os = httpURLConnection.getOutputStream();
                        os.write(jsonObject.toString().getBytes());
                        os.flush();

                        int statusCode = httpURLConnection.getResponseCode();

                        if (statusCode == 200) {
                            InputStream is = httpURLConnection.getInputStream();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            byte[] byteBuffer = new byte[1024];
                            byte[] byteData = null;
                            int nLength = 0;
                            while((nLength = is.read(byteBuffer, 0, byteBuffer.length)) != -1) {
                                baos.write(byteBuffer, 0, nLength);
                            }
                            byteData = baos.toByteArray();

                            String response = new String(byteData);

                            JSONObject responseJSON = new JSONObject(response);
                            String token = (String) responseJSON.get("token");

                            SharedPreferences sf = getSharedPreferences("auth_token", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sf.edit();
                            editor.putString("token", token);
                            editor.commit();
                            return true;
                        } else {
                            return false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }

                @Override
                protected void onPostExecute(Boolean result) {
                    super.onPostExecute(result);
                }
            };
            Post post = new Post();
            Boolean check = post.execute(API_URL).get();

            if (check) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "사용자 정보가 일치 하지 않습니다. 다시 한번 확인해 주세요", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void registerBtn(View view) {
        Intent intent = new Intent(getBaseContext(), RegisterActivity.class);
        startActivity(intent);
        finish();
    }
}
