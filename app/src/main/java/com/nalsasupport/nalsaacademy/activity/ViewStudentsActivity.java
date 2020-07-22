package com.nalsasupport.nalsaacademy.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nalsasupport.nalsaacademy.R;
import com.nalsasupport.nalsaacademy.model.Student;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import static com.nalsasupport.nalsaacademy.config.AppUrls.VIEW_STUDENT_URL;

public class ViewStudentsActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<String> nameList;
    private ArrayList<Student> studentArrayList;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_students);
        Toolbar toolbar = findViewById(R.id.toolbar_ViewStudentsActivity);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        listView = findViewById(R.id.listView_ViewStudentsActivity);
        progressDialog = new ProgressDialog(ViewStudentsActivity.this);
        nameList = new ArrayList<>();
        studentArrayList = new ArrayList<>();
        loadVideosList();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(ViewStudentsActivity.this, AddStudentActivity.class);
                i.putExtra("student", studentArrayList.get(position));
                startActivity(i);
                finish();
            }
        });
    }

    private void loadVideosList() {
        progressDialog.show();
        progressDialog.setMessage("Loading...");
        RequestQueue requestQueue = Volley.newRequestQueue(ViewStudentsActivity.this);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                VIEW_STUDENT_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (!jsonObject.getBoolean("error")) {
                                JSONArray dataArray = jsonObject.getJSONArray("data");
                                studentArrayList.clear();
                                nameList.clear();
                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject obj = dataArray.getJSONObject(i);
                                    Student student = new Student();
                                    student.setId(obj.getString("id"));
                                    student.setEmail(obj.getString("email"));
                                    student.setPassword(obj.getString("password"));
                                    student.setName(obj.getString("name"));
                                    student.setStandardId(obj.getString("standard"));
                                    studentArrayList.add(student);
                                    nameList.add(student.getName());
                                    Log.i("CUSTOM", obj.getString("name"));
                                }
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(ViewStudentsActivity.this, android.R.layout.simple_list_item_1, nameList);
                                listView.setAdapter(adapter);
                            } else {
                                Toast.makeText(ViewStudentsActivity.this, "" + jsonObject.getString("message"),
                                        Toast.LENGTH_LONG).show();
                            }
                            progressDialog.dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                            Toast.makeText(ViewStudentsActivity.this, "Server not reachable...\nTry again later!", Toast.LENGTH_LONG).show();
                            Log.i("CUSTOM", "Response Error Listener = " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ViewStudentsActivity.this, "Server not reachable...\nTry again later!", Toast.LENGTH_LONG).show();
                        Log.i("CUSTOM", "Response Error Listener = " + error.getMessage());
                    }
                });

        requestQueue.add(stringRequest);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Log.i("CUSTOM", "onbackpressed()");
        startActivity(new Intent(ViewStudentsActivity.this, HomeAdminActivity.class));
        finish();
        super.onBackPressed();
    }
}
