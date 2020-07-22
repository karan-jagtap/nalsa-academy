package com.nalsasupport.nalsaacademy.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
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
import com.nalsasupport.nalsaacademy.model.Playlist;
import com.nalsasupport.nalsaacademy.model.Standard;
import com.nalsasupport.nalsaacademy.model.Videos;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.nalsasupport.nalsaacademy.config.AppUrls.ADD_PLAYLIST_URL;
import static com.nalsasupport.nalsaacademy.config.AppUrls.DELETE_PLAYLIST_URL;
import static com.nalsasupport.nalsaacademy.config.AppUrls.EDIT_PLAYLIST_URL;
import static com.nalsasupport.nalsaacademy.config.AppUrls.STANDARD_URL;
import static com.nalsasupport.nalsaacademy.config.AppUrls.VIEW_VIDEO_URL;

public class AddPlaylistActivity extends AppCompatActivity {

    private EditText titleED, descED;
    private Spinner spinner, standardSpinner;
    private TextView selectedTitlesTV, clearTV;
    private Button addBT, delBT;
    private Playlist playlist;
    private ProgressDialog progressDialog;
    private ArrayList<String> videoIdList, videoTitleList, displayTitleList, nameArrayList;
    private ArrayList<Videos> videosList;
    private ArrayList<Standard> standardArrayList;
    private boolean edit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_playlist);
        Toolbar toolbar = findViewById(R.id.toolbar_AddPlaylistActivity);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        titleED = findViewById(R.id.editText_title_AddPlaylistActivity);
        descED = findViewById(R.id.editText_desc_AddPlaylistActivity);
        addBT = findViewById(R.id.button_add_AddPlaylistActivity);
        spinner = findViewById(R.id.spinner_video_titles_AddPlaylistActivity);
        standardSpinner = findViewById(R.id.spinner_standard_AddPlaylistActivity);
        selectedTitlesTV = findViewById(R.id.textView_selected_AddPlaylistActivity);
        clearTV = findViewById(R.id.textView_clear_AddPlaylistActivity);
        delBT = findViewById(R.id.button_delete_AddPlaylistActivity);
        progressDialog = new ProgressDialog(AddPlaylistActivity.this);
        videoIdList = new ArrayList<>();
        videoTitleList = new ArrayList<>();
        videosList = new ArrayList<>();
        displayTitleList = new ArrayList<>();
        standardArrayList = new ArrayList<>();
        nameArrayList = new ArrayList<>();

        playlist = (Playlist) getIntent().getSerializableExtra("playlist");
        if (playlist != null) {
            Log.i("CUSTOM", "Video not null");
            edit = true;
            titleED.setText(playlist.getTitle());
            descED.setText(playlist.getDescription());
            Collections.addAll(videoIdList, playlist.getVideoIds().split(","));
            addBT.setText("Edit");
            delBT.setVisibility(View.VISIBLE);
            getSupportActionBar().setTitle("View/Edit Playlist");
        }

        loadData();
        loadSpinner();


        addBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    if (edit) {
                        uploadData(EDIT_PLAYLIST_URL);
                    } else {
                        uploadData(ADD_PLAYLIST_URL);
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

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    Log.i("CUSTOM", "position= " + position + "\ntitle = " + videoTitleList.get(position) + "\nid = " + videosList.get(position - 1).getId());
                    if (!videoIdList.contains(videosList.get(position - 1).getId())) {
                        videoIdList.add(videosList.get(position - 1).getId());
                        displayTitleList.add(videoTitleList.get(position));
                        setTextViewOfTitles();
                    } else {
                        Log.i("CUSTOM", "else");
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        clearTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoIdList.clear();
                displayTitleList.clear();
                selectedTitlesTV.setText("No Video Selected.");
                spinner.setSelection(0);
                standardSpinner.setSelection(0);
            }
        });
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete " + playlist.getTitle() + "?")
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

    private void loadSpinner() {
        progressDialog.show();
        progressDialog.setMessage("Loading...");
        RequestQueue requestQueue = Volley.newRequestQueue(AddPlaylistActivity.this);
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
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(AddPlaylistActivity.this, android.R.layout.simple_list_item_1, nameArrayList);
                                standardSpinner.setAdapter(adapter);

                                if (edit) {
                                    Log.i("CUSTOM", "standard edit = " + playlist.getStandardId());
                                    boolean found = false;
                                    for (Standard s : standardArrayList) {
                                        Log.i("CUSTOM", "s = " + s.getId());
                                        if (s.getId().equals(playlist.getStandardId())) {
                                            standardSpinner.setSelection(nameArrayList.indexOf(s.getName()));
                                            found = true;
                                            Log.i("CUSTOM", "found at index = " + nameArrayList.indexOf(s.getName()));
                                        }
                                    }
                                    if (!found) {
                                        showStandardDialog();
                                    }
                                }
                            } else {
                                Toast.makeText(AddPlaylistActivity.this, "" + jsonObject.getString("message"),
                                        Toast.LENGTH_LONG).show();
                            }
                            progressDialog.dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                            Toast.makeText(AddPlaylistActivity.this, "Server not reachable...\nTry again later!", Toast.LENGTH_LONG).show();
                            Log.i("CUSTOM", "Response Error Listener = " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(AddPlaylistActivity.this, "Server not reachable...\nTry again later!", Toast.LENGTH_LONG).show();
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

    private void showStandardDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Important");
        String msg = "Playlist '" + playlist.getTitle() + "' was assigned under a standard which now has been deleted. Please set a new Standard and Continue.";
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

    private void setTextViewOfTitles() {
        String data = "";
        for (int i = 0; i < displayTitleList.size(); i++) {
            data += (i + 1) + ". " + displayTitleList.get(i) + "\n";
        }
        selectedTitlesTV.setText(data.trim());
        spinner.setSelection(0);
        Log.i("CUSTOM", "setTextViewOfTitles() - " + selectedTitlesTV.getText());
    }

    private void loadData() {
        progressDialog.show();
        progressDialog.setMessage("Loading...");
        RequestQueue requestQueue = Volley.newRequestQueue(AddPlaylistActivity.this);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                VIEW_VIDEO_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (!jsonObject.getBoolean("error")) {
                                JSONArray dataArray = jsonObject.getJSONArray("data");
                                videosList.clear();
                                videoTitleList.clear();
                                if (!edit) {
                                    videoIdList.clear();
                                }
                                videoTitleList.add("Select Videos :");
                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject obj = dataArray.getJSONObject(i);
                                    Videos video = new Videos();
                                    video.setId(obj.getString("id"));
                                    video.setTitle(obj.getString("title"));
                                    video.setDescription(obj.getString("description"));
                                    video.setVideoId(obj.getString("video_id"));
                                    videosList.add(video);
                                    videoTitleList.add(obj.getString("title"));
                                    if (edit) {
                                        if (videoIdList.contains(video.getId())) {
                                            displayTitleList.add(video.getTitle());
                                        }
                                    }
                                    Log.i("CUSTOM", video.getId() + " - " + video.getTitle());
                                }
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(AddPlaylistActivity.this,
                                        android.R.layout.simple_list_item_1, videoTitleList);
                                spinner.setAdapter(adapter);
                                if (edit) {
                                    setTextViewOfTitles();
                                }
                            } else {
                                Toast.makeText(AddPlaylistActivity.this, "" + jsonObject.getString("message"),
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
                        Log.i("CUSTOM", "Response Error Listener = " + error.getMessage());
                    }
                });
        requestQueue.add(stringRequest);
    }

    private void uploadData(String URL) {
        progressDialog.show();
        progressDialog.setMessage("Loading...");
        RequestQueue requestQueue = Volley.newRequestQueue(AddPlaylistActivity.this);
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
                                    Toast.makeText(AddPlaylistActivity.this,
                                            "Playlist edited Successfully!", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(AddPlaylistActivity.this,
                                            "Playlist added Successfully!", Toast.LENGTH_LONG).show();
                                    clearData();
                                }

                            } else {
                                Toast.makeText(AddPlaylistActivity.this, "" + jsonObject.getString("message"),
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
                        Toast.makeText(AddPlaylistActivity.this, "Server not reachable...\nTry again later!", Toast.LENGTH_LONG).show();
                        Log.i("CUSTOM", "Response Error Listener = " + error.getMessage());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                if (edit) {
                    params.put("id", playlist.getId());
                }
                params.put("title", playlist.getTitle());
                params.put("description", playlist.getDescription());
                params.put("video_ids", playlist.getVideoIds());
                params.put("standard", playlist.getStandardId());
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void deleteData() {
        progressDialog.show();
        progressDialog.setMessage("Loading...");
        RequestQueue requestQueue = Volley.newRequestQueue(AddPlaylistActivity.this);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                DELETE_PLAYLIST_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.i("CUSTOM", "response = " + response);
                            JSONObject jsonObject = new JSONObject(response);
                            if (!jsonObject.getBoolean("error")) {
                                progressDialog.dismiss();
                                Toast.makeText(AddPlaylistActivity.this, "Video deleted Successfully!",
                                        Toast.LENGTH_LONG).show();
                                onBackPressed();
                            } else {
                                Toast.makeText(AddPlaylistActivity.this, "" + jsonObject.getString("message"),
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
                        Toast.makeText(AddPlaylistActivity.this, "Server not reachable...\nTry again later!", Toast.LENGTH_LONG).show();
                        Log.i("CUSTOM", "Response Error Listener = " + error.getCause().getMessage());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id", playlist.getId());
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void clearData() {
        titleED.setText("");
        descED.setText("");
        spinner.setSelection(0);
        selectedTitlesTV.setText("No Video Selected.");
        videoIdList.clear();
        displayTitleList.clear();
        standardSpinner.setSelection(0);
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
        if (videoIdList.isEmpty()) {
            Toast.makeText(this, "No Video Selected.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (standardSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Select Standard", Toast.LENGTH_LONG).show();
            return false;
        }
        if (!edit) {
            playlist = new Playlist();
        }
        playlist.setTitle(titleED.getText().toString().trim());
        playlist.setDescription(descED.getText().toString().trim());
        String data = "";
        for (String id : videoIdList) {
            data += id + ",";
        }
        playlist.setVideoIds(data);
        playlist.setStandardId(standardArrayList.get(standardSpinner.getSelectedItemPosition()-1).getId());
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
            startActivity(new Intent(AddPlaylistActivity.this, ViewPlaylistsActivity.class));
        } else {
            startActivity(new Intent(AddPlaylistActivity.this, HomeAdminActivity.class));
        }
        finish();
        super.onBackPressed();
    }
}
