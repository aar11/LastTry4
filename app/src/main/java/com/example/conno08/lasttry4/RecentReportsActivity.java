package com.example.conno08.lasttry4;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by conno08 on 22/04/2015.
 */
public class RecentReportsActivity extends Activity
{
    private Button backBtn;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reports_activity);
        ListView listView1 = (ListView) findViewById(R.id.listView1);

        String[] items = { "Mayberry Road Dublin", "Main Street Tallaght", "Westpark Tallaght", "Belgard Road Tallaght", "Greenhills Road Tallaght", "Beachhill Road", "Redwood Walk", "Parkhill Lawns", "TreePark Road", "Michael Collins Park" };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, items);
        listView1.setAdapter(adapter);

        backBtn = (Button) findViewById(R.id.back);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecentReportsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

}
