package com.sscp.pechostelmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddAttendance extends AppCompatActivity {

    DatabaseReference ref;
    ExpandableListView expandableListView;
    ExpandableListViewAdapter adapter;
    ExpandableListAdapter listAdapter;
    List<String> expandableListTitle;
    HashMap<String, List<String>> expandableListDetail = new HashMap<String, List<String>>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_add_attendance);

        expandableListView = findViewById(R.id.expandable_listView);

        retriveData();

        expandableListTitle = new ArrayList<String>(expandableListDetail.keySet());
        listAdapter = new ExpandableListViewAdapter(this, expandableListTitle, expandableListDetail);
        expandableListView.setAdapter(listAdapter);
    }

    private void retriveData() {
        ref = FirebaseDatabase.getInstance().getReference("Students");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snap:snapshot.getChildren()){
                    Student  student = snap.getValue(Student.class);
                    Toast.makeText(getApplicationContext(), ""+student.getName(), Toast.LENGTH_SHORT).show();
                }
                /*for(DataSnapshot snap:snapshot.getChildren()){
                    Student  student = snap.getValue(Student.class);
                    expandableListDetail.get(student.getRoomNo()).add(student.getRollNo());
                }
                */
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}