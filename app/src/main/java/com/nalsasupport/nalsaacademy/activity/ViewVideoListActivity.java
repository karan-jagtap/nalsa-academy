package com.nalsasupport.nalsaacademy.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import java.util.Collections;
import java.util.Objects;

import static com.nalsasupport.nalsaacademy.config.AppUrls.VIEW_VIDEO_URL;

public class ViewVideoListActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<String> titleList;
    private ArrayList<Videos> videosArrayList;
    private static ProgressDialog progressDialog;

    private static ArrayList<String> videoIdsList;
    private static String videoIds, playlistName, description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_videos_list);
        Toolbar toolbar = findViewById(R.id.toolbar_ViewVideoListActivity);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        listView = findViewById(R.id.listView_ViewVideoListActivity);
        progressDialog = new ProgressDialog(ViewVideoListActivity.this);
        titleList = new ArrayList<>();
        videosArrayList = new ArrayList<>();
        videoIdsList = new ArrayList<>();

        if (getIntent().getExtras() != null || !videoIdsList.isEmpty()) {
            videoIds = getIntent().getStringExtra("video_ids");
            playlistName = getIntent().getStringExtra("title");
            description = getIntent().getStringExtra("description");
            getSupportActionBar().setTitle(playlistName);
            String data[] = videoIds.split(",");
            Collections.addAll(videoIdsList, data);
            loadVideosList();
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (videosArrayList.get(position).getTitle().equals(titleList.get(position))) {
                    Intent intent = new Intent(ViewVideoListActivity.this, CustomPlayerActivity.class);
                    intent.putExtra("video", videosArrayList.get(position));
                    startActivity(intent);
                }
            }
        });
    }

    private void loadVideosList() {
        progressDialog.show();
        progressDialog.setMessage("Loading...");
        RequestQueue requestQueue = Volley.newRequestQueue(ViewVideoListActivity.this);
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
                                    if (videoIdsList.contains(video.getId())) {
                                        videosArrayList.add(video);
                                        titleList.add(obj.getString("title"));
                                    }
                                    Log.i("CUSTOM", obj.getString("title"));
                                }
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(ViewVideoListActivity.this, android.R.layout.simple_list_item_1, titleList);
                                listView.setAdapter(adapter);
                            } else {
                                Toast.makeText(ViewVideoListActivity.this, "" + jsonObject.getString("message"),
                                        Toast.LENGTH_LONG).show();
                            }
                            progressDialog.dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                            Toast.makeText(ViewVideoListActivity.this, "Server not reachable...\nTry again later!", Toast.LENGTH_LONG).show();
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

    private void showInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Info");
        builder.setCancelable(true);
        String msg = "\nPlaylist Title :\n"+playlistName+"\n\nDescription :\n"+description;
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder(msg);
        stringBuilder.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.nalsa_blue)),
                msg.indexOf("Playlist Title"), ("Playlist Title".length()+msg.indexOf("Playlist Title")), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        stringBuilder.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.nalsa_blue)),
                msg.indexOf("Description"), ("Description".length()+msg.indexOf("Description")), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setMessage(stringBuilder)
                .setCancelable(false)
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_info, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_info) {
            showInfoDialog();
        }
        return super.onOptionsItemSelected(item);
    }
}
