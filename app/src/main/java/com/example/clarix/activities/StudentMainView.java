package com.example.clarix.activities;



import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.clarix.database_handlers.FirebaseManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseUser;

import com.example.clarix.R;


public class StudentMainView extends AppCompatActivity {
    Button btnLogout;
    ImageButton btnMeetings, btnSearch;
    ImageView profilePic;
    TextView name;
    FirebaseUser user;
    private FirebaseManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);
        manager = new FirebaseManager(this);
        FirebaseApp.initializeApp(this);
        btnLogout = findViewById(R.id.btn_logout_student);
        btnMeetings = findViewById(R.id.meetingsBtn);
        btnSearch = findViewById(R.id.searchBtn);
        name = findViewById(R.id.user_name_student);
        user = manager.getCurrentUser();
        profilePic = findViewById(R.id.ProfilePicStudent);


        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), LogIn.class);
            startActivity(intent);
            finish();
        }

        profilePic.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ViewScheduleActivity.class);
            startActivity(intent);
        });
        btnLogout.setOnClickListener(v -> {
            manager.signOut();
            Intent intent = new Intent(getApplicationContext(), LogIn.class);
            startActivity(intent);
            finish();
        });

        btnMeetings.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ViewScheduleActivity.class);
            startActivity(intent);
        });
        btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ViewScheduleActivity.class);
            startActivity(intent);
        });}
    @Override
    protected void onStart() {
        super.onStart();
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), LogIn.class);
            startActivity(intent);
            finish();
        }
        manager.getUserData("name", data -> name.setText(data));

        manager.getUserData("surname", data -> name.setText(name.getText() + " " + data));
//
//        manager.getImage(manager.getCurrentUser().getUid(),
//                picture -> {
//                    if (picture == 0) {
//                        profilePic.setImageResource(R.drawable.annonym);
//                    } else {
//                        profilePic.setImageResource(picture);
//
//                    }
//                },
//                e -> {
//                });


    }
}