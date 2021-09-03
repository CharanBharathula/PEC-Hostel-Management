package com.sscp.pechostelmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class AdminProfileActivity extends AppCompatActivity {

    ImageView logout;
    EditText name, des, des_description;
    ProgressDialog pd;
    Button save;
    DatabaseReference ref;

    TextInputLayout email, mobile;
    String mob;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_admin_profile);

        email = findViewById(R.id.admin_em);
        name = findViewById(R.id.aname);
        mobile = findViewById(R.id.admin_mobile);
        des = findViewById(R.id.occupation);
        des_description = findViewById(R.id.workplace);
        save = findViewById(R.id.save);
        ref = FirebaseDatabase.getInstance().getReference("Admin");

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mob = mobile.getEditText().getText().toString();
                ref.child("mobile").setValue(mob).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(), "Mobile Number updated successfully", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        ArrayList<String> arr = new ArrayList<>();
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Admin admin = snapshot.getValue(Admin.class);

                name.setText(admin.getName());
                email.getEditText().setText(admin.getEmail());
                mobile.getEditText().setText(admin.getMobile());
                des.setText(snapshot.child("designation").getValue(String.class));
                des_description.setText(snapshot.child("des_description").getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Data load failed", Toast.LENGTH_SHORT).show();
            }
        });

        logout = findViewById(R.id.admin_logout);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(AdminProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

}