package com.example.clarix.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.clarix.R;
import com.example.clarix.database_handlers.FirebaseManager;

public class SignUp extends AppCompatActivity {
    private EditText SignUpEmail, SignUpPassword, SignUpName, SignUpSurname;
    private Switch userTypeSwitch;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        userTypeSwitch = findViewById(R.id.signup_user_type_switch);

        SignUpEmail = findViewById(R.id.signup_email);
        SignUpPassword = findViewById(R.id.signup_password);
        SignUpName = findViewById(R.id.signup_name);
        SignUpSurname = findViewById(R.id.signup_surname);

        Button btnRegister = findViewById(R.id.btn_register);
        TextView switchToLogin = findViewById(R.id.go_login);

        firebaseManager = new FirebaseManager(this);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sign_up_email = SignUpEmail.getText().toString().trim();
                String sign_up_password = SignUpPassword.getText().toString().trim();
                String sign_up_name = SignUpName.getText().toString().trim();
                String sign_up_surname = SignUpSurname.getText().toString().trim();

                if (TextUtils.isEmpty(sign_up_email) || TextUtils.isEmpty(sign_up_password) || TextUtils.isEmpty(sign_up_name) || TextUtils.isEmpty(sign_up_surname)) {
                    Toast.makeText(SignUp.this, "All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                firebaseManager.registerUser(sign_up_email, sign_up_password, sign_up_name, sign_up_surname, userTypeSwitch.isChecked());
            }

        });

        switchToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LogIn.class);
                startActivity(intent);
                finish();
            }
        });
    }
}