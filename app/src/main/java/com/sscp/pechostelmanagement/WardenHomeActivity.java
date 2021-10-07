package com.sscp.pechostelmanagement;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;
import com.google.firebase.auth.FirebaseAuth;

public class WardenHomeActivity extends AppCompatActivity {

    ImageView take_attendance, check_attendance, updateAttendance, logout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_warden_home);

        logout = findViewById(R.id.warden_logout);
        take_attendance = findViewById(R.id.take_attendance);
        check_attendance = findViewById(R.id.check_attendance);
        updateAttendance = findViewById(R.id.update_attendance);

        take_attendance.setOnClickListener(view -> startActivity(new Intent(WardenHomeActivity.this, AddAttendance.class)));

        check_attendance.setOnClickListener(view -> startActivity(new Intent(WardenHomeActivity.this, AttendanceDateDetails.class)));
        updateAttendance.setOnClickListener(v->{
            startActivity(new Intent(WardenHomeActivity.this, UpdateAttendance.class));
        });
        logout.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(WardenHomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

}