package com.example.ar;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.TreeSet;

public class BookMarkList extends AppCompatActivity {

    MainActivity mainActivity;
    BackgroundWorker backgroundWorker;
    static ArrayList<String> list = new ArrayList<String>();
    static ArrayList<String> list3 = new ArrayList<String>();
    static String getListViewString;
    static int ListView = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_mark_list);

        list.clear();
        String str = backgroundWorker.markList;

        String[] array = str.split(":");

        for(int i=1 ; i< Integer.parseInt(array[0])+1; i++){
            list.add(array[i]);
        }
        TreeSet<String> list2 = new TreeSet<String>(list); //TreeSet에 list데이터 삽입
        list3 = new ArrayList<String>(list2); //중복이 제거된 HachSet을 다시 ArrayList에 삽입


        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list3) ;

        ListView listview = (ListView) findViewById(R.id.listview1) ;
        listview.setAdapter(adapter) ;

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {

                // get TextView's Text.
                getListViewString = (String) parent.getItemAtPosition(position) ;
                ListView =1;

                mainActivity.STT.setText(mainActivity.bookMarkList.getListViewString);
                mainActivity.txtView.setText(mainActivity.bookMarkList.getListViewString);
                mainActivity.a_d.setText("즐겨찾기 삭제");
                mainActivity.buttonState = "delete";
                mainActivity.bookMarkList.ListView = 0;
                finish();
            }
        }) ;
    }
}