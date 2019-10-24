package com.sonagi.android.myapplication;


import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sonagi.android.myapplication.row.Address_Item;
import com.sonagi.android.myapplication.today_tab.CheckSchedule;
import com.sonagi.android.myapplication.today_tab.MyAdapter;
import com.sonagi.android.myapplication.today_tab.MyListDecoration;
import com.sonagi.android.myapplication.today_tab.Schedule;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;


/**
 * A simple {@link Fragment} subclass.
 */
public class SecondFragment extends Fragment {

    private RecyclerView listview;
    private MyAdapter adapter;

    private ArrayList<String> spinnerArray;
    private ArrayAdapter<String> arrayAdapter;
    private Spinner spinner;
    private int cal_month;
    private int cal_year;

    Button btnAdd, btnDel, prevBtn, nextBtn;
    TextView cal_text;
    EditText editText;
    String token;
    JSONArray monthSchedule;
    JSONArray currentSchedule;

    //일정 목록을 띄워주는 뷰를
    private ArrayList<CheckSchedule> arrayList=new ArrayList<CheckSchedule>();
    private ListView mListView;
    com.sonagi.android.myapplication.row.MyAdapter myAdapter=new com.sonagi.android.myapplication.row.MyAdapter();




    public SecondFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_second, container, false);

        SharedPreferences sf = getActivity().getSharedPreferences("auth_token", MODE_PRIVATE);
        token = sf.getString("token", "null");

        if (token.equals("null")) {
            Intent intent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        }

        prevBtn = (Button)view.findViewById(R.id.prev);
        nextBtn = (Button)view.findViewById(R.id.next);
        cal_text = (TextView)view.findViewById(R.id.calendar);

        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prevMonth(view);
            }
        });
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextMonth(view);
            }
        });

        init(view);


        return view;
    }

    public void prevMonth(View view) {
        if (cal_month == 1) {
            cal_month = 12;
            cal_year--;
        } else {
            cal_month--;
        }
        cal_text.setText(cal_year +  " / " + cal_month);

        monthSchedule = getSchedule(cal_year, cal_month);
        myAdapter.initArray();

        try {
            for (int i = 0; i < monthSchedule.length(); i++) {
                JSONObject jsonObject = monthSchedule.getJSONObject(i);
                int pk = jsonObject.getInt("id");
                String title = jsonObject.getString("title");
                String start_date = jsonObject.getString("start_date");
                String end_date = jsonObject.getString("end_date");
                int type = jsonObject.getInt("schedule_type");
                myAdapter.addItem(pk, start_date, end_date, title, type);
            }
        } catch (Exception e) {
            Toast.makeText(getActivity().getApplicationContext(), "오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        mListView.setAdapter(myAdapter);
        mListView.deferNotifyDataSetChanged();


    }

    public void nextMonth(View view) {
        if (cal_month == 12) {
            cal_month = 1;
            cal_year++;
        } else {
            cal_month++;
        }
        cal_text.setText(cal_year +  " / " + cal_month);

        monthSchedule = getSchedule(cal_year, cal_month);
        myAdapter.initArray();

        try {
            for (int i = 0; i < monthSchedule.length(); i++) {
                JSONObject jsonObject = monthSchedule.getJSONObject(i);
                int pk = jsonObject.getInt("id");
                String title = jsonObject.getString("title");
                String start_date = jsonObject.getString("start_date");
                String end_date = jsonObject.getString("end_date");
                int type = jsonObject.getInt("schedule_type");
                myAdapter.addItem(pk, start_date, end_date, title, type);
            }
        } catch (Exception e) {
            Toast.makeText(getActivity().getApplicationContext(), "오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        mListView.setAdapter(myAdapter);
        mListView.deferNotifyDataSetChanged();
    }

    public JSONArray getNearSchedule() {
        class Get extends AsyncTask<String, Void, JSONArray> {
            @Override
            public JSONArray doInBackground(String... strings) {
                try {
                    URL url = new URL(strings[0] + "schedule/near/");
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

                        JSONArray responseJSON = new JSONArray(response);
                        return responseJSON;
                    } else {
                        return new JSONArray();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return new JSONArray();
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

    public JSONArray getSchedule(final int year, final int month) {
        class Get extends AsyncTask<String, Void, JSONArray> {
            @Override
            public JSONArray doInBackground(String... strings) {
                try {
                    URL url = new URL(strings[0] + "schedule/list/" + Integer.toString(year) + "/" + Integer.toString(month) + "/");
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

    public JSONObject postSchedule(final int type, final String title, final String start_date, final String end_date) {
        class Post extends AsyncTask<String, Void, JSONObject> {
            public JSONObject doInBackground(String... strings) {
                try {
                    URL url = new URL(strings[0] + "schedule/write/");
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
                    obj.put("schedule_type", type);
                    obj.put("title", title);
                    obj.put("start_date", start_date);
                    obj.put("end_date", end_date);
                    obj.put("content", title);

                    OutputStream os = httpURLConnection.getOutputStream();
                    os.write(obj.toString().getBytes());
                    os.flush();

                    int statusCode = httpURLConnection.getResponseCode();



                    if (statusCode == 201) {
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
                        Intent intent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        getActivity().finish();

                        throw new Exception();
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
            Post post = new Post();
            return post.execute("http://13.125.196.191/").get();
        } catch (Exception e) {
            e.printStackTrace();
            JSONObject jsonObject = new JSONObject();
            return jsonObject;
        }
    }

    public Boolean putSchedule(final int pk, final int type, final String title, final String start_date, final String end_date) {
        class Put extends AsyncTask<String, Void, Boolean> {
            public Boolean doInBackground(String... strings) {
                try {
                    URL url = new URL(strings[0] + "schedule/" + Integer.toString(pk) +"/");
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
                    obj.put("schedule_type", type);
                    obj.put("title", title);
                    obj.put("start_date", start_date);
                    obj.put("end_date", end_date);
                    obj.put("content", title);

                    OutputStream os = httpURLConnection.getOutputStream();
                    os.write(obj.toString().getBytes());
                    os.flush();

                    int statusCode = httpURLConnection.getResponseCode();

                    if (statusCode == 202) {
                        Toast.makeText(getActivity().getApplicationContext(), "성공적으로 등록되었습니다.", Toast.LENGTH_LONG).show();
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
            Put put = new Put();
            return put.execute("http://13.125.196.191/").get();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Boolean deleteSchedule(final int pk) {
        class Delete extends AsyncTask<String, Void, Boolean> {
            public Boolean doInBackground(String... strings) {
                try {
                    URL url = new URL(strings[0] + "schedule/" + Integer.toString(pk) +"/");
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

                    if (statusCode == 202) {
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

    public void init(View view){

        mListView=(ListView)view.findViewById(R.id.listview1);

        Calendar calendar = Calendar.getInstance();
        System.out.println(calendar.toString());
        cal_year = calendar.get(calendar.YEAR);
        cal_month = calendar.get(calendar.MONTH) + 1;
        cal_text.setText(cal_year +  " / " + cal_month);
        monthSchedule = getSchedule(calendar.get(calendar.YEAR), calendar.get(calendar.MONTH) + 1);
        myAdapter.initArray();

        try {
            for (int i = 0; i < monthSchedule.length(); i++) {
                JSONObject jsonObject = monthSchedule.getJSONObject(i);
                int pk = jsonObject.getInt("id");
                String title = jsonObject.getString("title");
                String start_date = jsonObject.getString("start_date");
                String end_date = jsonObject.getString("end_date");
                int type = jsonObject.getInt("schedule_type");
                myAdapter.addItem(pk, start_date, end_date, title, type);
            }
        } catch (Exception e) {
            Toast.makeText(getActivity().getApplicationContext(), "오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        mListView.setAdapter(myAdapter);

        listview = (RecyclerView)view.findViewById(R.id.main_listview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        listview.setLayoutManager(layoutManager);
        ArrayList<Schedule> itemList = new ArrayList<>(); //itemlist를 불러와서 서버에 저장하세요
        JSONArray jsonArray = getNearSchedule();

        try {
            if (jsonArray.length() == 0) {
                Schedule schedule = new Schedule();
                schedule.schedule_type = -1;
                schedule.start_date = "null";
                schedule.end_date = "null";
                schedule.title = "null";
                schedule.pk = -1;
                itemList.add(schedule);
            } else {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    Schedule schedule = new Schedule();
                    schedule.schedule_type = object.getInt("schedule_type");
                    schedule.start_date = object.getString("start_date");
                    schedule.end_date = object.getString("end_date");
                    schedule.title = object.getString("title");
                    schedule.pk = object.getInt("id");
                    itemList.add(schedule);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity().getApplicationContext(), "에러 발생", Toast.LENGTH_LONG).show();
        }

        btnAdd=(Button)view.findViewById(R.id.btnAdd);
        btnDel=(Button)view.findViewById(R.id.btnDel);

        adapter = new MyAdapter(getActivity(), itemList, onClickItem);
        listview.setAdapter(adapter);

        MyListDecoration decoration = new MyListDecoration();
        listview.addItemDecoration(decoration);


         View.OnClickListener listener = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                switch(v.getId()) {
                    case R.id.btnAdd:
                        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
                        builder.setTitle("일정 입력");
                        LayoutInflater inflater = getLayoutInflater();
                        View v1 = inflater.inflate(R.layout.add_dialog,null);
                        builder.setView(v1);

                        spinnerArray = new ArrayList<>();
                        spinnerArray.add("휴가");
                        spinnerArray.add("훈련");
                        spinnerArray.add("작업");

                        arrayAdapter = new ArrayAdapter<>(getActivity().getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, spinnerArray);

                        spinner = (Spinner)v1.findViewById(R.id.spinner);
                        spinner.setAdapter(arrayAdapter);

                        final TextView sdate = (TextView)v1.findViewById(R.id.sdateD);
                        final TextView edate = (TextView)v1.findViewById(R.id.edateD);

                        final DatePickerDialog.OnDateSetListener startCallbackMethod;
                        final DatePickerDialog.OnDateSetListener endCallbackMethod;

                        startCallbackMethod = new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                                sdate.setText(i + "-" + String.format("%02d", i1 + 1) + "-" + String.format("%02d", i2));
                            }
                        };

                        endCallbackMethod = new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                                edate.setText(i + "-" + String.format("%02d", i1 + 1) + "-" + String.format("%02d", i2));
                            }
                        };

                        View.OnClickListener dateListener = new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                switch (view.getId()) {
                                    case R.id.sdateD:
                                        try {
                                            Calendar cal = Calendar.getInstance();
                                            DatePickerDialog dialog = new DatePickerDialog(getActivity(), startCallbackMethod, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                                            dialog.show();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        break;
                                    case R.id.edateD:
                                        try {
                                            Calendar cal = Calendar.getInstance();
                                            DatePickerDialog dialog = new DatePickerDialog(getActivity(), endCallbackMethod, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                                            dialog.show();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        break;
                                }
                            }
                        };

                        sdate.setOnClickListener(dateListener);
                        edate.setOnClickListener(dateListener);

                        DialogListener listener = new DialogListener();
                        builder.setPositiveButton("확인", listener);
                        builder.setNegativeButton("취소",null);
                        builder.show();
                        break;
                    case R.id.btnDel:
                        myAdapter.removeItem(token);
                        myAdapter.notifyDataSetChanged();
                        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
                        listview.setLayoutManager(layoutManager);
                        JSONArray jsonArray = getNearSchedule();

                        try {
                            adapter.initArray();
                            if (jsonArray.length() == 0) {
                                Schedule schedule = new Schedule();
                                schedule.schedule_type = -1;
                                schedule.start_date = "null";
                                schedule.end_date = "null";
                                schedule.title = "null";
                                schedule.pk = -1;
                                adapter.addItem(schedule);
                            } else {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    Schedule schedule = new Schedule();
                                    schedule.schedule_type = object.getInt("schedule_type");
                                    schedule.start_date = object.getString("start_date");
                                    schedule.end_date = object.getString("end_date");
                                    schedule.title = object.getString("title");
                                    schedule.pk = object.getInt("id");
                                    adapter.addItem(schedule);
                                }
                            }
                            adapter.notifyDataSetChanged();

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity().getApplicationContext(), "에러 발생", Toast.LENGTH_LONG).show();
                        }
                        break;
                }

            }
        };

        btnAdd.setOnClickListener(listener);
        btnDel.setOnClickListener(listener);

    }
    class DialogListener implements Dialog.OnClickListener{
        @Override
        public void onClick(DialogInterface dialog, int which) {
            //얼럿다이올로그가 가지고 있는 뷰 가져온다
            AlertDialog alert =(AlertDialog)dialog;
            final TextView sdate = (TextView)alert.findViewById(R.id.sdateD);
            final TextView edate = (TextView)alert.findViewById(R.id.edateD);
            EditText plan = (EditText)alert.findViewById(R.id.planD);
            String type = spinner.getSelectedItem().toString();
            int i_type;

            if (type.equals("훈련"))
                i_type = 0;
            else if (type.equals("작업"))
                i_type = 1;
            else if (type.equals("휴가"))
                i_type = 2;
            else
                i_type = 3;
            try {
                JSONObject jsonObject = postSchedule(i_type, plan.getText().toString(), sdate.getText().toString(), edate.getText().toString());
                myAdapter.addItem(jsonObject.getInt("id"), jsonObject.getString("start_date"), jsonObject.getString("end_date"), jsonObject.getString("title"), i_type);
            } catch (Exception e) {
                e.printStackTrace();
            }
            myAdapter.notifyDataSetChanged();//리스너에게 바꼇다고 알람

            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
            listview.setLayoutManager(layoutManager);
            JSONArray jsonArray = getNearSchedule();

            try {
                adapter.initArray();
                if (jsonArray.length() == 0) {
                    Schedule schedule = new Schedule();
                    schedule.schedule_type = -1;
                    schedule.start_date = "null";
                    schedule.end_date = "null";
                    schedule.title = "null";
                    schedule.pk = -1;
                    adapter.addItem(schedule);
                } else {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        Schedule schedule = new Schedule();
                        schedule.schedule_type = object.getInt("schedule_type");
                        schedule.start_date = object.getString("start_date");
                        schedule.end_date = object.getString("end_date");
                        schedule.title = object.getString("title");
                        schedule.pk = object.getInt("id");
                        adapter.addItem(schedule);
                    }
                }
                adapter.notifyDataSetChanged();

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getActivity().getApplicationContext(), "에러 발생", Toast.LENGTH_LONG).show();
            }
        }


    }


    private View.OnClickListener onClickItem = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String str = (String) v.getTag();
            Toast.makeText(getActivity(), str, Toast.LENGTH_SHORT).show();
        }
    };
}