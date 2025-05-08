package ell.one.clarix.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

import ell.one.clarix.R;
import ell.one.clarix.database_handlers.FirebaseManager;

public class SignupActivity extends AppCompatActivity {
    private EditText fullNameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private Button signupButton;
    private ImageView topBackgroundImage;
    private TextView textViewLogin;
    private RadioGroup roleRadioGroup;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signupp);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signup_screen), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textViewLogin = findViewById(R.id.textViewLogin);
        textViewLogin.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        // UI elements
        fullNameEditText = findViewById(R.id.fullname_singup);
        emailEditText = findViewById(R.id.signup_email);
        passwordEditText = findViewById(R.id.signup_password);
        confirmPasswordEditText = findViewById(R.id.signup_conf_pass);
        roleRadioGroup = findViewById(R.id.role_radio_group);
        signupButton = findViewById(R.id.signup_button);


        signupButton.setOnClickListener(v -> {
            String fullName = fullNameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();

            int selectedRoleId = roleRadioGroup.getCheckedRadioButtonId();
            if (selectedRoleId == -1) {
                Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
                return;
            }

            String role = "guest"; // default
            if (selectedRoleId == R.id.radio_tutor) role = "tutor";
            else if (selectedRoleId == R.id.radio_tutee) role = "tutee";

            // Field validations
            if (TextUtils.isEmpty(fullName)) {
                fullNameEditText.setError("Full Name is required");
                return;
            }

            if (TextUtils.isEmpty(email)) {
                emailEditText.setError("Email is required");
                return;
            }

            if (TextUtils.isEmpty(password)) {
                passwordEditText.setError("Password is required");
                return;
            }

            if (password.length() < 6) {
                passwordEditText.setError("Password must be at least 6 characters");
                return;
            }

            if (!password.equals(confirmPassword)) {
                confirmPasswordEditText.setError("Passwords do not match");
                return;
            }

            // Register user with FirebaseManager
            FirebaseManager firebaseManager = new FirebaseManager(SignupActivity.this);
            firebaseManager.registerUser(email, password, fullName, role);
        });
    }
}