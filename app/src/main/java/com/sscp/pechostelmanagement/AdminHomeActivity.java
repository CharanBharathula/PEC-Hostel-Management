package com.sscp.pechostelmanagement;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class AdminHomeActivity extends AppCompatActivity {

    ImageView gotoProfile, add_student, add_warden, outing_requests, attendance,removeStudent, removeWarden;
    DatabaseReference ref;
    String batch, wardenKey, rNo, rollN;
    boolean wardenExist = false, studentExistInRoom = false, stop = false, batchExist = false, rollExist = false;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_admin_home);

        gotoProfile = findViewById(R.id.profile_icon);
        add_student = findViewById(R.id.add_student);
        add_warden = findViewById(R.id.add_warden);
        attendance = findViewById(R.id.attendance);
        outing_requests = findViewById(R.id.outing_requests);
        removeStudent = findViewById(R.id.remove_student);
        removeWarden = findViewById(R.id.remove_warden);
        ref = FirebaseDatabase.getInstance().getReference();
        pd = new ProgressDialog(this);

        add_student.setOnClickListener(view -> startActivity(new Intent(AdminHomeActivity.this, AddStudent.class)));

        add_warden.setOnClickListener(v -> {
            View view=getLayoutInflater().inflate(R.layout.activity_add_warden,null);

            AlertDialog.Builder builder=new AlertDialog.Builder(AdminHomeActivity.this);
            builder.setView(view);
            builder.setTitle("Add Warden");
            final AlertDialog alert=builder.create();
            alert.show();
            final TextInputLayout name=view.findViewById(R.id.warden_name);
            final TextInputLayout mobile=view.findViewById(R.id.warden_mobile);
            final TextInputLayout email=view.findViewById(R.id.warden_email);

            Button submit=view.findViewById(R.id.register_warden);

            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(name.getEditText().getText().toString().equals(""))
                        name.setError("Enter name please");
                    else if(mobile.getEditText().getText().toString().equals(""))
                        mobile.setError("Enter mobile please");
                    else
                    {
                        ProgressDialog progressDialog=new ProgressDialog(AdminHomeActivity.this);
                        progressDialog.setTitle("Please Wait...");
                        progressDialog.setMessage("Signing Up please wait a while :)");
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Warden");
                        HashMap<String,Object> studentDetails=new HashMap<>();
                        String id = ref.push().getKey();
                        studentDetails.put("warden_name",name.getEditText().getText().toString());
                        studentDetails.put("warden_mobile",mobile.getEditText().getText().toString());
                        studentDetails.put("warden_email",email.getEditText().getText().toString());
                        studentDetails.put("warden_password",mobile.getEditText().getText().toString());
                        ref.child(id).setValue(studentDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(getApplicationContext(), "Warden Added to database", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                    alert.dismiss();

                                }
                                else{
                                    FirebaseException e = (FirebaseException) task.getException();
                                    Toast.makeText(AdminHomeActivity.this, "Failed Storing data: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                    alert.dismiss();
                                }
                            }
                        });
                    }
                }
            });
        });

        attendance.setOnClickListener(view -> startActivity(new Intent(AdminHomeActivity.this, Attendance.class)));

        outing_requests.setOnClickListener(v->{ startActivity(new Intent(AdminHomeActivity.this, OutingRequets.class)); });

        gotoProfile.setOnClickListener(view -> startActivity(new Intent(AdminHomeActivity.this, AdminProfileActivity.class)));

        removeStudent.setOnClickListener(view->{
            View v=getLayoutInflater().inflate(R.layout.remove_user,null);

            AlertDialog.Builder builder=new AlertDialog.Builder(AdminHomeActivity.this);
            builder.setView(v);
            builder.setTitle("Remove Student");
            final AlertDialog alert=builder.create();
            alert.show();

            EditText uName = v.findViewById(R.id.username);
            uName.setHint("Enter Student Roll Number");
            Button removeUser = v.findViewById(R.id.remove);
            Spinner sp = v.findViewById(R.id.select_b);

            sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    batch = adapterView.getItemAtPosition(i).toString();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            removeUser.setOnClickListener(vi->{
                String us = uName.getText().toString();
                if(us.equals(""))
                    Toast.makeText(getApplicationContext(), "Please Enter Student Roll Number", Toast.LENGTH_SHORT).show();
                else if(batch.equals("Choose Batch"))
                    Toast.makeText(getApplicationContext(), "Please Choose batch", Toast.LENGTH_SHORT).show();
                else{
                    ref.child("Students").child(batch).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                deleteStudentFromStudents(us);
                                batchExist = true;
                            }
                            else
                                Toast.makeText(getApplicationContext(),batch+" doesn't exist", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    if(batchExist && rollExist){
                        deleteStudentFromRoom(us);
                        deleteStudentFromAttendance(us);
                        pd.dismiss();
                        Toast.makeText(getApplicationContext(), "Student was removed successfully", Toast.LENGTH_SHORT).show();
                        alert.dismiss();
                    }
                }
            });

        });

        removeWarden.setOnClickListener(view->{
            View v=getLayoutInflater().inflate(R.layout.remove_user,null);

            AlertDialog.Builder builder=new AlertDialog.Builder(AdminHomeActivity.this);
            builder.setView(v);
            builder.setTitle("Remove Warden");
            final AlertDialog alert=builder.create();
            alert.show();

            EditText uName = v.findViewById(R.id.username);
            uName.setHint("Enter Warden Email");
            Button removeUser = v.findViewById(R.id.remove);
            Spinner sp = v.findViewById(R.id.select_b);
            sp.setVisibility(View.GONE);

            removeUser.setOnClickListener(vi->{
                String us = uName.getText().toString();
                if(us.equals(""))
                    Toast.makeText(getApplicationContext(), "Please Warden email", Toast.LENGTH_SHORT).show();
                else{
                    ref.child("Warden").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot snap: snapshot.getChildren()){
                                if(snap.child("warden_email").getValue().equals(us)){
                                    wardenExist = true;
                                    wardenKey = snap.getKey();
                                }
                            }
                            if(wardenExist) {
                                ref.child("Warden").child(wardenKey).removeValue();
                                Toast.makeText(getApplicationContext(), "Warden Was Deleted Successfully", Toast.LENGTH_SHORT).show();
                                alert.dismiss();
                            }
                            else{
                                Toast.makeText(getApplicationContext(), "Warden Email "+us+" doesn't exist", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            });
        });

    }

    private void deleteStudentFromAttendance(String us) {
        ref.child("Attendance").child(batch).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot years) {
                for(DataSnapshot year:years.getChildren()){
                    for(DataSnapshot date:year.getChildren()){
                        for(DataSnapshot time:date.getChildren()){
                            stop = false;
                            for(DataSnapshot roomNo:time.getChildren()){
                                if(stop)
                                    break;
                                for(DataSnapshot rollNumber:roomNo.getChildren()){

                                    if(rollNumber.getKey().equals(us)){
                                        String r = rollNumber.getKey();
                                        String y = year.getKey();
                                        String d = date.getKey();
                                        String t = time.getKey();
                                        String rn = roomNo.getKey();

                                        ref.child("Attendance").child(batch).child(y)
                                                .child(d).child(t)
                                                .child(rn).child(r).removeValue();
                                        stop = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void deleteStudentFromRoom(String us) {
        ref.child("RoomDetails").child(batch).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot rooms) {
                for(DataSnapshot roomNo: rooms.getChildren()){
                    for(DataSnapshot roll:roomNo.getChildren()){
                        if(roll.getKey().equals(us)){
                            studentExistInRoom = true;
                            rNo = roomNo.getKey();
                            rollN = roll.getKey();
                            break;
                        }
                    }
                }
                if(studentExistInRoom)
                    ref.child("RoomDetails").child(batch).child(rNo).child(rollN).removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void deleteStudentFromStudents(String us) {
        ref.child("Students").child(batch).child(us).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot roll) {
                if(roll.exists()){
                    pd.setTitle("Removing");
                    pd.setMessage("Removing Student Please Wait");
                    pd.setCancelable(false);
                    pd.show();
                    ref.child("Students").child(batch).child(us).removeValue();
                    rollExist = true;
                }
                else{
                    Toast.makeText(getApplicationContext(), "Student with Roll Number "+us+" doesn't exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}