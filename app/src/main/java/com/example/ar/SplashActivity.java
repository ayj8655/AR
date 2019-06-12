package com.example.ar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

//로딩
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     //   setContentView(R.layout.activity_splash);
        try {
            Thread.sleep(1000); //대기 시간 설정
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    //1000 대기 후 로그인 액티비티 실행
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
