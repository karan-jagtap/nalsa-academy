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
import com.nalsasupport.nalsaacademy.model.Playlist;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import static com.nalsasupport.nalsaacademy.config.AppUrls.VIEW_PLAYLIST_URL;
import static com.nalsasupport.nalsaacademy.config.AppUrls.VIEW_VIDEO_URL;

public class ViewPlaylistsActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<String> titleList;
    private ArrayList<Playlist> playlistsArrayList;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_playlists);
        Toolbar toolbar = findViewById(R.id.toolbar_ViewPlaylistsActivity);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        listView = findViewById(R.id.listView_ViewPlaylistsActivity);
        progressDialog = new ProgressDialog(ViewPlaylistsActivity.this);
        titleList = new ArrayList<>();
        playlistsArrayList = new ArrayList<>();
        loadVideosList();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(ViewPlaylistsActivity.this, AddPlaylistActivity.class);
                i.putExtra("playlist", playlistsArrayList.get(position));
                startActivity(i);
                finish();
            }
        });
    }

    private void loadVideosList() {
        progressDialog.show();
        progressDialog.setMessage("Loading...");
        RequestQueue requestQueue = Volley.newRequestQueue(ViewPlaylistsActivity.this);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                VIEW_PLAYLIST_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Log.i("CUSTOM", "response = " + response);
                            if (!jsonObject.getBoolean("error")) {
                                JSONArray dataArray = jsonObject.getJSONArray("data");
                                playlistsArrayList.clear();
                                titleList.clear();
                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject obj = dataArray.getJSONObject(i);
                                    Playlist playlist = new Playlist();
                                    playlist.setId(obj.getString("id"));
                                    playlist.setTitle(obj.getString("title"));
                                    playlist.setDescription(obj.getString("description"));
                                    playlist.setVideoIds(obj.getString("video_ids"));
                                    playlist.setStandardId(obj.getString("standard"));
                                    playlistsArrayList.add(playlist);
                                    titleList.add(playlist.getTitle());
                                    Log.i("CUSTOM", obj.getString("title"));
                                }
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(ViewPlaylistsActivity.this, android.R.layout.simple_list_item_1, titleList);
                                listView.setAdapter(adapter);
                            } else {
                                Toast.makeText(ViewPlaylistsActivity.this, "" + jsonObject.getString("message"),
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
                        progressDialog.dismiss();
                        Toast.makeText(ViewPlaylistsActivity.this, "Server not reachable...\nTry again later!", Toast.LENGTH_LONG).show();
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
        startActivity(new Intent(ViewPlaylistsActivity.this, HomeAdminActivity.class));
        finish();
        super.onBackPressed();
    }
}
