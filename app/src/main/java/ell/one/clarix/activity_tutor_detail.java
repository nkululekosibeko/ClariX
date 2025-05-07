package ell.one.clarix;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class activity_tutor_detail extends AppCompatActivity {

    private TextView nameText, specializationText, rateText, bioText;
    private Button btnGoBack, btnBookNow;
    private String tutorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tutor_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        nameText = findViewById(R.id.detailTutorName);
        specializationText = findViewById(R.id.detailTutorSpecialization);
        rateText = findViewById(R.id.detailTutorRate);
        bioText = findViewById(R.id.detailTutorBio);
        btnGoBack = findViewById(R.id.btnGoBack);
        btnBookNow = findViewById(R.id.btnBookNow);

        // Get intent data
        Intent intent = getIntent();
        tutorId = intent.getStringExtra("tutorId");
        String name = intent.getStringExtra("name");
        String specialization = intent.getStringExtra("specialization");
        String rate = intent.getStringExtra("rate");
        String bio = intent.getStringExtra("bio");

        // Populate UI
        if (name != null) nameText.setText(name);
        if (specialization != null) specializationText.setText("Specialization: " + specialization);
        if (rate != null) rateText.setText("Rate: R" + rate + "/hr");
        if (bio != null) bioText.setText(bio);

        // Back button logic
        btnGoBack.setOnClickListener(v -> finish());

        // Book Now button logic
        btnBookNow.setOnClickListener(v -> {
            if (tutorId != null && !tutorId.isEmpty()) {
                Intent bookingIntent = new Intent(activity_tutor_detail.this, activity_book_session.class);
                bookingIntent.putExtra("tutorId", tutorId);
                startActivity(bookingIntent);
            } else {
                Toast.makeText(this, "Tutor ID not available", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
