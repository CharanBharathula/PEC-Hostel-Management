package com.sscp.pechostelmanagement;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;

public class Attendance extends AppCompatActivity {

    ImageView consolidated_reports, specific_reports, check_attendance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_attendance);

        consolidated_reports = findViewById(R.id.consolidated_attendance_report);
        specific_reports = findViewById(R.id.specific_attendance_report);
        check_attendance = findViewById(R.id.check_att);

        consolidated_reports.setOnClickListener(v->{

        });

        specific_reports.setOnClickListener(v->{

        });
        check_attendance.setOnClickListener(v->{
            startActivity(new Intent(Attendance.this, AttendanceDateDetails.class));
        });

    }
}