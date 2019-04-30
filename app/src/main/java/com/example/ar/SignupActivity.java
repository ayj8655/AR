package com.example.ar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

//회원가입
public class SignupActivity extends AppCompatActivity {

    EditText id, password, password2, name, email, phone;
    CheckBox agree;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        id = (EditText)findViewById(R.id.et_id);
        password = (EditText)findViewById(R.id.et_password);
        password2 = (EditText)findViewById(R.id.et_password2);
        name = (EditText)findViewById(R.id.et_name);
        email = (EditText)findViewById(R.id.et_email);
        phone = (EditText)findViewById(R.id.et_phone);
        agree = (CheckBox)findViewById(R.id.checkBox);
    }

    public void OnReg(View view) {
        String str_id = id.getText().toString();
        String str_password = password.getText().toString();
        String str_password2 = password2.getText().toString();
        String str_name = name.getText().toString();
        String str_email = email.getText().toString();
        String str_phone = phone.getText().toString();
        String str_agree;

        if(agree.isChecked()){
            str_agree = "yes";
        }
        else{
            str_agree = "no";
        }

        String type = "register";
        BackgroundWorker backgroundWorker = new BackgroundWorker(this);
        backgroundWorker.execute(type, str_id, str_password, str_password2, str_name, str_email, str_phone, str_agree);

    }

}
