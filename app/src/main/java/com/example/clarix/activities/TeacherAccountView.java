package com.example.clarix.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.clarix.R;
import com.example.clarix.data.classes.TeacherClass;
import com.example.clarix.database_handlers.FirebaseManager;

import java.util.List;

public class TeacherAccountView extends AppCompatActivity {

    private FirebaseManager manager;
    private TeacherClass teacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_account_view);

        teacher = (TeacherClass) getIntent().getSerializableExtra("teacher");
        manager = new FirebaseManager(this);

        ImageView profilePicture = findViewById(R.id.profile_picutre);
        Button reserveTermButton = findViewById(R.id.show_terms_calendar);
        TextView nameText = findViewById(R.id.name_text);
        TextView surnameText = findViewById(R.id.surname_text);
        RatingBar ratingBar = findViewById(R.id.ratingBar);
        TextView subjectListView = findViewById(R.id.subject_list_view);

        nameText.setText(teacher.getName());
        surnameText.setText(teacher.getSurname());
        ratingBar.setRating(teacher.getRate());

        // Load profile picture from URL using Glide
        if (teacher.getProfileImageUrl() != null && !teacher.getProfileImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(teacher.getProfileImageUrl())
                    .placeholder(R.drawable.annonym)
                    .into(profilePicture);
        } else {
            profilePicture.setImageResource(R.drawable.annonym);
        }

        ratingBar.setOnRatingBarChangeListener((ratingBar1, rating, fromUser) -> {
            if (fromUser) {
                int integerRating = Math.round(rating);
                manager.addRate(teacher.getId(), manager.getCurrentUser().getUid(), integerRating);
            }
        });

        List<String> subjects = teacher.getSubjects();
        for (int i = 0; i < subjects.size(); i++) {
            subjectListView.append(subjects.get(i));
            if (i < subjects.size() - 1) subjectListView.append(", ");
            if ((i + 1) % 3 == 0 && i < subjects.size() - 1) subjectListView.append("\n");
        }

        reserveTermButton.setOnClickListener(view -> {
            Intent intent = new Intent(TeacherAccountView.this, ReserveTermView.class);
            intent.putExtra("teacher", teacher);
            startActivity(intent);
        });
    }
}
