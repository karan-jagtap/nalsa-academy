package com.nalsasupport.nalsaacademy.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nalsasupport.nalsaacademy.R;
import com.nalsasupport.nalsaacademy.helper.SessionManager;
import com.nalsasupport.nalsaacademy.model.Playlist;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.nalsasupport.nalsaacademy.config.AppUrls.FILTER_PLAYLIST_URL;
import static com.nalsasupport.nalsaacademy.config.AppUrls.VALIDATION_URL;

public class HomeActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<String> titleList;
    private ArrayList<Playlist> playlistsArrayList;
    private ProgressDialog progressDialog;
    private SessionManager sessionManager;
    private String standardId;
    private TextView msgTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar_HomeActivity);
        setSupportActionBar(toolbar);

        listView = findViewById(R.id.listView_HomeActivity);
        msgTV = findViewById(R.id.textView_msg_HomeActivity);
        progressDialog = new ProgressDialog(HomeActivity.this);
        sessionManager = new SessionManager(HomeActivity.this);
        standardId = sessionManager.getStandardId();
        titleList = new ArrayList<>();
        playlistsArrayList = new ArrayList<>();
        config();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (playlistsArrayList.get(position).getTitle().equals(titleList.get(position))) {
                    Intent intent = new Intent(HomeActivity.this, ViewVideoListActivity.class);
                    intent.putExtra("video_ids", playlistsArrayList.get(position).getVideoIds());
                    intent.putExtra("title", playlistsArrayList.get(position).getTitle());
                    intent.putExtra("description", playlistsArrayList.get(position).getDescription());
                    startActivity(intent);
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
            HomeActivity.this.finish();
            //return true;
        } else if (today.compareTo(date) < 0) {
            //return false;
        }
        // return true if today;s date is allowed
        // else return false
    }

    private void loadPlaylists() {

        RequestQueue requestQueue = Volley.newRequestQueue(HomeActivity.this);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                FILTER_PLAYLIST_URL,
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
                                    playlistsArrayList.add(playlist);
                                    titleList.add(playlist.getTitle());
                                    Log.i("CUSTOM", obj.getString("title"));
                                }
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(HomeActivity.this, android.R.layout.simple_list_item_1, titleList);
                                listView.setAdapter(adapter);

                                if (playlistsArrayList.isEmpty()) {
                                    msgTV.setVisibility(View.VISIBLE);
                                    msgTV.setText("No Playlist Found");
                                } else {
                                    msgTV.setVisibility(View.GONE);
                                }
                            } else {
                                Toast.makeText(HomeActivity.this, "" + jsonObject.getString("message"),
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
                        Toast.makeText(HomeActivity.this, "Server not reachable...\nTry again later!", Toast.LENGTH_LONG).show();
                        Log.i("CUSTOM", "Response Error Listener = " + error.getMessage());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("standard", standardId);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                9000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }

    private void config() {
        progressDialog.show();
        progressDialog.setMessage("Loading...");
        RequestQueue requestQueue = Volley.newRequestQueue(HomeActivity.this);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                VALIDATION_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("CUSTOM", "response = " + response);
                        if (response.equals("TRUE")) {
                            HomeActivity.this.finish();
                        }
                        loadPlaylists();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        //Toast.makeText(HomeActivity.this, "Server not reachable...\nTry again later!", Toast.LENGTH_LONG).show();
                        Log.i("CUSTOM", "Response Error Listener = " + error.getMessage());
                    }
                });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_admin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            SessionManager sm = new SessionManager(HomeActivity.this);
            sm.setLogin(false);
            sm.setAsAdmin(false);
            startActivity(new Intent(HomeActivity.this, DeciderActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
