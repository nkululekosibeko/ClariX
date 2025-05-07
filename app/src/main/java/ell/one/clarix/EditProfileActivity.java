package ell.one.clarix;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import ell.one.clarix.database_handlers.FirebaseManager;

public class EditProfileActivity extends AppCompatActivity {

    EditText editName, editEmailText, editPhoneNo, editBio, editRate;
    Spinner specializationSpinner;
    Button saveButton, deleteButton;

    FirebaseAuth firebaseAuth;
    FirebaseManager firebaseManager;
    ArrayAdapter<CharSequence> specializationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Bind views
        editName = findViewById(R.id.editName);
        editEmailText = findViewById(R.id.editEmail);
        editPhoneNo = findViewById(R.id.editPhone);
        editBio = findViewById(R.id.editBio);
        specializationSpinner = findViewById(R.id.specializationSpinner);
        editRate = findViewById(R.id.editRate);
        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.deleteButton);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseManager = new FirebaseManager(this);
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        // Spinner setup
        specializationAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.specializations_array,
                android.R.layout.simple_spinner_item
        );
        specializationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        specializationSpinner.setAdapter(specializationAdapter);

        if (firebaseUser != null) {
            showData();
        }

        saveButton.setOnClickListener(v -> updateUserProfile(firebaseUser));
        deleteButton.setOnClickListener(v -> deleteUser(firebaseUser));
    }

    private void showData() {
        firebaseManager.getUserProfile(profile -> {
            if (profile != null) {
                editName.setText(profile.getName());
                editEmailText.setText(firebaseAuth.getCurrentUser().getEmail());
                editPhoneNo.setText(profile.getPhoneNo());
                editBio.setText(profile.getBio());
                editRate.setText(profile.getRate());

                String currentSpecialization = profile.getSpecialization();
                int position = specializationAdapter.getPosition(currentSpecialization);
                if (position >= 0) {
                    specializationSpinner.setSelection(position);
                }
            } else {
                Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserProfile(FirebaseUser firebaseUser) {
        String nameUser = editName.getText().toString().trim();
        String emailUser = editEmailText.getText().toString().trim();
        String phoneUser = editPhoneNo.getText().toString().trim();
        String bioUser = editBio.getText().toString().trim();
        String specializationUser = specializationSpinner.getSelectedItem().toString();
        String rateUser = editRate.getText().toString().trim();

        if (TextUtils.isEmpty(nameUser)) {
            editName.setError("Name is required");
            editName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(emailUser)) {
            editEmailText.setError("Email is required");
            editEmailText.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(emailUser).matches()) {
            editEmailText.setError("Invalid email format");
            editEmailText.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(phoneUser)) {
            editPhoneNo.setError("Phone number is required");
            editPhoneNo.requestFocus();
            return;
        }
        if (phoneUser.length() != 10) {
            editPhoneNo.setError("Phone number must be 10 digits");
            editPhoneNo.requestFocus();
            return;
        }

        HelperClass updatedProfile = new HelperClass(nameUser, emailUser, phoneUser, bioUser, specializationUser, rateUser);

        firebaseManager.updateUserProfile(updatedProfile, new FirebaseManager.OnProfileUpdateListener() {
            @Override
            public void onUpdateSuccess() {
                UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                        .setDisplayName(nameUser)
                        .build();
                firebaseUser.updateProfile(userProfileChangeRequest);

                Toast.makeText(EditProfileActivity.this, "Profile updated", Toast.LENGTH_LONG).show();
                startActivity(new Intent(EditProfileActivity.this, tutor_home.class));
                finish();
            }

            @Override
            public void onUpdateFailure(Exception e) {
                Toast.makeText(EditProfileActivity.this, "Could not update profile", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void deleteUser(FirebaseUser firebaseUser) {
        String uid = firebaseUser.getUid();

        firebaseManager.deleteUserProfile(uid, new FirebaseManager.OnDeleteListener() {
            @Override
            public void onDeleteSuccess() {
                firebaseUser.delete().addOnSuccessListener(unused -> {
                    firebaseAuth.signOut();
                    Toast.makeText(EditProfileActivity.this, "Profile deleted", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(EditProfileActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }).addOnFailureListener(e -> {
                    Toast.makeText(EditProfileActivity.this, "Failed to delete Firebase Auth user", Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onDeleteFailure(Exception e) {
                Toast.makeText(EditProfileActivity.this, "Failed to delete user profile", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nav_menus, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemID = item.getItemId();

        if (itemID == R.id.nav_home) {
            startActivity(new Intent(EditProfileActivity.this, tutor_home.class));
            finish();
        } else if (itemID == R.id.nav_prof) {
            startActivity(new Intent(getIntent()));
        } else if (itemID == R.id.nav_logout) {
            firebaseAuth.signOut();
            Toast.makeText(this, "Logged out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(EditProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
