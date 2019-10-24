package com.sonagi.android.myapplication;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Pair;

import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationManagerCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AlarmService extends JobIntentService {
    private static final int NOTIFICAION_ID = 3;
    private String token;
    @Override
    protected void onHandleWork(Intent intent) {
        System.out.println("Check");
        SharedPreferences sf = getSharedPreferences("auth_token", MODE_PRIVATE);
        token = sf.getString("token", "null");

        class Get extends AsyncTask<String, Void, Pair<Boolean, JSONArray>> {
            public Pair<Boolean, JSONArray> doInBackground(String... strings){
                Pair<Boolean, JSONArray> pair = new Pair<Boolean, JSONArray>(false, new JSONArray());
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
                        pair = new Pair<Boolean, JSONArray>(true, responseJSON);

                        return pair;
                    } else {
                        return pair;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return pair;
                }
            }
        }

        try {
            if (!token.equals("null")) {
                Get get = new Get();
                Pair<Boolean, JSONArray> pair = get.execute("http://13.125.196.191/").get();
                String message = "";

                if (pair.first) {
                    if (pair.second.length() != 0) {
                        for (int i = 0; i < pair.second.length() - 1; i++) {
                            JSONObject object = pair.second.getJSONObject(i);
                            message += object.getString("title") + ", ";
                        }
                        JSONObject object = pair.second.getJSONObject(pair.second.length() - 1);
                        message += object.getString("title");

                        Notification.Builder builder = new Notification.Builder(this);
                        builder.setContentTitle("오늘과 내일의 일정을 확인해 보세요");
                        builder.setContentText(message);
                        builder.setSmallIcon(R.drawable.ic_launcher_foreground);

                        // MainActivity 실행
                        Intent notifyIntent = new Intent(this, MainActivity.class);
                        PendingIntent pendingIntent = PendingIntent.getActivity(this, 2, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        builder.setContentIntent(pendingIntent);
                        Notification notificationCompat = builder.build();
                        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
                        managerCompat.notify(NOTIFICAION_ID, notificationCompat);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
