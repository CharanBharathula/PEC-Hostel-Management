package com.sscp.pechostelmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.diegodobelo.expandingview.ExpandingList;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public class UpdateAttendance extends AppCompatActivity {

    Button time_choose;
    Spinner yr;
    ImageView upload;

    ExpandingList mExpandingList;
    DatabaseReference ref;
    SimpleDateFormat _12HourSDF, _24HourSDF;
    Date _24HourDt;
    HashMap<String, StudentClass> studentDetails;

    String timePicked, year;
    String[] newString;
    int h, m;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Objects.requireNonNull(getSupportActionBar()).hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_update_attendance);

        time_choose = findViewById(R.id.choose_time);
        upload = findViewById(R.id.upload_updated_attendance);
        yr = findViewById(R.id.select_yr);
        mExpandingList = findViewById(R.id.expanding_list_students);
        ref = FirebaseDatabase.getInstance().getReference();

        time_choose.setOnClickListener(view -> {
            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(UpdateAttendance.this,
                    (timePicker, selectedHour, selectedMinute) -> {
                        try{
                            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");
                            Date date = sdf.parse(selectedHour+":"+selectedMinute);
                            timePicked = sdf.format(date);
                        }catch(ParseException e){
                            e.printStackTrace();
                        }
                    }, 12, 0, false);
            mTimePicker.setTitle("Select Time");
            mTimePicker.show();
            Toast.makeText(getApplicationContext(), ""+timePicked, Toast.LENGTH_SHORT).show();
        });

        yr.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                year = adapterView.getItemAtPosition(i).toString();
                createItems();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void createItems() {
        if(year == null)
            Toast.makeText(getApplicationContext(), "Please choose year", Toast.LENGTH_SHORT).show();
        else{
            int[] colors = {R.color.black, R.color.blue, R.color.yellow, R.color.orange, R.color.pink};

            ref.child("Students").child(year).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot rollNo:snapshot.getChildren()){
                        StudentClass student = rollNo.getValue(StudentClass.class);
                        studentDetails.put(rollNo.getKey(), student);

                    }
                    Toast.makeText(getApplicationContext(), ""+studentDetails, Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

}