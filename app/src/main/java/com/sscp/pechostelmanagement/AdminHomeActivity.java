package com.sscp.pechostelmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class AdminHomeActivity extends AppCompatActivity {

    ImageView gotoProfile, add_student, add_warden, check_attendance, search_student;
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
        search_student = findViewById(R.id.search_student);
        check_attendance = findViewById(R.id.check_attendance);

        add_student.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AdminHomeActivity.this, AddStudent.class));
            }
        });
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
        search_student.setOnClickListener(view -> startActivity(new Intent(AdminHomeActivity.this, SearchStudent.class)));
        check_attendance.setOnClickListener(view -> startActivity(new Intent(AdminHomeActivity.this, AttendanceDateDetails.class)));
        gotoProfile.setOnClickListener(view -> startActivity(new Intent(AdminHomeActivity.this, AdminProfileActivity.class)));

    }
}