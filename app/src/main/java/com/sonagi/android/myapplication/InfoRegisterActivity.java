package com.sonagi.android.myapplication;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Pattern;

public class InfoRegisterActivity extends Activity {
    private String API_URL = "http://13.125.196.191/";
    private TextView start_date;
    private TextView end_date;
    private String token;
    private int s_y;
    private int s_m;
    private int s_d;
    private int e_y;
    private int e_m;
    private int e_d;

    private DatePickerDialog.OnDateSetListener startCallbackMethod;
    private DatePickerDialog.OnDateSetListener endCallbackMethod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_register);

        SharedPreferences sf = getSharedPreferences("auth_token", MODE_PRIVATE);
        token = sf.getString("token", "null");

        start_date = (TextView)findViewById(R.id.start_date);
        end_date = (TextView)findViewById(R.id.end_date);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Calendar cal = Calendar.getInstance();
        s_y = e_y = cal.get(cal.YEAR);
        s_m = e_m = cal.get(cal.MONTH);
        s_d = e_d = cal.get(cal.DATE);
        start_date.setText(simpleDateFormat.format(System.currentTimeMillis()));
        end_date.setText(simpleDateFormat.format(System.currentTimeMillis()));

        startCallbackMethod = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                s_y = i;
                s_m = i1 + 1;
                s_d = i2;
                start_date.setText(s_y + "-" + String.format("%02d", s_m) + "-" + String.format("%02d", s_d));
            }
        };
        endCallbackMethod = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                e_y = i;
                e_m = i1 + 1;
                e_d = i2;
                end_date.setText(e_y + "-" + String.format("%02d", e_m) + "-" + String.format("%02d", e_d));
            }
        };
    }

    public void startDatePicker(View view) {
        DatePickerDialog dialog = new DatePickerDialog(this, startCallbackMethod, s_y, s_m, s_d);
        dialog.show();
    }

    public void endDatePicker(View view) {
        DatePickerDialog dialog = new DatePickerDialog(this, endCallbackMethod, e_y, e_m, e_d);
        dialog.show();
    }

    public void infoRegister(View view) {
        EditText say = (EditText)findViewById(R.id.say);

        final String s_start_date = start_date.getText().toString();
        final String s_end_date = end_date.getText().toString();
        final String s_say = say.getText().toString();
        final String f_token = token;


        if (s_start_date.length() == 0 || s_end_date.length() == 0 || s_say.length() == 0) {
            Toast.makeText(getApplicationContext(), "입력하지 않은 필드가 존재합니다.", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            class InfoRegister extends AsyncTask<String, Void, Integer> {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                }

                @Override
                protected Integer doInBackground(String... strings) {
                    try {
                        URL url = new URL(strings[0] + "user/info_register/");
                        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                        httpURLConnection.setReadTimeout(3000);
                        httpURLConnection.setConnectTimeout(3000);
                        httpURLConnection.setDoOutput(true);
                        httpURLConnection.setDoInput(true);
                        httpURLConnection.setRequestProperty("Authorization", "jwt " + f_token);
                        httpURLConnection.setRequestProperty("Content-Type","application/json");
                        httpURLConnection.setRequestProperty("Accept","application/json");
                        httpURLConnection.setRequestMethod("POST");
                        httpURLConnection.setUseCaches(false);

                        // JSON 키-값 설정
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("start_date", s_start_date);
                        jsonObject.put("end_date", s_end_date);
                        jsonObject.put("say", s_say);

                        // body에 json을 실어줌
                        OutputStream os = httpURLConnection.getOutputStream();
                        os.write(jsonObject.toString().getBytes());
                        os.flush();

                        int statusCode = httpURLConnection.getResponseCode();


                        if (statusCode == 201) {
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
                            } else if (message.equals("key error")) {
                                return 2;
                            } else {
                                return 3;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return 3;
                    }
                }

                @Override
                protected void onPostExecute(Integer result) {
                    super.onPostExecute(result);
                }
            };
            InfoRegister register = new InfoRegister();
            Integer check = register.execute(API_URL).get();

            if (check == 0) {
                Toast.makeText(getApplicationContext(), "계정 생성 성공!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            } else if (check == 1) {
                Toast.makeText(getApplicationContext(), "이미 추가 정보가 입력되어 있습니다.", Toast.LENGTH_LONG).show();
            } else if (check == 2) {
                Toast.makeText(getApplicationContext(), "입력하지 않은 필드가 존재합니다.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "뭔가 단단히 잘못됐습니다.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
