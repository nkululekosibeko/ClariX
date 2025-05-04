package com.example.clarix.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.clarix.R;
import com.example.clarix.database_handlers.FirebaseManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;



public class LogIn extends AppCompatActivity {
    private FirebaseManager manager;
    private EditText LogInEmail, LogInPassword;
    private FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            manager.getUserData("userType", data -> {
                if (!TextUtils.isEmpty(data)) {
                    // Redirect based on user type
                    if ("student".equals(data)) {
                        Intent intent = new Intent(LogIn.this, StudentMainView.class);
                        startActivity(intent);
                    } else if ("teacher".equals(data)) {
                        Intent intent = new Intent(LogIn.this, TeacherMainView.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(LogIn.this, "User Unkown, do register with us", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                } else {
                    Toast.makeText(LogIn.this, "User type not found", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        manager = new FirebaseManager(this);

        LogInEmail = findViewById(R.id.login_email);
        LogInPassword = findViewById(R.id.login_password);

        TextView switchToRegister = findViewById(R.id.go_register);
        Button btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(v -> {
            String login_email, login_password;

            login_email = String.valueOf(LogInEmail.getText());
            login_password = String.valueOf(LogInPassword.getText());

            if (TextUtils.isEmpty(login_email)) {
                Toast.makeText(LogIn.this, "Enter email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(login_password)) {
                Toast.makeText(LogIn.this, "Enter password", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(login_email, login_password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            manager.getUserData("userType", data -> {
                                if (!TextUtils.isEmpty(data)) {
                                    // Redirect based on user type
                                    if ("student".equals(data)) {
                                        Intent intent = new Intent(LogIn.this, StudentMainView.class);
                                        startActivity(intent);
                                    } else if ("teacher".equals(data)) {
                                        Intent intent = new Intent(LogIn.this, TeacherMainView.class);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(LogIn.this, "Unknown user type", Toast.LENGTH_SHORT).show();
                                    }
                                    finish();
                                } else {
                                    Toast.makeText(LogIn.this, "User type not found", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(LogIn.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        switchToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SignUp.class);
            startActivity(intent);
            finish();
        });
    }
}