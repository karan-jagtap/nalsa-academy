package com.nalsasupport.nalsaacademy.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.nalsasupport.nalsaacademy.R;
import com.nalsasupport.nalsaacademy.helper.SessionManager;

public class DeciderActivity extends AppCompatActivity {

    Button studentBT, adminBT;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decider);

        sessionManager = new SessionManager(DeciderActivity.this);
        checkIfAlreadyLoggedIn();

        studentBT = findViewById(R.id.button_student_login);
        adminBT = findViewById(R.id.button_admin_login);

        studentBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DeciderActivity.this, LoginStudentActivity.class));
            }
        });

        adminBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DeciderActivity.this, LoginActivity.class));
            }
        });
    }

    private void checkIfAlreadyLoggedIn() {
        if (sessionManager.isLoggedIn()) {
            if (sessionManager.isAdmin()) {
                startActivity(new Intent(DeciderActivity.this, HomeAdminActivity.class));
            } else {
                startActivity(new Intent(DeciderActivity.this, HomeActivity.class));
            }
            finish();
        }
    }
}
