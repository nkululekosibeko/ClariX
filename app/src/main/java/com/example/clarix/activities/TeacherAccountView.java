package com.example.clarix.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import com.example.clarix.R;
import com.example.clarix.data.classes.TeacherClass;
import com.example.clarix.database_handlers.FirebaseManager;

public class TeacherAccountView extends AppCompatActivity {

    private FirebaseManager manager;
    private TeacherClass teacher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_account_view);

        Intent receivedIntent = getIntent();
        teacher = (TeacherClass) receivedIntent.getExtras().get("teacher");

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
        profilePicture.setImageResource(teacher.getPicture());
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                int integerRating = Math.round(rating);

                manager.addRate(teacher.getId(), manager.getCurrentUser().getUid(), integerRating);
            }
        });

        List<String> subjects = teacher.getSubjects();

        for (int i = 0; i < subjects.size(); i++) {
            subjectListView.append(subjects.get(i));
            if (i < subjects.size() - 1) {
                subjectListView.append(", ");
            }
            if ((i + 1) % 3 == 0 && i < subjects.size() - 1) {
                subjectListView.append("\n");
            }
        }

        reserveTermButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TeacherAccountView.this, ReserveTermView.class);
                intent.putExtra("teacher", teacher);
                startActivity(intent);
            }
        });
    }
}
