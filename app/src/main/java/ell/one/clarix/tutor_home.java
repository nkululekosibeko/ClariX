package ell.one.clarix;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class tutor_home extends AppCompatActivity {

    private static final String TAG = "TutorHome";

    private TextView welcomeText;
    private Button btnEditProfile, btnSetAvailability, btnLogout, btnScheduleSession, btnViewBookedSessions;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tutor_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Firebase
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // UI Elements
        welcomeText = findViewById(R.id.welcomeText);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnSetAvailability = findViewById(R.id.btnSetAvailability);
        btnScheduleSession = findViewById(R.id.btnScheduleSession);
        btnViewBookedSessions = findViewById(R.id.btnViewBookedSessions);
        btnLogout = findViewById(R.id.btnLogout);

        // Load personalized greeting
        loadTutorName();

        btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(tutor_home.this, EditProfileActivity.class));
        });

        btnSetAvailability.setOnClickListener(v -> {
            startActivity(new Intent(tutor_home.this, set_availability.class));
        });

        btnScheduleSession.setOnClickListener(v -> {
            startActivity(new Intent(tutor_home.this, ViewScheduleActivity.class));
        });

        btnViewBookedSessions.setOnClickListener(v -> {
            startActivity(new Intent(tutor_home.this, TutorBookings.class));
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(tutor_home.this, LoginActivity.class));
            finish();
        });
    }

    private void loadTutorName() {
        if (currentUser == null) {
            Log.w(TAG, "User not logged in");
            return;
        }

        String uid = currentUser.getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String name = snapshot.getString("name");
                        welcomeText.setText("Welcome, " + (name != null ? name : "Tutor") + "!");
                    } else {
                        welcomeText.setText("Welcome, Tutor!");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch tutor name", e);
                    welcomeText.setText("Welcome, Tutor!");
                });
    }
}
