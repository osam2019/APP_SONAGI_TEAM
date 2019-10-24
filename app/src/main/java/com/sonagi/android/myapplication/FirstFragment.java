package com.sonagi.android.myapplication;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.sonagi.android.myapplication.decorators.OneDayDecorator;
import com.sonagi.android.myapplication.decorators.SaturdayDecorator;
import com.sonagi.android.myapplication.decorators.SaveDayDecorator;
import com.sonagi.android.myapplication.decorators.SundayDecorator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class FirstFragment extends Fragment{

    String time,kcal,menu;
    private final OneDayDecorator oneDayDecorator = new OneDayDecorator();
    Cursor cursor;
    MaterialCalendarView datePicker;

    TextView viewDatePick;  //  viewDatePick - 선택한 날짜를 보여주는 textView
    EditText edtDiary;   //  edtDiary - 선택한 날짜의 일기를 쓰거나 기존에 저장된 일기가 있다면 보여주고 수정하는 영역
    Button btnSave;   //  btnSave - 선택한 날짜의 일기 저장 및 수정(덮어쓰기) 버튼
    String dateName;   //  dateName - 돌고 도는 선택된 날짜의 파일 이름
    String [] permission_list={Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.READ_CALENDAR}; //파일입출력을 위한 권한 체크
    String token;
    Boolean isPost;
    int pk;

    public FirstFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        checkPermission(); // 권한 확인
        // Inflate the layout for this fragment

        SharedPreferences sf = getActivity().getSharedPreferences("auth_token", MODE_PRIVATE);
        token = sf.getString("token", "null");

        View view= inflater.inflate(R.layout.fragment_first, container, false);
        datePicker = (MaterialCalendarView)view.findViewById(R.id.datePicker);
        viewDatePick = (TextView) view.findViewById(R.id.viewDatePick);
        edtDiary = (EditText) view.findViewById(R.id.edtDiary);
        btnSave = (Button) view.findViewById(R.id.btnSave);

        //오늘날 체크가 자동으로 되게 하는 코드
        datePicker.setCurrentDate(new Date(System.currentTimeMillis()));
        datePicker.setDateSelected(new Date(System.currentTimeMillis()), true);
        datePicker.setSelectedDate(new Date(System.currentTimeMillis()));

        //달력 셋팅
        datePicker.state().edit()
                .setFirstDayOfWeek(Calendar.SUNDAY)
                .setMinimumDate(CalendarDay.from(2017, 0, 1)) // 달력의 시작
                .setMaximumDate(CalendarDay.from(2030, 11, 31)) // 달력의 끝
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit();
        //주말 데코레이터해주기
        datePicker.addDecorators(
                new SundayDecorator(),
                new SaturdayDecorator()
                );

        // 오늘 날짜를 받게해주는 Calender 친구들
        Calendar c = Calendar.getInstance();

        int cYear = c.get(Calendar.YEAR);
        int cMonth = c.get(Calendar.MONTH);cMonth++;
        int cDay = c.get(Calendar.DAY_OF_MONTH);

        JSONArray jsonArray = getDiaryList(cYear, cMonth);
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String date_string = jsonObject.getString("written");
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date date = simpleDateFormat.parse(date_string);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

                datePicker.addDecorator(new SaveDayDecorator(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 첫 시작 시에는 오늘 날짜 일기 읽어주기
        checkedDay(cYear, cMonth, cDay);
        DateListener listener = new DateListener();
        datePicker.setOnDateChangedListener(listener);


        // 저장/수정 버튼 누르면 실행되는 리스너
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(dateName);
                saveDiary(dateName);
            }
        });
        return view;
    }

    public JSONArray getDiaryList(final int year, final int month) {
        class Get extends AsyncTask<String, Void, JSONArray> {
            @Override
            public JSONArray doInBackground(String... strings) {
                try {
                    URL url = new URL(strings[0] + "diary/list/" + Integer.toString(year) + "/" + Integer.toString(month) + "/");
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

                        JSONArray jsonArray = new JSONArray(response);
                        return jsonArray;
                    } else {
                        Intent intent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        getActivity().finish();

                        throw new Exception();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity().getApplicationContext(), "오류가 발생했습니다.", Toast.LENGTH_LONG).show();

                    JSONArray jsonArray = new JSONArray();
                    return jsonArray;
                }
            }
        }
        try {
            Get get = new Get();
            return get.execute("http://13.125.196.191/").get();
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    public JSONObject getDiaryByDate(final int year, final int month, final int day) {
        class Get extends AsyncTask<String, Void, JSONObject> {
            @Override
            public JSONObject doInBackground(String... strings) {
                try {
                    URL url = new URL(strings[0] + "diary/day/" + Integer.toString(year) + "/" + Integer.toString(month) + "/" + Integer.toString(day) + "/");
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

                        JSONObject jsonObject = new JSONObject(response);
                        return jsonObject;
                    } else {
                        return new JSONObject();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity().getApplicationContext(), "오류가 발생했습니다.", Toast.LENGTH_LONG).show();

                    JSONObject jsonObject = new JSONObject();
                    return jsonObject;
                }
            }
        }
        try {
            Get get = new Get();
            return get.execute("http://13.125.196.191/").get();
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    public Boolean postDiary(final String written, final String content) {
        class Post extends AsyncTask<String, Void, Boolean> {
            public Boolean doInBackground(String... strings) {
                try {
                    URL url = new URL(strings[0] + "diary/write/");
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setReadTimeout(3000);
                    httpURLConnection.setConnectTimeout(3000);
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setRequestProperty("Authorization", "jwt " + token);
                    httpURLConnection.setRequestProperty("Content-Type","application/json");
                    httpURLConnection.setRequestProperty("Accept","application/json");
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setUseCaches(false);

                    JSONObject obj = new JSONObject();
                    obj.put("written", written);
                    obj.put("content", content);

                    OutputStream os = httpURLConnection.getOutputStream();
                    os.write(obj.toString().getBytes());
                    os.flush();

                    int statusCode = httpURLConnection.getResponseCode();

                    if (statusCode == 201) {
                        return true;
                    } else {
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        try {
            Post post = new Post();
            return post.execute("http://13.125.196.191/").get();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Boolean putDiary(final int pk, final String content) {
        class Put extends AsyncTask<String, Void, Boolean> {
            public Boolean doInBackground(String... strings) {
                try {
                    URL url = new URL(strings[0] + "diary/" + Integer.toString(pk) +"/");
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

                    JSONObject obj = new JSONObject();
                    obj.put("content", content);

                    OutputStream os = httpURLConnection.getOutputStream();
                    os.write(obj.toString().getBytes());
                    os.flush();

                    int statusCode = httpURLConnection.getResponseCode();

                    if (statusCode == 202) {
                        return true;
                    } else {
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        try {
            Put put = new Put();
            return put.execute("http://13.125.196.191/").get();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Boolean deleteDiary(final int pk) {
        class Delete extends AsyncTask<String, Void, Boolean> {
            public Boolean doInBackground(String... strings) {
                try {
                    URL url = new URL(strings[0] + "diary/" + Integer.toString(pk) +"/");
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

                    int statusCode = httpURLConnection.getResponseCode();

                    if (statusCode == 201) {
                        Toast.makeText(getActivity().getApplicationContext(), "성공적으로 제거되었습니다.", Toast.LENGTH_LONG).show();
                        return true;
                    } else {
                        Toast.makeText(getActivity().getApplicationContext(), "서버와의 통신이 원활하지 않습니다. 잠시후 다시 시도해 주세요", Toast.LENGTH_LONG).show();
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity().getApplicationContext(), "오류가 발생했습니다.", Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        }
        try {
            Delete delete = new Delete();
            return delete.execute("http://13.125.196.191/").get();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //데이터피커 리스너
    class DateListener implements OnDateSelectedListener{
        @Override
        public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                // 이미 선택한 날짜에 일기가 있는지 없는지 체크해야할 시간이다
                int yearc=date.getYear();
                int days=date.getMonth(); days++;
                int dayq=date.getDay();
                checkedDay(yearc, days, dayq);
        }
    }

    private void checkedDay(int year, int monthOfYear, int dayOfMonth) {
        viewDatePick.setText("일기 작성일 : " + year + " - " + monthOfYear + " - " + dayOfMonth);
        dateName = year + "-" + monthOfYear + "-" + dayOfMonth;

        try {
            Calendar calendar = Calendar.getInstance();
            JSONObject jsonObject = getDiaryByDate(year, monthOfYear, dayOfMonth);

            if (jsonObject.isNull("content")) {
                edtDiary.setText("");
                btnSave.setText("새 일기 저장");
                isPost = true;
            } else {
                String content = jsonObject.getString("content");
                pk = jsonObject.getInt("id");
                isPost = false;

                edtDiary.setText(content);
                btnSave.setText("수정하기");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // 일기 저장하는 메소드
    private void saveDiary(String readDay) {
        try {
            String content = edtDiary.getText().toString();
            Boolean check;
            if (isPost) {
                check = postDiary(readDay, content);
                if (check) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = sdf.parse(readDay);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);

                    datePicker.addDecorator(new SaveDayDecorator(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)));
                }
            } else {
                check = putDiary(pk, content);
            }

            if (check) {
                Toast.makeText(getActivity(), "성공적으로 등록 되었습니다!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), "오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "오류가 발생했습니다.", Toast.LENGTH_LONG).show();
        }
    }
    public void checkPermission(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            return;
        }
        // 각 권한의 허용 여부를 확인한다.
        for(String permission : permission_list){
            // 권한 허용 여부를 확인한다.
            int chk = getActivity().checkCallingOrSelfPermission(permission);
            // 거부 상태라고 한다면..
            if(chk == PackageManager.PERMISSION_DENIED){
                // 사용자에게 권한 허용여부를 확인하는 창을 띄운다.
                requestPermissions(permission_list, 0);
            }
        }
    }

}
