package com.example.clarix.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Calendar;

import com.example.clarix.R;
import com.example.clarix.data.classes.Term;
import com.example.clarix.database_handlers.FirebaseManager;


public class TeacherTermsView extends AppCompatActivity {
    private FirebaseManager manager;
    private ArrayList<Term> user_terms;
    private ArrayList<String> terms_hours;
    private int SelectedYear, SelectedMonth, SelectedDay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_terms);
        manager = new FirebaseManager(this);
        CalendarView calendarView = findViewById(R.id.calendarView);
        Button addButton = findViewById(R.id.addTermButton);

        user_terms = new ArrayList<>();
        terms_hours = new ArrayList<>();


        Calendar today = Calendar.getInstance();
        SelectedYear = today.get(Calendar.YEAR);
        SelectedMonth = today.get(Calendar.MONTH);
        SelectedDay = today.get(Calendar.DAY_OF_MONTH);
        manager.getTermsForTeacher(manager.getCurrentUser().getUid(), successListener);

        calendarView.setOnDateChangeListener((view, year, month, day) -> {
            SelectedYear = year;
            SelectedMonth = month;
            SelectedDay = day;
            terms_hours.clear();
            manager.getTermsForTeacher(manager.getCurrentUser().getUid(), successListener);
        });

        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherTermsView.this, AddingTermView.class);
            startActivity(intent);

    }
        );
    }

        private final OnSuccessListener<ArrayList<Term>> successListener = new OnSuccessListener<ArrayList<Term>>() {
            @Override
            public void onSuccess(ArrayList<Term> terms) {
                if (terms.isEmpty()) {
                    terms_hours.add("No terms");
                } else {
                    for (Term term : terms) {
                        if (term.checkDate(SelectedYear, SelectedMonth, SelectedDay)) {
                            user_terms.add(term);
                            terms_hours.add(term.getTimeAsString());
                        }
                    }

                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(TeacherTermsView.this, android.R.layout.simple_list_item_1, terms_hours);
                    ListView listView = findViewById(R.id.listView);
                    listView.setAdapter(arrayAdapter);
                }
            }
        };

        private final OnFailureListener failureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(TeacherTermsView.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        };
}

