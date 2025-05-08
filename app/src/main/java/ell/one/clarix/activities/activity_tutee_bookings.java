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

import ell.one.clarix.models.BookingModel;
import ell.one.clarix.R;
import ell.one.clarix.data_adapters.BookingAdapter;

public class activity_tutee_bookings extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private RecyclerView recyclerView;
    private BookingAdapter adapter;
    private List<BookingModel> bookings = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(ell.one.clarix.R.layout.activity_tutee_bookings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(ell.one.clarix.R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(ell.one.clarix.R.id.tuteeBookingsRecycler);
        Button btnBackToHome = findViewById(R.id.btnBackToHome);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookingAdapter(
                bookings,
                false, // isTutor = false
                this::cancelBooking,
                null // no status change listener for tutees
        );
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        loadBookings();

        btnBackToHome.setOnClickListener(v -> {
            startActivity(new Intent(this, tutee_home.class));
            finish();
        });
    }

    private void loadBookings() {
        if (currentUser == null) return;

        db.collection("bookings")
                .whereEqualTo("tuteeId", currentUser.getUid())
                .get()
                .addOnSuccessListener(query -> {
                    bookings.clear();
                    for (QueryDocumentSnapshot bookingDoc : query) {
                        String tutorId = bookingDoc.getString("tutorId");
                        String tuteeId = bookingDoc.getString("tuteeId");
                        String date = bookingDoc.getString("date");
                        String startTime = bookingDoc.getString("startTime");
                        String endTime = bookingDoc.getString("endTime");
                        String status = bookingDoc.getString("status");
                        String meetingLink = bookingDoc.getString("meetingLink");

                        if (tutorId == null || tuteeId == null) continue;

                        db.collection("users").document(tutorId).get().addOnSuccessListener(tutorDoc -> {
                            String tutorName = tutorDoc.getString("name");

                            db.collection("users").document(tuteeId).get().addOnSuccessListener(tuteeDoc -> {
                                String tuteeName = tuteeDoc.getString("name");

                                bookings.add(new BookingModel(
                                        bookingDoc.getId(),
                                        tutorId,
                                        tutorName != null ? tutorName : "Tutor",
                                        tuteeId,
                                        tuteeName != null ? tuteeName : "You",
                                        date,
                                        startTime,
                                        endTime,
                                        status,
                                        meetingLink
                                ));
                                adapter.notifyDataSetChanged();
                            });
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("TuteeBookings", "Failed to load bookings", e);
                    Toast.makeText(this, "Error loading bookings", Toast.LENGTH_SHORT).show();
                });
    }

    private void cancelBooking(BookingModel booking) {
        db.collection("bookings").document(booking.getBookingId()).delete()
                .addOnSuccessListener(unused -> {
                    // Restore availability
                    Map<String, Object> restoredSlot = new HashMap<>();
                    restoredSlot.put("date", booking.getDate());
                    restoredSlot.put("startTime", booking.getStartTime());
                    restoredSlot.put("endTime", booking.getEndTime());
                    restoredSlot.put("timestamp", FieldValue.serverTimestamp());

                    db.collection("users").document(booking.getTutorId())
                            .collection("availability")
                            .add(restoredSlot)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Booking cancelled & slot restored", Toast.LENGTH_SHORT).show();
                                loadBookings();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Cancelled but failed to restore slot", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to cancel booking", Toast.LENGTH_SHORT).show());
    }
}
