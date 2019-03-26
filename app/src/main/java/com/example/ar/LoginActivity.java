package com.example.ar;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;


//로그인
public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }


    public void Login_onClick(View v){  //온클릭으로 인텐트
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    public void Signup_onClick(View v){
        Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
        startActivity(intent);
    }


}
