package com.example.clarix.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.example.clarix.R;
import com.example.clarix.data.classes.TeacherClass;
import com.example.clarix.data.classes.Term;
import com.example.clarix.database_handlers.FirebaseManager;
import com.example.clarix.database_handlers.OnDataRetrievedListener;

public class ReserveTermView extends AppCompatActivity {
    private FirebaseManager manager;
    private List<Term> termList;
    private ArrayList<String> termsHours;
    private TeacherClass teacher;
    private String studentName;

    private ArrayAdapter<String> arrayAdapter;
    private int selectedYear, selectedMonth, selectedDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve_term_view);

        Intent receivedIntent = getIntent();
        teacher = (TeacherClass) receivedIntent.getExtras().get("teacher");

        manager = new FirebaseManager(this);
        termsHours = new ArrayList<>();

        CalendarView calendarView = findViewById(R.id.calendarView2);
        Calendar today = Calendar.getInstance();
        selectedYear = today.get(Calendar.YEAR);
        selectedMonth = today.get(Calendar.MONTH);
        selectedDay = today.get(Calendar.DAY_OF_MONTH);

        calendarView.setOnDateChangeListener((view, year, month, day) -> {
            selectedYear = year;
            selectedMonth = month;
            selectedDay = day;
            manager.getTermsForTeacher(teacher.getId(), successListener);
        });

        termList = new ArrayList<>();
        manager.getUserData("name", new OnDataRetrievedListener() {
            @Override
            public void onDataRetrieved(String data) {
                studentName = data;
            }
        });
    }

    private void showConfirmationDialog(final Term selectedTerm) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Reservation");
        builder.setMessage("Do you want to reserve the term at " + selectedTerm.getTimeAsString() + "?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                manager.addMeetingForTeacherAndStudent(teacher.getId(), manager.getCurrentUser().getUid(), selectedTerm.getTimestamp(), selectedTerm.getLink(), teacher.getName(), studentName);
                Toast.makeText(ReserveTermView.this, "Reservation confirmed", Toast.LENGTH_SHORT).show();
                termsHours.remove(selectedTerm.getTimeAsString());
                termList.remove(selectedTerm);
                arrayAdapter.notifyDataSetChanged();
                finish();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private final OnSuccessListener<ArrayList<Term>> successListener = new OnSuccessListener<ArrayList<Term>>() {
        @Override
        public void onSuccess(ArrayList<Term> terms) {
            termList.clear();
            termsHours.clear();

            for (Term term : terms) {
                if (term.checkDate(selectedYear, selectedMonth, selectedDay) && !term.isBooked()) {
                    termList.add(term);
                    termsHours.add(term.getTimeAsString());
                }
            }

            arrayAdapter = new ArrayAdapter<>(ReserveTermView.this, android.R.layout.simple_list_item_1, termsHours);
            ListView listView = findViewById(R.id.availableTermsListView);
            listView.setAdapter(arrayAdapter);

            listView.setOnItemClickListener((parent, view, position, id) -> {
                Term selectedTerm = termList.get(position);
                showConfirmationDialog(selectedTerm);
            });
        }
    };

    private final OnFailureListener failureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            Toast.makeText(ReserveTermView.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    };
}
