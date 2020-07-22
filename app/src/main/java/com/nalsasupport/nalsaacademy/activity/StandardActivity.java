package com.nalsasupport.nalsaacademy.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.nalsasupport.nalsaacademy.config.AppUrls.STANDARD_URL;

public class StandardActivity extends AppCompatActivity {

    private EditText standardED;
    private Button addBT;
    private ListView listView;
    private TextView msgTV;
    private ProgressDialog progressDialog;
    private boolean edit = false;
    private Standard standard;
    private ArrayList<Standard> standardArrayList;
    private ArrayList<String> nameArrayList;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standard);
        Toolbar toolbar = findViewById(R.id.toolbar_StandardActivity);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        standardED = findViewById(R.id.editText_standard_StandardActivity);
        addBT = findViewById(R.id.button_add_StandardActivity);
        listView = findViewById(R.id.listView_StandardActivity);
        msgTV = findViewById(R.id.textView_msg_StandardActivity);

        standardArrayList = new ArrayList<>();
        nameArrayList = new ArrayList<>();
        progressDialog = new ProgressDialog(StandardActivity.this);
        requestQueue = Volley.newRequestQueue(StandardActivity.this);

        loadListView();

        addBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!standardED.getText().toString().trim().isEmpty()) {
                    if (!edit) {
                        standard = new Standard();
                    }
                    standard.setName(standardED.getText().toString().trim());
                    if (nameArrayList.isEmpty() || !nameArrayList.contains(standard.getName())) {
                        uploadData();
                    } else {
                        Toast.makeText(StandardActivity.this,
                                "Duplicate Standard not allowed.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(StandardActivity.this,
                            "Enter Standard", Toast.LENGTH_LONG).show();
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                standard = standardArrayList.get(position);
                standardED.setText(standard.getName());
                edit = true;
                addBT.setText("Edit");
                Toast.makeText(StandardActivity.this,
                        "Long press to delete", Toast.LENGTH_LONG).show();
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                standard = standardArrayList.get(position);
                edit = false;
                addBT.setText("Add");
                standardED.setText("");
                showDialog();
                return true;
            }
        });
    }

    private void uploadData() {
        progressDialog.show();
        progressDialog.setMessage("Loading...");
        //RequestQueue requestQueue = Volley.newRequestQueue(StandardActivity.this);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST, STANDARD_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (!jsonObject.getBoolean("error")) {
                                if (edit) {
                                    Toast.makeText(StandardActivity.this, "Student edited Successfully!",
                                            Toast.LENGTH_LONG).show();
                                    //loadListView();
                                    standardED.setText("");
                                    edit = false;
                                    addBT.setText("Add");
                                    loadListView();
                                } else {
                                    Toast.makeText(StandardActivity.this,
                                            "Standard added Successfully!", Toast.LENGTH_LONG).show();
                                    standardED.setText("");
                                    loadListView();
                                }
                            } else {
                                Toast.makeText(StandardActivity.this, "" + jsonObject.getString("message"),
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
                        Toast.makeText(StandardActivity.this, "Server not reachable...\nTry again later!", Toast.LENGTH_LONG).show();
                        Log.i("CUSTOM", "Response Error Listener = " + error.getMessage());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                if (edit) {
                    params.put("id", standard.getId());
                    params.put("op", "1");
                } else {
                    params.put("op", "0");
                }
                params.put("name", standard.getName());
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void loadListView() {
        Log.i("CUSTOM", "loadlistview()");
        progressDialog.show();
        progressDialog.setMessage("Loading...");
        //RequestQueue requestQueue = Volley.newRequestQueue(StandardActivity.this);
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
                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject obj = dataArray.getJSONObject(i);
                                    Standard standard = new Standard();
                                    standard.setId(obj.getString("id"));
                                    standard.setName(obj.getString("name"));
                                    standardArrayList.add(standard);
                                    nameArrayList.add(standard.getName());
                                    Log.i("CUSTOM", obj.getString("name"));
                                }
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(StandardActivity.this, android.R.layout.simple_list_item_1, nameArrayList);
                                listView.setAdapter(adapter);
                                if (nameArrayList.isEmpty()) {
                                    msgTV.setVisibility(View.VISIBLE);
                                    msgTV.setText("No Data Found.");
                                } else {
                                    msgTV.setVisibility(View.GONE);
                                }
                                for (String n : nameArrayList) {
                                    Log.i("CUSTOM", "loadlistview() - success - " + n);
                                }
                            } else {
                                Toast.makeText(StandardActivity.this, "" + jsonObject.getString("message"),
                                        Toast.LENGTH_LONG).show();
                            }
                            progressDialog.dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                            Toast.makeText(StandardActivity.this, "Server not reachable...\nTry again later!", Toast.LENGTH_LONG).show();
                            Log.i("CUSTOM", "Response Error Listener = " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(StandardActivity.this, "Server not reachable...\nTry again later!", Toast.LENGTH_LONG).show();
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
        builder.setMessage("Are you sure you want to delete " + standard.getName() + "?")
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

    private void deleteData() {
        progressDialog.show();
        progressDialog.setMessage("Loading...");
        //RequestQueue requestQueue = Volley.newRequestQueue(StandardActivity.this);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                STANDARD_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.i("CUSTOM", "response = " + response);
                            JSONObject jsonObject = new JSONObject(response);
                            if (!jsonObject.getBoolean("error")) {
                                progressDialog.dismiss();
                                Toast.makeText(StandardActivity.this, "Standard deleted Successfully!",
                                        Toast.LENGTH_LONG).show();
                                //onBackPressed();
                                loadListView();
                            } else {
                                Toast.makeText(StandardActivity.this, "" + jsonObject.getString("message"),
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
                        Toast.makeText(StandardActivity.this, "Server not reachable...\nTry again later!", Toast.LENGTH_LONG).show();
                        Log.i("CUSTOM", "Response Error Listener = " + error.getCause().getMessage());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("op", "3");
                params.put("id", standard.getId());
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_info, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_reset) {
            loadListView();
            standardED.setText("");
            standard = new Standard();
            addBT.setText("Add");
            edit = false;
        }
        return super.onOptionsItemSelected(item);
    }*/
}