package com.example.clarix.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.*;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.clarix.R;
import com.example.clarix.database_handlers.FirebaseManager;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ManageProfileActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 1;

    private EditText nameField, surnameField, phoneField, bioField, rateField;
    private TextView emailView;
    private Spinner subjectSpinner;
    private FirebaseManager manager;
    private FirebaseUser user;
    private ImageView profileImage;
    private Uri selectedImageUri;
    private int fallbackImageRes = R.drawable.annonym;
    private List<String> subjects;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                        profileImage.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        Log.d("UPLOAD_URI", "Uri: " + selectedImageUri.toString());
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

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

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if (checkSelfPermission(Manifest.permission.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
//                requestPermissions(new String[]{Manifest.permission.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION}, 101);
//            }
//        }

        manager = new FirebaseManager(this);
        user = manager.getCurrentUser();

        nameField = findViewById(R.id.profile_name);
        surnameField = findViewById(R.id.profile_surname);
        emailView = findViewById(R.id.profile_email);
        phoneField = findViewById(R.id.profile_phone);
        bioField = findViewById(R.id.profile_bio);
        subjectSpinner = findViewById(R.id.profile_subject_spinner);
        rateField = findViewById(R.id.profile_rate);
        profileImage = findViewById(R.id.profile_image);

        Button updateBtn = findViewById(R.id.btn_update_profile);
        Button changePwdBtn = findViewById(R.id.btn_change_password);
        Button backBtn = findViewById(R.id.btn_back_home);

        // Load subjects from strings.xml
        subjects = Arrays.asList(getResources().getStringArray(R.array.subjects_array));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subjects);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectSpinner.setAdapter(adapter);

        if (user != null) {
            emailView.setText(user.getEmail());


            manager.getTeacherById(user.getUid(), teacher -> {
                if (teacher != null) {
                    nameField.setText(teacher.getName());
                    surnameField.setText(teacher.getSurname());
                    phoneField.setText(teacher.getPhoneNumber());
                    bioField.setText(teacher.getBio());
                    rateField.setText(String.valueOf(teacher.getPrice()));

                    if (teacher.getSubjects() != null && !teacher.getSubjects().isEmpty()) {
                        int index = subjects.indexOf(teacher.getSubjects().get(0));
                        if (index >= 0) subjectSpinner.setSelection(index);
                    }
                }
            });
        }

        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        updateBtn.setOnClickListener(v -> {
            String name = nameField.getText().toString().trim();
            String surname = surnameField.getText().toString().trim();
            String phone = phoneField.getText().toString().trim();
            String bio = bioField.getText().toString().trim();
            String subject = subjectSpinner.getSelectedItem().toString();
            String rateStr = rateField.getText().toString().trim();

            if (name.isEmpty() || surname.isEmpty() || phone.isEmpty() || bio.isEmpty() || rateStr.isEmpty()) {
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

            // Save basic info
            manager.updateTeacherProfile(user.getUid(), name, surname, phone, bio, subject, rate);

            // Save profile image if selected
            if (selectedImageUri != null) {
                manager.uploadProfileImage(user.getUid(), selectedImageUri, uri -> {
                    Toast.makeText(this, "Profile image uploaded", Toast.LENGTH_SHORT).show();
                });
            }
        });

        changePwdBtn.setOnClickListener(v ->
                Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show()
        );

        backBtn.setOnClickListener(v ->
                startActivity(new Intent(this, TeacherMainView.class))
        );
    }
}
