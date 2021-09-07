package com.sscp.pechostelmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    TextInputLayout email, password;
    String username, pwd;
    FirebaseAuth auth;
    FirebaseUser fUser;
    Button login;
    Spinner selection;
    String loginType;
    boolean flag = false;
    boolean isExist = false;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        Initialize();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                username = email.getEditText().getText().toString();
                pwd = password.getEditText().getText().toString();

                if(username.trim().equals("") )
                    Toast.makeText(LoginActivity.this, "Enter email please ", Toast.LENGTH_SHORT).show();
                else if(pwd.trim().equals(""))
                    Toast.makeText(LoginActivity.this, "Enter password please ", Toast.LENGTH_SHORT).show();
                else
                    checkUserType(username, pwd);
            }
        });


        selection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                loginType = adapterView.getItemAtPosition(i).toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    private void Initialize() {
        email = findViewById(R.id.username);
        password = findViewById(R.id.pwd);
        login = findViewById(R.id.login_id);

        auth=FirebaseAuth.getInstance();
        selection = findViewById(R.id.spinner);
    }


    private void checkUserType(String em, String pass)
    {
        pd=new ProgressDialog(this);
        pd.setMessage("Please wait...");
        pd.setCancelable(false);
        pd.show();

        if(loginType.equals("Admin") && email.getEditText().getText().toString().equals("peckulfy111@gmail.com"))
            signInAdmin(em, pass);
        else if(loginType.equals("Warden"))
        {
            if(em.equals("peckulfy111@gmail.com")) {
                pd.dismiss();
                Toast.makeText(getApplicationContext(), "Sorry wrong option was choosen", Toast.LENGTH_SHORT).show();
            }
            else {
                checkExistence(em, pass);
            }
        }
        else{
            pd.dismiss();
            Toast.makeText(getApplicationContext(), "Sorry wrong option was choosen", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean wardenAdded(String em) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Warden");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snap:snapshot.getChildren()){

                    Warden warden = snap.getValue(Warden.class);
                    if(warden.getWarden_email().equals(em))
                        isExist = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return isExist;

    }

    private void signInAdmin(String em, String pass) {
        auth.signInWithEmailAndPassword(em,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    pd.dismiss();
                    Intent intent = new Intent(LoginActivity.this, AdminHomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    FirebaseAuthException e = (FirebaseAuthException )task.getException();
                    Toast.makeText(LoginActivity.this, "Login Failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            }
        });
    }

    private void signInWarden(String em, String pass) {
        auth.signInWithEmailAndPassword(em,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    pd.dismiss();
                    Intent intent = new Intent(LoginActivity.this, WardenHomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    FirebaseAuthException e = (FirebaseAuthException )task.getException();
                    Toast.makeText(LoginActivity.this, "Login Failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            }
        });
    }

    private void signUpWarden(String em, String pass) {
        auth.createUserWithEmailAndPassword(em, pass)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(getApplicationContext(), "SignUp Successfull as you are logged in for the first time", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, WardenHomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                        else{
                            FirebaseAuthException e = (FirebaseAuthException )task.getException();
                            Toast.makeText(LoginActivity.this, "Failed Registration: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            pd.dismiss();
                        }
                    }
                });
    }

    private boolean checkExistence(String em, String pass) {

        auth.fetchSignInMethodsForEmail(em)
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {

                        if(task.isSuccessful()){
                            if (task.getResult().getSignInMethods().isEmpty()){
                                if(wardenAdded(em))
                                    signUpWarden(em, pass);
                                else {
                                    email.setError("Sorry, you was not added by admin");
                                    pd.dismiss();
                                }
                            }
                            else {
                                signInWarden(em, pass);
                            }
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Warden Checking process failed. Please check your credentials", Toast.LENGTH_SHORT).show();
                            pd.dismiss();
                        }

                    }
                });
        return flag;
    }

    @Override
    protected void onStart() {
        super.onStart();
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if(fUser!=null)
        {
            if(fUser.getUid().equals("kRQVOjvMOLgGpF6yuUFHso6TzGh1"))
            {
                Intent intent = new Intent(LoginActivity.this, AdminHomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
            else{
                Intent intent = new Intent(LoginActivity.this, WardenHomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }

        }
    }
}

