package com.sonagi.android.myapplication;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class InfoModifyActivity extends AppCompatActivity {

    private String API_URL = "http://13.125.196.191/";
    private TextView start_date;
    private TextView end_date;
    private TextView private_first_class;
    private TextView corparal;
    private TextView sergeant;
    private TextView regular_holiday;
    private TextView reward_holiday;
    private TextView consolation_holiday;
    private EditText say;
    private String token;
    private JSONObject jsonObject;

    private DatePickerDialog.OnDateSetListener startCallbackMethod;
    private DatePickerDialog.OnDateSetListener endCallbackMethod;
    private DatePickerDialog.OnDateSetListener pfcCallbackMethod;
    private DatePickerDialog.OnDateSetListener corparalCallbackMethod;
    private DatePickerDialog.OnDateSetListener sergeantCallbackMethod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_modify);

        SharedPreferences sf = getSharedPreferences("auth_token", MODE_PRIVATE);
        token = sf.getString("token", "null");

        start_date = (TextView)findViewById(R.id.start_date);
        end_date = (TextView)findViewById(R.id.end_date);
        private_first_class = (TextView)findViewById(R.id.pfc);
        corparal = (TextView)findViewById(R.id.corparal);
        sergeant = (TextView)findViewById(R.id.sergeant);
        say = (EditText)findViewById(R.id.say);
        regular_holiday = (TextView)findViewById(R.id.regular_holiday);
        reward_holiday = (TextView)findViewById(R.id.reward_holiday);
        consolation_holiday = (TextView)findViewById(R.id.consolation_holiday);

        startCallbackMethod = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                start_date.setText(i + "-" + String.format("%02d", i1 + 1) + "-" + String.format("%02d", i2));
            }
        };
        endCallbackMethod = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                end_date.setText(i + "-" + String.format("%02d", i1 + 1) + "-" + String.format("%02d", i2));
            }
        };
        pfcCallbackMethod = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                private_first_class.setText(i + "-" + String.format("%02d", i1 + 1) + "-" + String.format("%02d", i2));
            }
        };
        corparalCallbackMethod = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                corparal.setText(i + "-" + String.format("%02d", i1 + 1) + "-" + String.format("%02d", i2));
            }
        };
        sergeantCallbackMethod = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                sergeant.setText(i + "-" + String.format("%02d", i1 + 1) + "-" + String.format("%02d", i2));
            }
        };

        class Get extends AsyncTask<String, Void, JSONObject> {
            protected JSONObject doInBackground(String... strings){
                try {
                    URL url = new URL(strings[0] + "user/info/");
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setReadTimeout(3000);
                    httpURLConnection.setConnectTimeout(3000);
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setRequestProperty("Authorization", "jwt " + token);
                    httpURLConnection.setRequestProperty("Content-Type","application/json");
                    httpURLConnection.setRequestProperty("Accept","application/json");
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setUseCaches(false);

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
                        System.out.println(response);

                        JSONObject responseJSON = new JSONObject(response);
                        return responseJSON;
                    } else {
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
                        return responseJSON;
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    JSONObject jsonObject = new JSONObject();
                    return jsonObject;
                }
            }
        };

        try {
            Get get = new Get();
            jsonObject = get.execute(API_URL).get();

            if (jsonObject.length() == 0) {
                Toast.makeText(getApplicationContext(),"뭔가 단단히 잘못 됐습니다.", Toast.LENGTH_LONG).show();
            } else if (jsonObject.isNull("message")) {
                start_date.setText(jsonObject.getString("start_date"));
                end_date.setText(jsonObject.getString("end_date"));
                private_first_class.setText(jsonObject.getString("private_first_class"));
                corparal.setText(jsonObject.getString("corparal"));
                sergeant.setText(jsonObject.getString("sergeant"));
                regular_holiday.setText(Integer.toString(jsonObject.getInt("regular_holiday")));
                reward_holiday.setText(Integer.toString(jsonObject.getInt("reward_holiday")));
                consolation_holiday.setText(Integer.toString(jsonObject.getInt("consolation_holiday")));
                say.setText(jsonObject.getString("say"));
            } else {
                Toast.makeText(getApplicationContext(), "정보가 존재하지 않습니다.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void startDatePicker(View view) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(jsonObject.getString("start_date"));
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            DatePickerDialog dialog = new DatePickerDialog(this, startCallbackMethod, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void endDatePicker(View view) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(jsonObject.getString("end_date"));
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            DatePickerDialog dialog = new DatePickerDialog(this, endCallbackMethod, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pfcDatePicker(View view) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(jsonObject.getString("private_first_class"));
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            DatePickerDialog dialog = new DatePickerDialog(this, pfcCallbackMethod, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void corparalDatePicker(View view) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(jsonObject.getString("corparal"));
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            DatePickerDialog dialog = new DatePickerDialog(this, corparalCallbackMethod, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sergeantDatePicker(View view) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(jsonObject.getString("sergeant"));
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            DatePickerDialog dialog = new DatePickerDialog(this, sergeantCallbackMethod, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void decreaseBtn1(View view) {
        int rh = Integer.parseInt(regular_holiday.getText().toString()) - 1;

        if (rh < 0)
            Toast.makeText(getApplicationContext(),"0 이하로 설정 할 수 없습니다!", Toast.LENGTH_SHORT);
        else
            regular_holiday.setText(Integer.toString(rh));
    }

    public void increaseBtn1(View view) {
        int rh = Integer.parseInt(regular_holiday.getText().toString()) + 1;
        regular_holiday.setText(Integer.toString(rh));
    }

    public void decreaseBtn2(View view) {
        int rh = Integer.parseInt(reward_holiday.getText().toString()) - 1;
        if (rh < 0)
            Toast.makeText(getApplicationContext(),"0 이하로 설정 할 수 없습니다!", Toast.LENGTH_SHORT);
        else
            reward_holiday.setText(Integer.toString(rh));
    }

    public void increaseBtn2(View view) {
        int rh = Integer.parseInt(reward_holiday.getText().toString()) + 1;
        reward_holiday.setText(Integer.toString(rh));
    }

    public void decreaseBtn3(View view) {
        int ch = Integer.parseInt(consolation_holiday.getText().toString()) - 1;
        if (ch < 0)
            Toast.makeText(getApplicationContext(),"0 이하로 설정 할 수 없습니다!", Toast.LENGTH_SHORT);
        else
            consolation_holiday.setText(Integer.toString(ch));
    }

    public void increaseBtn3(View view) {
        int ch = Integer.parseInt(consolation_holiday.getText().toString()) + 1;
        consolation_holiday.setText(Integer.toString(ch));
    }

    public void modify(View view) {
        class Put extends AsyncTask<String, Void, Pair<Boolean, JSONObject>> {
            protected Pair<Boolean, JSONObject> doInBackground(String... strings) {
                try {
                    URL url = new URL(strings[0] + "user/info/");
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setReadTimeout(3000);
                    httpURLConnection.setConnectTimeout(3000);
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setRequestProperty("Authorization", "jwt " + token);
                    httpURLConnection.setRequestProperty("Content-Type","application/json");
                    httpURLConnection.setRequestProperty("Accept","application/json");
                    httpURLConnection.setRequestMethod("PUT");
                    httpURLConnection.setUseCaches(false);

                    // JSON 키-값 설정
                    jsonObject = new JSONObject();
                    jsonObject.put("start_date", start_date.getText());
                    jsonObject.put("end_date", end_date.getText());
                    jsonObject.put("say", say.getText());
                    jsonObject.put("private_first_class", private_first_class.getText());
                    jsonObject.put("corparal", corparal.getText());
                    jsonObject.put("sergeant", sergeant.getText());
                    jsonObject.put("regular_holiday", Integer.parseInt(regular_holiday.getText().toString()));
                    jsonObject.put("reward_holiday", Integer.parseInt(reward_holiday.getText().toString()));
                    jsonObject.put("consolation_holiday", Integer.parseInt(consolation_holiday.getText().toString()));

                    // body에 json을 실어줌
                    OutputStream os = httpURLConnection.getOutputStream();
                    os.write(jsonObject.toString().getBytes());
                    os.flush();

                    int statusCode = httpURLConnection.getResponseCode();

                    if (statusCode == 202) {
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

                        Pair<Boolean, JSONObject> pair = new Pair<Boolean, JSONObject>(true, responseJSON);
                        return pair;
                    } else {
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
                        Pair<Boolean, JSONObject> pair = new Pair<Boolean, JSONObject>(false, responseJSON);
                        return pair;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JSONObject responseJSON = new JSONObject();
                    Pair<Boolean, JSONObject> pair = new Pair<Boolean, JSONObject>(true, responseJSON);
                    return pair;
                }
            }
        }
        try {
            Put put = new Put();
            Pair<Boolean, JSONObject> pair = put.execute(API_URL).get();
            if (pair.first) {
                Toast.makeText(getApplicationContext(), "정보 변경에 성공 하였습니다!", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "심상치 않은 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void cancel(View view) {
        onBackPressed();
    }
}
