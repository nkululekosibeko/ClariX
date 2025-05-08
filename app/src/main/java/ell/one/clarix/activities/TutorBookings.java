package ell.one.clarix.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ell.one.clarix.R;
import ell.one.clarix.data_adapters.BookingAdapter;
import ell.one.clarix.models.BookingModel;

public class TutorBookings extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private RecyclerView recyclerView;
    private BookingAdapter adapter;
    private final List<BookingModel> bookings = new ArrayList<>();
    private Button btnBackToTutorHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(ell.one.clarix.R.layout.activity_tutor_bookings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(ell.one.clarix.R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(ell.one.clarix.R.id.tutorBookingsRecycler);
        btnBackToTutorHome = findViewById(R.id.btnBackToTutorHome);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new BookingAdapter(
                bookings,
                true,
                null,
                this::updateBookingStatus
        );
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        loadTutorBookings();

        btnBackToTutorHome.setOnClickListener(v -> {
            startActivity(new Intent(this, tutor_home.class));
            finish();
        });
    }

    private void loadTutorBookings() {
        if (currentUser == null) return;

        db.collection("bookings")
                .whereEqualTo("tutorId", currentUser.getUid())
                .get()
                .addOnSuccessListener(query -> {
                    bookings.clear();
                    for (QueryDocumentSnapshot bookingDoc : query) {
                        String bookingId = bookingDoc.getId();
                        String tuteeId = bookingDoc.getString("tuteeId");
                        String date = bookingDoc.getString("date");
                        String startTime = bookingDoc.getString("startTime");
                        String endTime = bookingDoc.getString("endTime");
                        String status = bookingDoc.getString("status");
                        String meetingLink = bookingDoc.getString("meetingLink");

                        if (tuteeId != null) {
                            db.collection("users").document(tuteeId).get()
                                    .addOnSuccessListener(tuteeDoc -> {
                                        String tuteeName = tuteeDoc.getString("name");
                                        bookings.add(new BookingModel(
                                                bookingId,
                                                currentUser.getUid(),
                                                null,
                                                tuteeId,
                                                tuteeName != null ? tuteeName : "Tutee",
                                                date,
                                                startTime,
                                                endTime,
                                                status,
                                                meetingLink
                                        ));
                                        adapter.notifyDataSetChanged();
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("TutorBookings", "Error fetching bookings", e);
                    Toast.makeText(this, "Failed to load tutor bookings", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateBookingStatus(BookingModel booking, String newStatus) {
        if (booking.getBookingId() == null || currentUser == null) return;

        if (!"Confirmed".equalsIgnoreCase(newStatus)) {
            // Update status and remove meeting link
            Map<String, Object> updateMap = new HashMap<>();
            updateMap.put("status", newStatus);
            updateMap.put("meetingLink", FieldValue.delete());

            db.collection("bookings").document(booking.getBookingId())
                    .update(updateMap)
                    .addOnSuccessListener(unused -> {
                        booking.setStatus(newStatus);
                        booking.setMeetingLink(null);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(this, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("TutorBookings", "Status update failed", e);
                        Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show();
                    });

        } else {
            // Confirm the booking using stored meeting link
            String meetingLink = booking.getMeetingLink();

            if (meetingLink == null || meetingLink.isEmpty()) {
                Toast.makeText(this, "No meeting link available to confirm this booking.", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> updateMap = new HashMap<>();
            updateMap.put("status", "Confirmed");
            updateMap.put("meetingLink", meetingLink);

            db.collection("bookings").document(booking.getBookingId())
                    .update(updateMap)
                    .addOnSuccessListener(unused -> {
                        // Refresh everything from Firestore to avoid stale UI
                        Toast.makeText(this, "Booking confirmed", Toast.LENGTH_SHORT).show();
                        loadTutorBookings();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("TutorBookings", "Error confirming booking", e);
                        Toast.makeText(this, "Failed to confirm booking", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
