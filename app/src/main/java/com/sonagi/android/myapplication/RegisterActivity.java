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

public class RegisterActivity extends Activity {
    private String API_URL = "http://13.125.196.191/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    public void nextStep(View view) {
        EditText user_id = (EditText)findViewById(R.id.user_id);
        EditText password = (EditText)findViewById(R.id.password);
        EditText first_name = (EditText)findViewById(R.id.first_name);
        EditText last_name = (EditText)findViewById(R.id.last_name);
        EditText email = (EditText)findViewById(R.id.email);

        final String s_id = user_id.getText().toString();
        final String s_password = password.getText().toString();
        final String s_first_name = first_name.getText().toString();
        final String s_last_name = last_name.getText().toString();
        final String s_email = email.getText().toString();

        if (s_id.length() == 0 || s_password.length() == 0 || s_first_name.length() == 0 || s_last_name.length() == 0 || s_email.length() == 0) {
            Toast.makeText(getApplicationContext(), "입력하지 않은 필드가 존재합니다.", Toast.LENGTH_LONG).show();
            return;
        } else if (!s_email.matches("^[a-zA-Z0-9]+@[a-zA-Z0-9]+.[a-zA-Z]{2,8}$")) {
            Toast.makeText(getApplicationContext(), "이메일을 정확한 형식으로 입력 해 주세요.", Toast.LENGTH_LONG).show();
            return;
        } else if (!s_password.matches("^[a-zA-Z0-9!@#$%\\^&*\\(\\)_+]{4,12}$")) {
            Toast.makeText(getApplicationContext(), "비밀번호는 4~12 자리입니다.", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            class Register extends AsyncTask<String, Void, Integer> {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                }

                @Override
                protected Integer doInBackground(String... strings) {
                    try {
                        URL url = new URL(strings[0] + "user/register/");
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
                        jsonObject.put("first_name", s_first_name);
                        jsonObject.put("last_name", s_last_name);
                        jsonObject.put("email", s_email);

                        // body에 json을 실어줌
                        OutputStream os = httpURLConnection.getOutputStream();
                        os.write(jsonObject.toString().getBytes());
                        os.flush();

                        int statusCode = httpURLConnection.getResponseCode();


                        if (statusCode == 201) {
                            url = new URL(strings[0] + "auth/");

                            httpURLConnection = (HttpURLConnection) url.openConnection();

                            httpURLConnection.setReadTimeout(3000);
                            httpURLConnection.setConnectTimeout(3000);
                            httpURLConnection.setDoOutput(true);
                            httpURLConnection.setDoInput(true);
                            httpURLConnection.setRequestProperty("Content-Type","application/json");
                            httpURLConnection.setRequestProperty("Accept","application/json");
                            httpURLConnection.setRequestMethod("POST");
                            httpURLConnection.setUseCaches(false);

                            // JSON 키-값 설정
                            jsonObject = new JSONObject();
                            jsonObject.put("username", s_id);
                            jsonObject.put("password", s_password);

                            os = httpURLConnection.getOutputStream();
                            os.write(jsonObject.toString().getBytes());
                            os.flush();

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
                            return 0;
                        } else { // message 체크
                            InputStream is = httpURLConnection.getErrorStream();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            byte[] byteBuffer = new byte[1024];
                            byte[] byteData = null;
                            int nLength = 0;
                            while((nLength = is.read(byteBuffer, 0, byteBuffer.length)) != -1) {
                                baos.write(byteBuffer, 0, nLength);
                            }
                            byteData = baos.toByteArray();

                            String response = new String(byteData);
                            System.out.println(response);

                            JSONObject responseJSON = new JSONObject(response);
                            String message = (String) responseJSON.get("message");

                            if (message.equals("info already exists")) {
                                return 1;
                            } else if (message.equals("user already exists")) {
                                return 2;
                            } else if (message.equals("email already exists")) {
                                return 3;
                            } else if (message.equals("key error")) {
                                return 4;
                            } else {
                                return -1;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return -1;
                    }
                }

                @Override
                protected void onPostExecute(Integer result) {
                    super.onPostExecute(result);
                }
            };
            Register register = new Register();
            Integer check = register.execute(API_URL).get();

            if (check == 0) {
                Intent intent = new Intent(getBaseContext(), InfoRegisterActivity.class);
                startActivity(intent);

                finish();
            } else if (check == 1) {
                Toast.makeText(getApplicationContext(), "사용자 정보가 중복 됩니다.", Toast.LENGTH_LONG).show();
            } else if (check == 2) {
                Toast.makeText(getApplicationContext(), "아이디가 이미 존재 합니다.", Toast.LENGTH_LONG).show();
            } else if (check == 3) {
                Toast.makeText(getApplicationContext(), "이메일이 중복 됩니다.", Toast.LENGTH_LONG).show();
            } else if (check == 4) {
                Toast.makeText(getApplicationContext(), "입력하지 않은 필드가 존재합니다.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
