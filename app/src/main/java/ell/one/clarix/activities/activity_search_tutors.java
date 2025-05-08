package ell.one.clarix.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import ell.one.clarix.R;
import ell.one.clarix.models.TutorModel;
import ell.one.clarix.data_adapters.TutorAdapter;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class activity_search_tutors extends AppCompatActivity {

    private Spinner specializationFilter;
    private EditText priceFilter;
    private Button searchButton;
    private RecyclerView tutorRecyclerView;
    private TutorAdapter tutorAdapter;
    private List<TutorModel> tutorList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_tutors);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        specializationFilter = findViewById(R.id.specializationSpinner);
        priceFilter = findViewById(R.id.priceEditText);
        searchButton = findViewById(R.id.searchButton);
        tutorRecyclerView = findViewById(R.id.tutorRecyclerView);

        // Firestore
        db = FirebaseFirestore.getInstance();

        // RecyclerView setup
        tutorList = new ArrayList<>();
        tutorAdapter = new TutorAdapter(tutorList, tutor -> {
            Intent intent = new Intent(activity_search_tutors.this, activity_tutor_detail.class);
            intent.putExtra("tutorId", tutor.getTutorId());
            intent.putExtra("name", tutor.getName());
            intent.putExtra("specialization", tutor.getSpecialization());
            intent.putExtra("rate", tutor.getRate());
            intent.putExtra("bio", tutor.getBio());
            startActivity(intent);
        });

        tutorRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tutorRecyclerView.setAdapter(tutorAdapter);

        // Spinner setup
        ArrayAdapter<CharSequence> specializationAdapter = ArrayAdapter.createFromResource(
                this, R.array.specializations_array, android.R.layout.simple_spinner_item);
        specializationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        specializationFilter.setAdapter(specializationAdapter);

        // Load initial data
        loadTutors(null, null);

        // Handle Search
        searchButton.setOnClickListener(v -> {
            String selectedSpec = specializationFilter.getSelectedItem().toString();
            String maxPriceStr = priceFilter.getText().toString().trim();
            Double maxPrice = null;

            if (!TextUtils.isEmpty(maxPriceStr)) {
                try {
                    maxPrice = Double.parseDouble(maxPriceStr);
                } catch (NumberFormatException ignored) {}
            }

            loadTutors(selectedSpec.equals("All") ? null : selectedSpec, maxPrice);
        });
    }

    private void loadTutors(String specialization, Double maxPrice) {
        db.collection("users")
                .whereEqualTo("role", "tutor")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        tutorList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String name = doc.getString("name");
                            String bio = doc.getString("bio");
                            String spec = doc.getString("specialization");
                            String rateStr = doc.getString("rate");

                            if (name != null && spec != null && rateStr != null) {
                                double rate;
                                try {
                                    rate = Double.parseDouble(rateStr);
                                } catch (NumberFormatException e) {
                                    continue;
                                }

                                boolean matchesSpec = specialization == null || spec.equalsIgnoreCase(specialization);
                                boolean matchesRate = maxPrice == null || rate <= maxPrice;

                                if (matchesSpec && matchesRate) {
                                    tutorList.add(new TutorModel(doc.getId(), name, spec, rateStr, bio));
                                }
                            }
                        }
                        tutorAdapter.notifyDataSetChanged();
                    }
                });
    }
}
