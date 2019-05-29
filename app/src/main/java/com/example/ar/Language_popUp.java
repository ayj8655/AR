package com.example.ar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Language_popUp extends AppCompatActivity {

    Button settingBtn, cancleBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_pop_up);

        settingBtn = (Button)findViewById(R.id.btn_setting);
        cancleBtn = (Button)findViewById(R.id.btn_cancle);
    }

    public void settingClick(View view) {
        finish();
    }

    public void cancleClick(View view) {
        finish();
    }
}