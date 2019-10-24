package com.sonagi.android.myapplication.row;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sonagi.android.myapplication.R;
import com.sonagi.android.myapplication.SecondFragment;
import com.sonagi.android.myapplication.today_tab.CheckSchedule;
import com.sonagi.android.myapplication.today_tab.Schedule;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class MyAdapter extends BaseAdapter {

    /* 아이템을 세트로 담기 위한 어레이 */
    private ArrayList<CheckSchedule> mItems;


    public void initArray() {
        mItems = new ArrayList<>();
    }
    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public CheckSchedule getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Context context = parent.getContext();

        /* 'listview_custom' Layout을 inflate하여 convertView 참조 획득 */
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row_chkbox, parent, false);
        }

        /* 'listview_custom'에 정의된 위젯에 대한 참조 획득 */
        TextView title = (TextView) convertView.findViewById(R.id.title) ;
        TextView content = (TextView) convertView.findViewById(R.id.date_content) ;
        CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);;
        ImageView imageView = (ImageView) convertView.findViewById(R.id.icon);

        /* 각 리스트에 뿌려줄 아이템을 받아오는데 mMyItem 재활용 */
        final CheckSchedule myItem = getItem(position);

        /* 각 위젯에 세팅된 아이템을 뿌려준다 */
        title.setText(myItem.title);
        content.setText(myItem.start_date + "~" + myItem.end_date);
        checkBox.setChecked(myItem.check);
        switch (myItem.schedule_type) {
            case 0:
                imageView.setImageResource(R.drawable.flag);
                break;
            case 1:
                imageView.setImageResource(R.drawable.shovel);
                break;
            case 2:
                imageView.setImageResource(R.drawable.holiday);
                break;
        }

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                myItem.check = isChecked;
            }
        });

        /* (위젯에 대한 이벤트리스너를 지정하고 싶다면 여기에 작성하면된다..)  */


        return convertView;
    }

    public void removeItem(String token){
        ArrayList<CheckSchedule> remove_object = new ArrayList<CheckSchedule>();
        boolean check;
        for(CheckSchedule item : mItems){
            if (item.check) {
                remove_object.add(item);
            }
        }

        for(CheckSchedule item : remove_object){
            mItems.remove(item);

            class Delete extends AsyncTask<String, Void, Boolean> {
                public Boolean doInBackground(String... strings) {
                    try {
                        URL url = new URL(strings[0]);
                        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                        httpURLConnection.setReadTimeout(3000);
                        httpURLConnection.setConnectTimeout(3000);
                        httpURLConnection.setDoOutput(true);
                        httpURLConnection.setDoInput(true);
                        httpURLConnection.setRequestProperty("Authorization", "jwt " + strings[1]);
                        httpURLConnection.setRequestProperty("Content-Type","application/json");
                        httpURLConnection.setRequestProperty("Accept","application/json");
                        httpURLConnection.setRequestMethod("DELETE");
                        httpURLConnection.setUseCaches(false);

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
                Delete delete = new Delete();
                check = delete.execute("http://13.125.196.191/schedule/" + Integer.toString(item.pk) +"/", token).get();
            } catch (Exception e) {
                e.printStackTrace();
                check = false;
            }
        }
    }

    /* 아이템 데이터 추가를 위한 함수. 자신이 원하는대로 작성 */
    public void addItem(int pk, String start_date, String end_date, String content, int type) {
        CheckSchedule mItem = new CheckSchedule();
        mItem.pk = pk;
        mItem.start_date = start_date;
        mItem.end_date = end_date;
        mItem.title = content;
        mItem.schedule_type = type;

        mItems.add(mItem);
    }
}