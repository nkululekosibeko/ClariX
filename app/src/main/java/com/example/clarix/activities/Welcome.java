package com.example.clarix.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.clarix.R;

public class Welcome extends AppCompatActivity {

    Button Sign_In, Sign_Up;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);

        // ✅ Fix: Use the correct ID and call after setContentView
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.welcome_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize buttons
        Sign_In = findViewById(R.id.wlcm_signin_button);
        Sign_Up = findViewById(R.id.wlcm_signup_button);

        // Set listeners
        Sign_In.setOnClickListener(v -> {
            Intent intent = new Intent(Welcome.this, LoginView.class);
            startActivity(intent);
        });

        Sign_Up.setOnClickListener(v -> {
            Intent intent = new Intent(Welcome.this, RegisterView.class);
            startActivity(intent);
        });
    }
}
