package com.example.clarix.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.clarix.R;
import com.example.clarix.database_handlers.FirebaseManager;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterView extends AppCompatActivity {
    private TextInputEditText editTextEmail, editTextPassword, editTextName, editTextSurname;
    private Switch userTypeSwitch;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        userTypeSwitch = findViewById(R.id.userTypeSwitch);
        editTextName = findViewById(R.id.name);
        editTextSurname = findViewById(R.id.surname);
        Button btnRegister = findViewById(R.id.btn_register);
        TextView switchToLogin = findViewById(R.id.go_login);

        firebaseManager = new FirebaseManager(this);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                String name = editTextName.getText().toString().trim();
                String surname = editTextSurname.getText().toString().trim();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(name) || TextUtils.isEmpty(surname)) {
                    Toast.makeText(RegisterView.this, "All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                firebaseManager.registerUser(email, password, name, surname, userTypeSwitch.isChecked());
            }

        });

        switchToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginView.class);
                startActivity(intent);
                finish();
            }
        });
    }
}