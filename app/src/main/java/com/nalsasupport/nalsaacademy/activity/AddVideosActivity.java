package com.nalsasupport.nalsaacademy.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.nalsasupport.nalsaacademy.model.Videos;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.nalsasupport.nalsaacademy.config.AppUrls.ADD_VIDEO_URL;
import static com.nalsasupport.nalsaacademy.config.AppUrls.DELETE_VIDEO_URL;
import static com.nalsasupport.nalsaacademy.config.AppUrls.EDIT_VIDEO_URL;

public class AddVideosActivity extends AppCompatActivity {

    private EditText titleED, descED, videoIdED;
    private Button addBT, delBT;
    private Videos video;
    private ProgressDialog progressDialog;
    private boolean edit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_videos);
        Toolbar toolbar = findViewById(R.id.toolbar_AddVideosActivity);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        titleED = findViewById(R.id.editText_title_AddVideosActivity);
        descED = findViewById(R.id.editText_desc_AddVideosActivity);
        videoIdED = findViewById(R.id.editText_videoId_AddVideosActivity);
        addBT = findViewById(R.id.button_add_AddVideosActivity);
        delBT = findViewById(R.id.button_delete_AddVideoActivity);
        progressDialog = new ProgressDialog(AddVideosActivity.this);

        video = (Videos) getIntent().getSerializableExtra("video");
        if (video != null) {
            Log.i("CUSTOM", "Video not null");
            edit = true;
            titleED.setText(video.getTitle());
            descED.setText(video.getDescription());
            videoIdED.setText(video.getVideoId());
            Log.i("CUSTOM", "id = " + video.getId());
            addBT.setText("Edit");
            delBT.setVisibility(View.VISIBLE);
            getSupportActionBar().setTitle("View/Edit Video");
        } else {
            Log.i("CUSTOM", "Video null");
        }


        addBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    if (edit) {
                        uploadData(EDIT_VIDEO_URL);
                    } else {
                        uploadData(ADD_VIDEO_URL);
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

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete " + video.getTitle() + "?")
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
        RequestQueue requestQueue = Volley.newRequestQueue(AddVideosActivity.this);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                DELETE_VIDEO_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.i("CUSTOM", "response = " + response);
                            JSONObject jsonObject = new JSONObject(response);
                            if (!jsonObject.getBoolean("error")) {
                                progressDialog.dismiss();
                                Toast.makeText(AddVideosActivity.this, "Video deleted Successfully!",
                                        Toast.LENGTH_LONG).show();
                                onBackPressed();
                            } else {
                                Toast.makeText(AddVideosActivity.this, "" + jsonObject.getString("message"),
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
                        Toast.makeText(AddVideosActivity.this, "Server not reachable...\nTry again later!", Toast.LENGTH_LONG).show();
                        Log.i("CUSTOM", "Response Error Listener = " + error.getCause().getMessage());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id", video.getId());
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }


    private void uploadData(String URL) {
        progressDialog.show();
        progressDialog.setMessage("Loading...");
        RequestQueue requestQueue = Volley.newRequestQueue(AddVideosActivity.this);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.i("CUSTOM", "response = " + response);
                            JSONObject jsonObject = new JSONObject(response);
                            if (!jsonObject.getBoolean("error")) {
                                if (edit) {
                                    Toast.makeText(AddVideosActivity.this, "Video edited Successfully!",
                                            Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(AddVideosActivity.this,
                                            "Video added Successfully!", Toast.LENGTH_LONG).show();
                                    clearData();
                                }
                            } else {
                                Toast.makeText(AddVideosActivity.this, "" + jsonObject.getString("message"),
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
                        Toast.makeText(AddVideosActivity.this, "Server not reachable...\nTry again later!", Toast.LENGTH_LONG).show();
                        Log.i("CUSTOM", "Response Error Listener = " + error.getCause().getMessage());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                if (edit) {
                    Log.i("CUSTOM", "sending id = "+video.getId());
                    params.put("id", video.getId());
                }
                params.put("title", video.getTitle());
                params.put("description", video.getDescription());
                params.put("video_id", video.getVideoId());
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void clearData() {
        titleED.setText("");
        descED.setText("");
        videoIdED.setText("");
    }

    private boolean validate() {

        if (titleED.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Enter Title", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (descED.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Enter Description", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (videoIdED.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Enter Video Id", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!edit) {
            video = new Videos();
        }
        video.setTitle(titleED.getText().toString().trim());
        video.setDescription(descED.getText().toString().trim());
        video.setVideoId(videoIdED.getText().toString().trim());
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
            startActivity(new Intent(AddVideosActivity.this, ViewVideosActivity.class));
        } else {
            startActivity(new Intent(AddVideosActivity.this, HomeAdminActivity.class));
        }
        finish();
        super.onBackPressed();
    }
}
