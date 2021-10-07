package com.sscp.pechostelmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StudentAttendanceSpecific extends AppCompatActivity {

    String time, year, batch, date, attendanceType;
    ArrayList<String> students;
    HashMap<String, StudentClass> studentDetails;
    List<HashMap<String, String>> entries;

    ListView listView;
    DatabaseReference ref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Objects.requireNonNull(getSupportActionBar()).hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_student_attendance_specific);

        Intent i = getIntent();
        time = i.getStringExtra("time");
        date = i.getStringExtra("date");
        batch = i.getStringExtra("batch");
        year = i.getStringExtra("year");
        attendanceType = i.getStringExtra("type");
        students = new ArrayList<>();
        entries = new ArrayList<>();
        studentDetails = new HashMap<>();

        ref = FirebaseDatabase.getInstance().getReference();

        listView = findViewById(R.id.students);

        retrieveStudentDetails();

        ref.child("Attendance").child(batch).child(year).child(date).child(time).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot child:snapshot.getChildren()){
                    if(!child.getKey().equals("count")){
                        HashMap<String, String> map = (HashMap<String, String>) child.getValue();
                        entries.add(map);
                    }
                }
                List<String> list = new ArrayList<>();
                for(HashMap<String, String> map:entries){

                    for (Map.Entry<String,String> entry : map.entrySet()){
                        if(attendanceType.equals("A")) {
                            if (entry.getValue().equals("Absent"))
                                list.add(entry.getKey());
                        }
                        else if(attendanceType.equals("P")) {
                            if (entry.getValue().equals("Present"))
                                list.add(entry.getKey());
                        }
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter <>(StudentAttendanceSpecific.this, android.R.layout.simple_list_item_1, list);
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                listView.setOnItemClickListener((adapterView, view, i1, l) -> {
                    String studentRoll = adapterView.getItemAtPosition(i1).toString();
                    retrieveStudent(studentRoll);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void retrieveStudentDetails() {
        ref.child("Students").child(batch).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot rollNo:snapshot.getChildren()){
                    studentDetails.put(rollNo.getKey(), rollNo.getValue(StudentClass.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void retrieveStudent(String rollNo) {
        StudentClass student = studentDetails.get(rollNo);

        View view=getLayoutInflater().inflate(R.layout.student_layout,null);

        AlertDialog.Builder builder=new AlertDialog.Builder(StudentAttendanceSpecific.this);
        builder.setView(view);

        final AlertDialog alert=builder.create();
        alert.show();

        TextView name = view.findViewById(R.id.std_name);
        assert student != null;
        name.setText(student.getStudentname());
        TextView mobile = view.findViewById(R.id.std_mobile);
        mobile.setText(student.getMobile());
        TextView roll = view.findViewById(R.id.std_roll);
        roll.setText(student.getRoll_no());
        TextView branch = view.findViewById(R.id.std_branch);
        branch.setText(student.getBranch());
        TextView room = view.findViewById(R.id.std_room);
        room.setText(student.getRoom_no());
    }
}