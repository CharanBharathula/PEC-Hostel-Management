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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.persons,
                android.R.layout.simple_spinner_item);
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        auth=FirebaseAuth.getInstance();
        selection = findViewById(R.id.spinner);

        if(fUser!=null)
        {
            if(fUser.getUid().equals("kRQVOjvMOLgGpF6yuUFHso6TzGh1"))
            {
                Intent intent = new Intent(LoginActivity.this, AdminHomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }

        }

        email = findViewById(R.id.username);
        password = findViewById(R.id.pwd);
        login = findViewById(R.id.login_id);

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
                    authenticateUser(username, pwd);
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

    private void authenticateUser(final String em, final String pass)
    {
        final ProgressDialog pd=new ProgressDialog(this);
        pd.setMessage("Please wait...");
        pd.setCancelable(false);
        pd.show();

        auth.signInWithEmailAndPassword(em,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    pd.dismiss();
                    if(auth.getCurrentUser().getUid().equals("kRQVOjvMOLgGpF6yuUFHso6TzGh1"))
                    {
                        if(loginType.equals("Admin")){
                            Intent intent = new Intent(LoginActivity.this, AdminHomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Sorry you have choosen wrong option. Please try again!!", Toast.LENGTH_SHORT).show();
                        }

                    }
                    else if(loginType.equals("Warden"))
                    {
                        if(auth.getCurrentUser().getUid().equals("kRQVOjvMOLgGpF6yuUFHso6TzGh1")){
                            Toast.makeText(getApplicationContext(), "Sorry you have choosen wrong option. Please try again!!", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Intent intent = new Intent(LoginActivity.this, WardenHomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }
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
}