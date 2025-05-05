package com.example.clarix.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import com.example.clarix.R;

import com.example.clarix.database_handlers.FirebaseManager;

public class SetAvailability extends AppCompatActivity {

    private Button btnPickDate, btnPickStartTime, btnPickEndTime, btnSubmit;
    private String selectedDate = null, startTime = null, endTime = null;

    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_availability);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firebaseManager = new FirebaseManager(this);

        // Bind buttons
        btnPickDate = findViewById(R.id.btnPickDate);
        btnPickStartTime = findViewById(R.id.btnPickStartTime);
        btnPickEndTime = findViewById(R.id.btnPickEndTime);
        btnSubmit = findViewById(R.id.btnSubmitSchedule);

        // Event Listeners
        btnPickDate.setOnClickListener(v -> showDatePicker());
        btnPickStartTime.setOnClickListener(v -> showTimePicker(true));
        btnPickEndTime.setOnClickListener(v -> showTimePicker(false));
        btnSubmit.setOnClickListener(v -> saveAvailability());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog picker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                Toast.makeText(this, "Only weekdays allowed (Mon–Fri)", Toast.LENGTH_SHORT).show();
            } else {
                selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                btnPickDate.setText("Date: " + selectedDate);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        picker.show();
    }

    private void showTimePicker(boolean isStart) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog picker = new TimePickerDialog(this, (view, hour, minute) -> {
            String time = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
            if (isStart) {
                startTime = time;
                btnPickStartTime.setText("Start: " + time);
            } else {
                endTime = time;
                btnPickEndTime.setText("End: " + time);
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        picker.show();
    }

    private void saveAvailability() {
        if (selectedDate == null || startTime == null || endTime == null) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Time logic validation
        try {
            Date start = timeFormat.parse(startTime);
            Date end = timeFormat.parse(endTime);

            if (start != null && end != null && !end.after(start)) {
                Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show();
                return;
            }

        } catch (ParseException e) {
            Toast.makeText(this, "Invalid time format", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        String dateKey = selectedDate.replace("/", "-"); // Firestore-safe key

        firebaseManager.saveAvailabilityForDay(dateKey, startTime, endTime, new FirebaseManager.AvailabilitySaveListener() {
            @Override
            public void onSuccess() {
                Log.d("Availability", "Saved for " + selectedDate);
                Toast.makeText(SetAvailability.this, "Availability saved", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Availability", "Failed to save", e);
                Toast.makeText(SetAvailability.this, "Error saving availability", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
