package com.sscp.pechostelmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddStudent extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    TextInputLayout name, room_no, mobile, roll_no, email;
    Spinner branch,year, sem;
    DatabaseReference ref;
    String b, y, s, n, room, roll, mob, em;
    Button register;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_add_student);

        Initialize();
        branch.setOnItemSelectedListener(this);
        year.setOnItemSelectedListener(this);
        sem.setOnItemSelectedListener(this);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateData())
                    storeData();
            }
        });

    }


    public boolean validateData(){
        n = name.getEditText().getText().toString();
        room = room_no.getEditText().getText().toString();
        mob = mobile.getEditText().getText().toString();
        roll = roll_no.getEditText().getText().toString();
        em = email.getEditText().getText().toString();
        if(n.isEmpty()) {
            name.setError("Name should not be empty");
            return false;
        }
        else if(room.isEmpty()) {
            room_no.setError("Roll Number should not be empty");
            return false;
        }
        else if(mob.isEmpty()) {
            mobile.setError("Mobile should nt be empty");
            return false;
        }
        else if(roll.isEmpty() ) {
            roll_no.setError("Roll number should no be empty");
            return false;
        }
        else if(em.isEmpty())
            email.setError("Email should not be empty");
        return true;
    }

    public void storeData(){

        ProgressDialog progressDialog=new ProgressDialog(AddStudent.this);
        progressDialog.setTitle("Please Wait...");
        progressDialog.setMessage("Signing Up please wait a while :)");
        progressDialog.setCancelable(false);
        progressDialog.show();


        ref= FirebaseDatabase.getInstance().getReference();
        HashMap<String,Object> studentDetails=new HashMap<>();
        String userid = ref.push().getKey();
        studentDetails.put("userid",userid);
        studentDetails.put("studentname",n);
        studentDetails.put("email","");
        studentDetails.put("password",roll);
        studentDetails.put("mobile",mob);
        studentDetails.put("imageurl","");
        studentDetails.put("branch",b);
        studentDetails.put("year",y);
        studentDetails.put("semester",s);
        studentDetails.put("room_no", room);
        studentDetails.put("roll_no", roll);



        ref.child("Students").child(y).child(userid).setValue(studentDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    progressDialog.dismiss();
                    Intent intent = new Intent(AddStudent.this, AdminHomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                else{
                    FirebaseException e = (FirebaseException) task.getException();
                    Toast.makeText(AddStudent.this, "Failed Storing data: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        });

        addStudentToRoom(userid);
    }

    private void addStudentToRoom(String userid) {
        List<String> list = new ArrayList<>();
        list.add(room);
        list.add(userid);
        ref.child("RoomDetails").child(y).child(room).child(roll).setValue(list).addOnCompleteListener(task -> {
            if (task.isSuccessful())
                Toast.makeText(getApplicationContext(), "Student Successfully added", Toast.LENGTH_SHORT).show();
            else{
                FirebaseException e = (FirebaseException) task.getException();
                Toast.makeText(AddStudent.this, "Failed Storing data: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void Initialize(){
        name = findViewById(R.id.student_name);
        room_no = findViewById(R.id.student_room);
        mobile = findViewById(R.id.student_mobile);
        roll_no = findViewById(R.id.rollno);
        branch = findViewById(R.id.branch);
        year = findViewById(R.id.year);
        sem = findViewById(R.id.semester);
        register = findViewById(R.id.register_student);
        email = findViewById(R.id.student_email);
        auth=FirebaseAuth.getInstance();

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String selection = adapterView.getItemAtPosition(i).toString();
        if(adapterView.getId() == R.id.branch)
            b = selection;
        else if(adapterView.getId() == R.id.year)
            y = selection;
        else if(adapterView.getId() == R.id.semester)
            s = selection;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}