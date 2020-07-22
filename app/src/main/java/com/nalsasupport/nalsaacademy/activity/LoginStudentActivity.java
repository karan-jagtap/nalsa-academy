package com.nalsasupport.nalsaacademy.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
import com.nalsasupport.nalsaacademy.helper.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.nalsasupport.nalsaacademy.config.AppUrls.LOGIN_STUDENT_URL;

public class LoginStudentActivity extends AppCompatActivity {

    private EditText emailED, passED;
    private Button loginBT;
    private String emailD, passwordD, secureIdD;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_student);
        Toolbar toolbar = findViewById(R.id.toolbar_LoginStudentActivity);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        emailED = findViewById(R.id.editText_email_LoginStudentActivity);
        passED = findViewById(R.id.editText_password_LoginStudentActivity);
        loginBT = findViewById(R.id.button_login_LoginStudentActivity);
        progressDialog = new ProgressDialog(LoginStudentActivity.this);
        secureIdD= Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        loginBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    loginUser();
                }
            }
        });
    }

    private void loginUser() {
        progressDialog.show();
        progressDialog.setMessage("Loading...");
        RequestQueue requestQueue = Volley.newRequestQueue(LoginStudentActivity.this);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                //TODO:: Change url to student login
                LOGIN_STUDENT_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.i("CUSTOM","response = "+response);
                            JSONObject jsonObject = new JSONObject(response);
                            if (!jsonObject.getBoolean("error")) {
                                if(jsonObject.getString("message").equals("success")) {
                                    SessionManager sessionManager = new SessionManager(LoginStudentActivity.this);
                                    sessionManager.setLogin(true);
                                    sessionManager.setAsAdmin(false);
                                    sessionManager.setStandardId(jsonObject.getString("standard"));
                                    progressDialog.dismiss();
                                    Toast.makeText(LoginStudentActivity.this,
                                            "Login Successful!", Toast.LENGTH_LONG).show();
                                    Intent i = new Intent(LoginStudentActivity.this, HomeActivity.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(i);
                                    finish();
                                } else if(jsonObject.getString("message").equals("invalid_id")){
                                    Toast.makeText(LoginStudentActivity.this,
                                            "Login Failed!\nDevice not recognized.", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                if (jsonObject.getString("message").equals("invalid")) {
                                    Toast.makeText(LoginStudentActivity.this, "Invalid Credentials.\nPlease Try Again!", Toast.LENGTH_LONG).show();
                                } else if(jsonObject.getString("message").equals("invalid_id")){
                                    Toast.makeText(LoginStudentActivity.this,
                                            "Login Failed!\nDevice not recognized.", Toast.LENGTH_LONG).show();
                                }
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
                        Toast.makeText(LoginStudentActivity.this, "Server not reachable...\nTry again later!", Toast.LENGTH_LONG).show();
                        Log.i("CUSTOM", "Response Error Listener = " + error.getMessage());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("email", emailD);
                params.put("password", passwordD);
                params.put("secure_id", secureIdD);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    private boolean validate() {
        if (emailED.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Enter Email", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (passED.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show();
            return false;
        }
        emailD = emailED.getText().toString().trim();
        passwordD = passED.getText().toString().trim();
        return true;
    }
}
