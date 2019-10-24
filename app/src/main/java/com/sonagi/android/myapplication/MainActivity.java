package com.sonagi.android.myapplication;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    //프레그 먼트 객체 생성
    SecondFragment first = new SecondFragment();
    FirstFragment second =new FirstFragment();
    ThirdFragment third=new ThirdFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        container_init();// 첫 화면 띄워주기 벡스택 없음

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //프래그 먼트를 관리하는 객체 추출
                FragmentManager manager = getSupportFragmentManager();
                // 프로그먼트 변경을 관리하는 객체를 추출
                FragmentTransaction tran= manager.beginTransaction();
                switch (item.getItemId()) {
                    case R.id.action_recents:
                        tran.replace(R.id.container,second);
                        tran.commit();
                        break;
                    case R.id.action_favorites:;
                        tran.replace(R.id.container,first);
                        tran.commit();
                        break;
                    case R.id.action_plus:
                        tran.replace(R.id.container,third);
                        tran.commit();
                        break;
                }
                return true;
            }
        });
    }
    public void container_init(){ // 처음 디폴트 값에 첫 화면 띄워주기
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction tran= manager.beginTransaction();
        tran.add(R.id.container,first);
        tran.commit();
    }
}