package com.example.clarix.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.clarix.R;
import com.example.clarix.database_handlers.FirebaseManager;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class ManageProfileActivity extends AppCompatActivity {

    private EditText nameField, phoneField, bioField, rateField;
    private TextView emailView;
    private Spinner subjectSpinner;
    private FirebaseManager manager;
    private FirebaseUser user;
    private ImageView profileImage;
    private int currentImageResource = R.drawable.annonym;
    private List<String> subjects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        manager = new FirebaseManager(this);
        user = manager.getCurrentUser();

        nameField = findViewById(R.id.profile_name);
        emailView = findViewById(R.id.profile_email);
        phoneField = findViewById(R.id.profile_phone);
        bioField = findViewById(R.id.profile_bio);
        subjectSpinner = findViewById(R.id.profile_subject_spinner);
        rateField = findViewById(R.id.profile_rate);
        profileImage = findViewById(R.id.profile_image);

        Button updateBtn = findViewById(R.id.btn_update_profile);
        Button changePwdBtn = findViewById(R.id.btn_change_password);
        Button backBtn = findViewById(R.id.btn_back_home);

        // Subject dropdown
        subjects = Arrays.asList("Mathematics", "Physics", "Chemistry", "Biology", "English", "Computer Science");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subjects);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectSpinner.setAdapter(adapter);

        if (user != null) {
            emailView.setText(user.getEmail());

            // Load profile picture
            manager.getImage(user.getUid(),
                    image -> {
                        currentImageResource = image;
                        profileImage.setImageResource(image);
                    },
                    e -> Toast.makeText(this, "Failed to load profile image", Toast.LENGTH_SHORT).show()
            );

            // Load other profile info
            manager.getTeacherById(user.getUid(), teacher -> {
                if (teacher != null) {
                    nameField.setText(teacher.getName());
                    phoneField.setText(teacher.getPhoneNumber());
                    bioField.setText(teacher.getBio());
                    rateField.setText(String.valueOf(teacher.getPrice()));

                    if (teacher.getSubjects() != null && !teacher.getSubjects().isEmpty()) {
                        String subject = teacher.getSubjects().get(0);
                        int index = subjects.indexOf(subject);
                        if (index >= 0) subjectSpinner.setSelection(index);
                    }
                }
            });
        }

        // Cycle profile images on click
        profileImage.setOnClickListener(v -> {
            int[] images = {
                    R.drawable.men1, R.drawable.men2, R.drawable.women1,
                    R.drawable.women2, R.drawable.boy1, R.drawable.boy2,
                    R.drawable.girl1, R.drawable.girl2, R.drawable.cat,
                    R.drawable.monkey, R.drawable.seal, R.drawable.annonym
            };
            int index = Arrays.asList(images).indexOf(currentImageResource);
            index = (index + 1) % images.length;
            currentImageResource = images[index];
            profileImage.setImageResource(currentImageResource);
        });

        // Update Profile
        updateBtn.setOnClickListener(v -> {
            String name = nameField.getText().toString().trim();
            String phone = phoneField.getText().toString().trim();
            String bio = bioField.getText().toString().trim();
            String subject = subjectSpinner.getSelectedItem().toString();
            String rateStr = rateField.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty() || bio.isEmpty() || rateStr.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int rate;
            try {
                rate = Integer.parseInt(rateStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid rate", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save profile image
            manager.setImage(user.getUid(), currentImageResource);

            // Save profile details
            manager.updateTeacherProfile(
                    user.getUid(),
                    name,
                    phone,
                    bio,
                    subject,
                    rate,
                    currentImageResource
            );
        });

        changePwdBtn.setOnClickListener(v ->
                Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show()
        );

    }
}
