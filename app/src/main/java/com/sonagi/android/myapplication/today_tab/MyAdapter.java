package com.sonagi.android.myapplication.today_tab;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.sonagi.android.myapplication.R;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private ArrayList<Schedule> itemList;
    private Context context;
    private View.OnClickListener onClickItem;

    public MyAdapter(Context context, ArrayList<Schedule> itemList, View.OnClickListener onClickItem) {
        this.context = context;
        this.itemList = itemList;
        this.onClickItem = onClickItem;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // context 와 parent.getContext() 는 같다.
        View view = LayoutInflater.from(context)
                .inflate(R.layout.today_tv, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        int pk = itemList.get(position).pk;
        String title = itemList.get(position).title;
        String start_date = itemList.get(position).start_date;
        String end_date = itemList.get(position).end_date;

        if (pk == -1) {
            holder.titleView.setText("등록된 일정이 없습니다.");
            holder.titleView.setTag("none_title");
            holder.dateContentView.setText("일정을 등록 해 주세요!");
            holder.dateContentView.setTag("none_date");
        } else {
            holder.titleView.setText(title);
            holder.titleView.setTag(title);
            holder.dateContentView.setText(start_date + "~" + end_date);
            holder.dateContentView.setTag(start_date + "~" + end_date);
        }

        switch (itemList.get(position).schedule_type) {
            case 0:
                holder.imageView.setImageResource(R.drawable.flag);
                break;
            case 1:
                holder.imageView.setImageResource(R.drawable.shovel);
                break;
            case 2:
                holder.imageView.setImageResource(R.drawable.holiday);
                break;
        }

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView titleView;
        public TextView dateContentView;
        public ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(onClickItem);

            titleView = itemView.findViewById(R.id.title);
            dateContentView = itemView.findViewById(R.id.date_content);
            imageView = itemView.findViewById(R.id.image);
        }
    }

    public void initArray() {
        itemList = new ArrayList<>();
    }

    public void addItem(Schedule schedule) {
        itemList.add(schedule);
    }
}