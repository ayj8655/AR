package com.example.ar;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

//로딩
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     //   setContentView(R.layout.activity_splash);
        try {
            Thread.sleep(4000); //대기 시간 설정
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    //4000 대기 후 로그인 액티비티 실행
        startActivity(new Intent(this,LoginActivity.class));
        finish();
    }
}