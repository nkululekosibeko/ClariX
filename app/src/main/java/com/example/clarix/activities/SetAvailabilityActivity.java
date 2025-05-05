package com.example.clarix.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.clarix.R;
import com.example.clarix.database_handlers.FirebaseManager;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SetAvailabilityActivity extends AppCompatActivity {
    private EditText dateField, startTimeField, endTimeField;
    private Button submitButton;
    private Calendar selectedDate;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_availability);

        firebaseManager = new FirebaseManager(this);

        dateField = findViewById(R.id.availability_date);
        startTimeField = findViewById(R.id.start_time);
        endTimeField = findViewById(R.id.end_time);
        submitButton = findViewById(R.id.btn_submit_availability);
        selectedDate = Calendar.getInstance();

        dateField.setOnClickListener(v -> showDatePicker());
        startTimeField.setOnClickListener(v -> showTimePicker(startTimeField));
        endTimeField.setOnClickListener(v -> showTimePicker(endTimeField));

        submitButton.setOnClickListener(v -> submitAvailability());
    }

    private void showDatePicker() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, day) -> {
            selectedDate.set(year, month, day);
            dateField.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.getTime()));
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));

        // Prevent past dates from being selected
        datePickerDialog.getDatePicker().setMinDate(now.getTimeInMillis());
        datePickerDialog.show();
    }


    private void showTimePicker(EditText target) {
        Calendar now = Calendar.getInstance();
        new TimePickerDialog(this, (view, hour, minute) -> {
            String time = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
            target.setText(time);
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show();
    }

    private void submitAvailability() {
        String date = dateField.getText().toString().trim();
        String start = startTimeField.getText().toString().trim();
        String end = endTimeField.getText().toString().trim();

        if (date.isEmpty() || start.isEmpty() || end.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String startDateTime = date + " " + start;
            String endDateTime = date + " " + end;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date startDate = sdf.parse(startDateTime);
            Date endDate = sdf.parse(endDateTime);

            if (startDate != null && endDate != null && startDate.before(endDate)) {
                Timestamp startTimestamp = new Timestamp(startDate);
                Timestamp endTimestamp = new Timestamp(endDate);

                firebaseManager.addAvailabilitySlot(
                        firebaseManager.getCurrentUser().getUid(),
                        startTimestamp,
                        endTimestamp
                );
                Toast.makeText(this, "Availability added", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Invalid time format", Toast.LENGTH_SHORT).show();
        }
    }
}
