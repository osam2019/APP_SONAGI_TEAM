package com.sonagi.android.myapplication;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class ThirdFragment extends Fragment {

    private String API_URL = "http://13.125.196.191/";
    private String token;
    private TextView classes;
    private TextView name;
    private TextView d_day;
    private TextView end_date;
    private TextView promotion_date;
    private TextView regular_holiday;
    private TextView reward_holiday;
    private TextView consolation_holiday;
    private TextView total_holiday;
    private TextView say;
    private  View view;
    private Button info_modi,info_out;
    Intent intent;

    public ThirdFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_third, container, false);
        return view;
    }
    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences sf = getActivity().getSharedPreferences("auth_token", MODE_PRIVATE);
        token = sf.getString("token", "null");

        if (token.equals("null")) {
            Intent intent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        }

        classes = (TextView)view.findViewById(R.id.classes);
        name = (TextView)view.findViewById(R.id.name);
        d_day = (TextView)view.findViewById(R.id.d_day);
        end_date = (TextView)view.findViewById(R.id.end_date);
        promotion_date = (TextView)view.findViewById(R.id.promotion_date);
        regular_holiday = (TextView)view.findViewById(R.id.regular_holiday);
        reward_holiday = (TextView)view.findViewById(R.id.reward_holiday);
        consolation_holiday = (TextView)view.findViewById(R.id.consolation_holiday);
        total_holiday = (TextView)view.findViewById(R.id.total_holiday);
        say = (TextView)view.findViewById(R.id.say);
        info_modi=(Button)view.findViewById(R.id.info_modi);
        info_out=(Button)view.findViewById(R.id.info_out);

        class Get extends AsyncTask<String, Void, Pair<Boolean, JSONObject>> {
            public Pair<Boolean, JSONObject> doInBackground(String... strings) {
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
                        Pair<Boolean, JSONObject> pair = new Pair<Boolean, JSONObject>(true, responseJSON);

                        return pair;
                    } else if (statusCode == 404) {
                        JSONObject responseJSON = new JSONObject("{\"message\":\"404\"}");
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
                }
                catch (Exception e) {
                    e.printStackTrace();
                    JSONObject responseJSON = new JSONObject();
                    Pair<Boolean, JSONObject> pair = new Pair<Boolean, JSONObject>(false, responseJSON);
                    return pair;
                }
            }

        }

        try {
            Get get = new Get();
            Pair<Boolean, JSONObject> pair = get.execute(API_URL).get();

            if (pair.first) {
                if (pair.second.isNull("message")) { // 성공
                    dataProcess(pair.second);

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    if (!prefs.getBoolean("firstTime", false)) {

                        Intent alarmIntent = new Intent(getActivity(), AlarmReceiver.class);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, alarmIntent, 0);

                        AlarmManager manager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(System.currentTimeMillis());
                        calendar.set(Calendar.HOUR_OF_DAY, 18);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);

                        manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                                AlarmManager.INTERVAL_DAY, pendingIntent);

                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("firstTime", true);
                        editor.apply();
                    }


                } else {
                    Intent intent = new Intent(getActivity().getApplicationContext(), InfoRegisterActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "심상치 않은 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        View.OnClickListener listener = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
            switch(v.getId()) {
                case R.id.info_modi:
                    Log.d("msg: ","nonono");
                    intent = new Intent(getActivity().getApplicationContext(), InfoModifyActivity.class);
                    startActivity(intent);
                    break;
                case R.id.info_out:
                    SharedPreferences sf = getActivity().getSharedPreferences("auth_token", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sf.edit();
                    editor.putString("token", "null");
                    editor.commit();

                    intent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                    break;

            }
            }
        };
        info_modi.setOnClickListener(listener);
        info_out.setOnClickListener(listener);
    }

    public void dataProcess(JSONObject jsonObject) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String s_name = jsonObject.getString("first_name") + jsonObject.getString("last_name");
            Date today = new Date(); // 오늘 날짜
            Date d_pfc = sdf.parse(jsonObject.getString("private_first_class"));
            Date corparal = sdf.parse(jsonObject.getString("corparal"));
            Date sergeant = sdf.parse(jsonObject.getString("sergeant"));
            Date end = sdf.parse(jsonObject.getString("end_date")); // 종료 일

            end_date.setText(jsonObject.getString("end_date"));
            name.setText(s_name);
            regular_holiday.setText(Integer.toString(jsonObject.getInt("regular_holiday")));
            reward_holiday.setText(Integer.toString(jsonObject.getInt("reward_holiday")));
            consolation_holiday.setText(Integer.toString(jsonObject.getInt("consolation_holiday")));
            total_holiday.setText(Integer.toString(jsonObject.getInt("regular_holiday") + jsonObject.getInt("reward_holiday") + jsonObject.getInt("consolation_holiday")));
            say.setText(jsonObject.getString("say"));

            long diffInMillies = today.getTime() - end.getTime();
            long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            d_day.setText("D" + diff);

            if (today.compareTo(d_pfc) < 0) {
                classes.setText("이병");
                promotion_date.setText(jsonObject.getString("private_first_class") + " (일병)");
            } else if (today.compareTo(corparal) < 0) {
                classes.setText("일병");
                promotion_date.setText(jsonObject.getString("corparal") + " (상병)");
            } else if (today.compareTo(sergeant) < 0) {
                classes.setText("상병");
                promotion_date.setText(jsonObject.getString("sergeant") + " (병장)");
            } else if (today.compareTo(end) < 0){
                classes.setText("병장");
                promotion_date.setText(jsonObject.getString("end_date") + " (민간인)");
            } else {
                classes.setText("민간인");
                promotion_date.setText("그치만 나는 민간인인걸");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}
