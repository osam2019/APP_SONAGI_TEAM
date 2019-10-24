package com.sonagi.android.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoadingActivity extends Activity {
    private String API_URL = "http://13.125.196.191/auth/";
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        SharedPreferences sf = getSharedPreferences("auth_token", MODE_PRIVATE);
        token = sf.getString("token", "null");

        startLoading();
    }

    private void startLoading() {
        final String f_token = token;
        class Post extends AsyncTask<String, Void, Integer> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Integer doInBackground(String... strings) {
                try {
                    URL url = new URL(strings[0] + "verify/");
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                    httpURLConnection.setReadTimeout(3000);
                    httpURLConnection.setConnectTimeout(3000);
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setRequestProperty("Content-Type", "application/json");
                    httpURLConnection.setRequestProperty("Accept", "application/json");
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setUseCaches(false);

                    // JSON 키-값 설정
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("token", f_token);

                    // body에 json을 실어줌
                    OutputStream os = httpURLConnection.getOutputStream();
                    os.write(jsonObject.toString().getBytes());
                    os.flush();

                    int statusCode = httpURLConnection.getResponseCode();

                    if (statusCode == 200) {
                        return 0;
                    } else {
                        url = new URL(strings[0] + "refresh/");
                        httpURLConnection = (HttpURLConnection) url.openConnection();

                        httpURLConnection.setReadTimeout(3000);
                        httpURLConnection.setConnectTimeout(3000);
                        httpURLConnection.setDoOutput(true);
                        httpURLConnection.setDoInput(true);
                        httpURLConnection.setRequestProperty("Content-Type", "application/json");
                        httpURLConnection.setRequestProperty("Accept", "application/json");
                        httpURLConnection.setRequestMethod("POST");
                        httpURLConnection.setUseCaches(false);

                        // JSON 키-값 설정
                        jsonObject = new JSONObject();
                        jsonObject.put("token", f_token);

                        // body에 json을 실어줌
                        os = httpURLConnection.getOutputStream();
                        os.write(jsonObject.toString().getBytes());
                        os.flush();

                        statusCode = httpURLConnection.getResponseCode();

                        if (statusCode != 200) {
                            return 1;
                        } else {
                            InputStream is = httpURLConnection.getInputStream();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            byte[] byteBuffer = new byte[1024];
                            byte[] byteData = null;
                            int nLength = 0;
                            while ((nLength = is.read(byteBuffer, 0, byteBuffer.length)) != -1) {
                                baos.write(byteBuffer, 0, nLength);
                            }
                            byteData = baos.toByteArray();


                            String response = new String(byteData);

                            JSONObject responseJSON = new JSONObject(response);
                            token = (String) responseJSON.get("token");

                            SharedPreferences sf = getSharedPreferences("auth_token", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sf.edit();
                            editor.putString("token", token);
                            editor.commit();

                            return 0;
                        }
                    }

                } catch (Exception e) {
                    return 2;
                }
            }
        };

        try {
            Post post = new Post();
            Integer check = post.execute(API_URL).get();
            if (check == 0) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                if (check == 2) {
                    Toast.makeText(getApplicationContext(), "예상치 못한 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
                }
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
