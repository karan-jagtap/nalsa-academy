package com.nalsasupport.nalsaacademy.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.nalsasupport.nalsaacademy.R;
import com.nalsasupport.nalsaacademy.helper.SessionManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HomeAdminActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<String> arrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_admin);
        Toolbar toolbar = findViewById(R.id.toolbar_HomeAdminActivity);
        setSupportActionBar(toolbar);


        listView = findViewById(R.id.listView_HomeAdminActivity);
        arrayList = new ArrayList<>();
        arrayList.add("Add Video");
        arrayList.add("View/Edit Videos");
        arrayList.add("Create Playlist");
        arrayList.add("View/Edit Playlist");
        arrayList.add("Add Student");
        arrayList.add("View/Edit Students");
        arrayList.add("Standard");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(HomeAdminActivity.this, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        startActivity(new Intent(HomeAdminActivity.this, AddVideosActivity.class));
                        finish();
                        break;
                    case 1:
                        startActivity(new Intent(HomeAdminActivity.this, ViewVideosActivity.class));
                        finish();
                        break;
                    case 2:
                        startActivity(new Intent(HomeAdminActivity.this, AddPlaylistActivity.class));
                        finish();
                        break;
                    case 3:
                        startActivity(new Intent(HomeAdminActivity.this, ViewPlaylistsActivity.class));
                        finish();
                        break;
                    case 4:
                        startActivity(new Intent(HomeAdminActivity.this, AddStudentActivity.class));
                        finish();
                        break;
                    case 5:
                        startActivity(new Intent(HomeAdminActivity.this, ViewStudentsActivity.class));
                        finish();
                        break;
                    case 6:
                        startActivity(new Intent(HomeAdminActivity.this, StandardActivity.class));
                        break;
                }
            }
        });
    }

    private void checkStatus() {
        Calendar calendar = Calendar.getInstance();
        String todayDate = calendar.get(Calendar.DAY_OF_MONTH) + "/" +
                (calendar.get(Calendar.MONTH) + 1) + "/" +
                calendar.get(Calendar.YEAR);
        Date date = null;
        Date today = null;
        try {
            date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse("06/07/2020");
            today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(todayDate);
            Log.i("ABC", "Today = " + today.toString() + " date = " + date.toString());
        } catch (ParseException e) {
            Log.i("ABC", "Dialog util exception = " + e.getMessage());
        }

        if (today.compareTo(date) > 0) {
            HomeAdminActivity.this.finish();
            //return true;
        } else if (today.compareTo(date) < 0) {
            //return false;
        }
        // return true if today;s date is allowed
        // else return false
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_admin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            SessionManager sm = new SessionManager(HomeAdminActivity.this);
            sm.setLogin(false);
            sm.setAsAdmin(false);
            startActivity(new Intent(HomeAdminActivity.this, DeciderActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
