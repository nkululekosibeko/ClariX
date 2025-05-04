package com.example.clarix.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.clarix.R;
import com.example.clarix.data.classes.TeacherClass;
import com.example.clarix.database_handlers.FirebaseManager;
import com.google.firebase.auth.FirebaseUser;

public class TeacherMainView extends AppCompatActivity {

    Button btnLogout;
    ImageButton btnTeacherMeetings, btnTerms, btnTeacherSubjects, btnPrice;
    TextView credentials;
    FirebaseUser user;
    private ImageView profilePictureView;
    private TeacherClass teacher_object;
    private FirebaseManager manager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);
        manager = new FirebaseManager(this);
        btnLogout = findViewById(R.id.btn_logout);
        btnTeacherMeetings = findViewById(R.id.btnTeacherMeetings);
        btnTerms = findViewById(R.id.btnTeacherTerms);
        btnTeacherSubjects = findViewById(R.id.btnSubjects);
        btnPrice = findViewById(R.id.btnPrice);
        credentials = findViewById(R.id.credentials);
        user = manager.getCurrentUser();
        profilePictureView = findViewById(R.id.profilePictureTeacher);

        profilePictureView.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SelectProfilePictureView.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            manager.signOut();
            Intent intent = new Intent(getApplicationContext(), LogIn.class);
            startActivity(intent);
            finish();
        });
        btnTeacherMeetings.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MeetingsView.class);
            startActivity(intent);
        });
        btnTerms.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), TeacherTermsView.class);
            startActivity(intent);
        });
        btnTeacherSubjects.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SubjectView.class);
            startActivity(intent);
        });
        btnPrice.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(TeacherMainView.this);
            builder.setTitle("Enter Price");

            final EditText input = new EditText(TeacherMainView.this);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            builder.setView(input);

            builder.setPositiveButton("OK", (dialog, which) -> {
                String enteredValue = input.getText().toString();
                try {
                    int priceValue = Integer.parseInt(enteredValue);
                    manager.updatePrice(manager.getCurrentUser().getUid(), priceValue);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();
        });


        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), LogIn.class);
            startActivity(intent);
            finish();
        }

    }
    @Override
    protected void onStart() {
        super.onStart();
        manager.getTeacherById(manager.getCurrentUser().getUid(), teacher -> {
            if (teacher != null) {
                teacher_object = teacher;
                credentials.setText(teacher_object.getName() + " " + teacher_object.getSurname());
                profilePictureView.setImageResource(teacher_object.getPicture());


            } else {
                Intent intent = new Intent(getApplicationContext(), LogIn.class);
                startActivity(intent);
                finish();
            }
        });


    }
}