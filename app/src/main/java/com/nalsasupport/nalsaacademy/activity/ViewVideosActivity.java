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
import com.nalsasupport.nalsaacademy.model.Videos;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import static com.nalsasupport.nalsaacademy.config.AppUrls.VIEW_VIDEO_URL;

public class ViewVideosActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<String> titleList;
    private ArrayList<Videos> videosArrayList;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_videos);
        Toolbar toolbar = findViewById(R.id.toolbar_ViewVideosActivity);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        listView = findViewById(R.id.listView_ViewVideosActivity);
        progressDialog = new ProgressDialog(ViewVideosActivity.this);
        titleList = new ArrayList<>();
        videosArrayList = new ArrayList<>();
        loadVideosList();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("CUSTOM", "on video clicked title = " + videosArrayList.get(position).getId());
                Intent i = new Intent(ViewVideosActivity.this, AddVideosActivity.class);
                i.putExtra("video", videosArrayList.get(position));
                startActivity(i);
                finish();
            }
        });
    }

    private void loadVideosList() {
        progressDialog.show();
        progressDialog.setMessage("Loading...");
        RequestQueue requestQueue = Volley.newRequestQueue(ViewVideosActivity.this);
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
                                videosArrayList.clear();
                                titleList.clear();
                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject obj = dataArray.getJSONObject(i);
                                    Videos video = new Videos();
                                    video.setId(obj.getString("id"));
                                    video.setTitle(obj.getString("title"));
                                    video.setDescription(obj.getString("description"));
                                    video.setVideoId(obj.getString("video_id"));
                                    videosArrayList.add(video);
                                    titleList.add(obj.getString("title"));
                                    Log.i("CUSTOM", obj.getString("title"));
                                }
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(ViewVideosActivity.this, android.R.layout.simple_list_item_1, titleList);
                                listView.setAdapter(adapter);
                            } else {
                                Toast.makeText(ViewVideosActivity.this, "" + jsonObject.getString("message"),
                                        Toast.LENGTH_LONG).show();
                            }
                            progressDialog.dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                            Toast.makeText(ViewVideosActivity.this, "Server not reachable...\nTry again later!", Toast.LENGTH_LONG).show();
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
        startActivity(new Intent(ViewVideosActivity.this, HomeAdminActivity.class));
        finish();
        super.onBackPressed();
    }
}
