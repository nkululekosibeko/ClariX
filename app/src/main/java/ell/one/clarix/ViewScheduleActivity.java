package ell.one.clarix;

import android.os.Bundle;

import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ViewScheduleActivity extends AppCompatActivity {

        private RecyclerView recyclerView;
        private FirebaseFirestore db;
        private FirebaseUser user;
        private List<Map<String, Object>> scheduleList = new ArrayList<>();
        private List<String> docKeys = new ArrayList<>();
        private ell.one.clarix.data_adapters.AvailabilityAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_schedule);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.availabilityRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        adapter = new ell.one.clarix.data_adapters.AvailabilityAdapter(scheduleList, docKeys, this::deleteAvailability);
        recyclerView.setAdapter(adapter);

        loadAvailability();
    }

        private void loadAvailability() {
            if (user == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("users")
                    .document(user.getUid())
                    .collection("availability")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        scheduleList.clear();
                        docKeys.clear();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            scheduleList.add(doc.getData());
                            docKeys.add(doc.getId());
                        }
                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firebase", "Failed to load availability", e);
                        Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show();
                    });
        }

        private void deleteAvailability(String docId) {
            db.collection("users")
                    .document(user.getUid())
                    .collection("availability")
                    .document(docId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                        loadAvailability();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show();
                    });
        }
    }
