package com.nalsasupport.nalsaacademy.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nalsasupport.nalsaacademy.R;
import com.nalsasupport.nalsaacademy.model.Standard;
import com.nalsasupport.nalsaacademy.model.Student;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.nalsasupport.nalsaacademy.config.AppUrls.ADD_STUDENT_URL;
import static com.nalsasupport.nalsaacademy.config.AppUrls.DELETE_STUDENT_URL;
import static com.nalsasupport.nalsaacademy.config.AppUrls.EDIT_STUDENT_URL;
import static com.nalsasupport.nalsaacademy.config.AppUrls.STANDARD_URL;

public class AddStudentActivity extends AppCompatActivity {

    private EditText emailED, passwordED, nameED;
    private Button addBT, delBT;
    private Spinner spinner;
    private String emailD, passwordD, nameD, standardIdD;
    private Student student;
    private ArrayList<Standard> standardArrayList;
    private ArrayList<String> nameArrayList;
    private ProgressDialog progressDialog;
    private boolean edit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);
        Toolbar toolbar = findViewById(R.id.toolbar_AddStudentActivity);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        emailED = findViewById(R.id.editText_email_AddStudentActivity);
        passwordED = findViewById(R.id.editText_password_AddStudentActivity);
        nameED = findViewById(R.id.editText_name_AddStudentActivity);
        addBT = findViewById(R.id.button_add_AddStudentActivity);
        delBT = findViewById(R.id.button_delete_AddStudentActivity);
        spinner = findViewById(R.id.spinner_standard_AddStudentActivity);

        standardArrayList = new ArrayList<>();
        nameArrayList = new ArrayList<>();
        progressDialog = new ProgressDialog(AddStudentActivity.this);

        student = (Student) getIntent().getSerializableExtra("student");
        if (student != null) {
            edit = true;
            nameED.setText(student.getName());
            emailED.setText(student.getEmail());
            passwordED.setText(student.getPassword());
            addBT.setText("Edit");
            delBT.setVisibility(View.VISIBLE);
            getSupportActionBar().setTitle("View/Edit Student");
        }
        loadSpinner();

        addBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    if (edit) {
                        uploadData(EDIT_STUDENT_URL);
                    } else {
                        uploadData(ADD_STUDENT_URL);
                    }
                }
            }
        });

        delBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
    }

    private void loadSpinner() {
        progressDialog.show();
        progressDialog.setMessage("Loading...");
        RequestQueue requestQueue = Volley.newRequestQueue(AddStudentActivity.this);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                STANDARD_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (!jsonObject.getBoolean("error")) {
                                Log.i("CUSTOM", "loadlistview() - success");
                                JSONArray dataArray = jsonObject.getJSONArray("data");
                                standardArrayList.clear();
                                nameArrayList.clear();
                                nameArrayList.add("Select Standard :");
                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject obj = dataArray.getJSONObject(i);
                                    Standard standard = new Standard();
                                    standard.setId(obj.getString("id"));
                                    standard.setName(obj.getString("name"));
                                    standardArrayList.add(standard);
                                    nameArrayList.add(standard.getName());
                                    Log.i("CUSTOM", obj.getString("name"));
                                }
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(AddStudentActivity.this, android.R.layout.simple_list_item_1, nameArrayList);
                                spinner.setAdapter(adapter);

                                if (edit) {
                                    Log.i("CUSTOM", "standard edit = " + student.getStandardId());
                                    boolean found = false;
                                    for (Standard s : standardArrayList) {
                                        Log.i("CUSTOM", "s = " + s.getId());
                                        if (s.getId().equals(student.getStandardId())) {
                                            spinner.setSelection(nameArrayList.indexOf(s.getName()));
                                            found = true;
                                            Log.i("CUSTOM", "found at index = " + nameArrayList.indexOf(s.getName()));
                                        }
                                    }
                                    if (!found) {
                                        showStandardDialog();
                                    }
                                }
                            } else {
                                Toast.makeText(AddStudentActivity.this, "" + jsonObject.getString("message"),
                                        Toast.LENGTH_LONG).show();
                            }
                            progressDialog.dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                            Toast.makeText(AddStudentActivity.this, "Server not reachable...\nTry again later!", Toast.LENGTH_LONG).show();
                            Log.i("CUSTOM", "Response Error Listener = " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(AddStudentActivity.this, "Server not reachable...\nTry again later!", Toast.LENGTH_LONG).show();
                        Log.i("CUSTOM", "Response Error Listener = " + error.getMessage());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("op", "2");
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete " + student.getName() + "?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        deleteData();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showStandardDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Important");
        String msg = student.getName() + "'s Standard set earlier has been deleted. Please set a new Standard and Save.";
        builder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void deleteData() {
        progressDialog.show();
        progressDialog.setMessage("Loading...");
        RequestQueue requestQueue = Volley.newRequestQueue(AddStudentActivity.this);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                DELETE_STUDENT_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.i("CUSTOM", "response = " + response);
                            JSONObject jsonObject = new JSONObject(response);
                            if (!jsonObject.getBoolean("error")) {
                                progressDialog.dismiss();
                                Toast.makeText(AddStudentActivity.this, "Student deleted Successfully!",
                                        Toast.LENGTH_LONG).show();
                                onBackPressed();
                            } else {
                                Toast.makeText(AddStudentActivity.this, "" + jsonObject.getString("message"),
                                        Toast.LENGTH_LONG).show();
                            }
                            progressDialog.dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                            Log.i("CUSTOM", "Response Error Listener = " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(AddStudentActivity.this, "Server not reachable...\nTry again later!", Toast.LENGTH_LONG).show();
                        Log.i("CUSTOM", "Response Error Listener = " + error.getCause().getMessage());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id", student.getId());
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void uploadData(String URL) {
        progressDialog.show();
        progressDialog.setMessage("Loading...");
        RequestQueue requestQueue = Volley.newRequestQueue(AddStudentActivity.this);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (!jsonObject.getBoolean("error")) {
                                if (edit) {
                                    Toast.makeText(AddStudentActivity.this, "Student edited Successfully!",
                                            Toast.LENGTH_LONG).show();
                                    student.setEmail(emailD);
                                    student.setName(nameD);
                                    student.setPassword(passwordD);
                                    student.setStandardId(standardIdD);
                                } else {
                                    Toast.makeText(AddStudentActivity.this,
                                            "Student added Successfully!", Toast.LENGTH_LONG).show();
                                    clearData();
                                }
                            } else {
                                Toast.makeText(AddStudentActivity.this, "" + jsonObject.getString("message"),
                                        Toast.LENGTH_LONG).show();
                            }
                            progressDialog.dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                            Log.i("CUSTOM", "Response Error Listener = " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(AddStudentActivity.this, "Server not reachable...\nTry again later!", Toast.LENGTH_LONG).show();
                        Log.i("CUSTOM", "Response Error Listener = " + error.getMessage());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                if (edit) {
                    params.put("id", student.getId());
                }
                params.put("name", nameD);
                params.put("email", emailD);
                params.put("password", passwordD);
                params.put("standard", standardIdD);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void clearData() {
        nameED.setText("");
        emailED.setText("");
        passwordED.setText("");
        spinner.setSelection(0);
    }

    private boolean validate() {
        if (nameED.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Enter Name", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (emailED.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Enter Email", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (passwordED.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (spinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Select Standard", Toast.LENGTH_LONG).show();
            return false;
        }

        nameD = nameED.getText().toString().trim();
        emailD = emailED.getText().toString().trim();
        passwordD = passwordED.getText().toString().trim();
        standardIdD = standardArrayList.get(spinner.getSelectedItemPosition() - 1).getId();
        return true;
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
        if (edit) {
            startActivity(new Intent(AddStudentActivity.this, ViewStudentsActivity.class));
        } else {
            startActivity(new Intent(AddStudentActivity.this, HomeAdminActivity.class));
        }
        finish();
        super.onBackPressed();
    }
}
