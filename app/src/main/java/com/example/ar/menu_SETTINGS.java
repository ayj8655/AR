package com.example.ar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.intellij.lang.annotations.Language;

public class menu_SETTINGS extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu__settings);

        TextView.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.txt_language:
                        Intent intent = new Intent(menu_SETTINGS.this, Language_popUp.class);
                        startActivityForResult(intent, 1);

                        break;
                }
            }
        };

        TextView language = (TextView)findViewById(R.id.txt_language);
        language.setOnClickListener(clickListener);
    }


}