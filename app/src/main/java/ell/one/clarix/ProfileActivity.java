package ell.one.clarix;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ell.one.clarix.database_handlers.FirebaseManager;

public class ProfileActivity extends AppCompatActivity {
    TextView profileBio, profileSpecialization, profileRate;
    private String profile_bio, profile_specialization, profile_rate;


    TextView profileName, profileEmail, profilePhoneNo;
    private String profile_name, profile_email, profile_phone;
    TextView bookAppointment;
    Button editProfile, changePassword;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileBio = findViewById(R.id.profileBio);
        profileSpecialization = findViewById(R.id.profileSpecialization);
        profileRate = findViewById(R.id.profileRate);

        profileName = findViewById(R.id.profileName);
        profilePhoneNo = findViewById(R.id.profilePhone);
        profileEmail = findViewById(R.id.profileEmail);
        editProfile = findViewById(R.id.editButton);
        changePassword = findViewById(R.id.changePassword);
        bookAppointment = findViewById(R.id.book_app_text);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null){
            Toast.makeText(this, "Cannot get user details", Toast.LENGTH_LONG).show();
        }else {
//            checkEmailVarification(firebaseUser);
            showAllUserData();

        }

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ChangePasswordActivity.class));
            }
        });


        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
            }
        });

    }

    private void checkEmailVarification(FirebaseUser firebaseUser) {
        if (!firebaseUser.isEmailVerified()){
            displayDialogBox();
        }
    }

    private void displayDialogBox() {

//        set alert box
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setTitle("Verify Email");
        builder.setMessage("Please verify your Email to save your data.");

//        open email app
        builder.setPositiveButton("Verify", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
//        create dialog box
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void showAllUserData() {
        FirebaseManager firebaseManager = new FirebaseManager(this);
        firebaseManager.getUserProfile(new FirebaseManager.OnUserProfileRetrieved() {
            @Override
            public void onUserProfileLoaded(HelperClass profile) {
                if (profile != null) {
                    profile_name = profile.name;
                    profile_email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                    profile_phone = profile.phoneNo;

                    profile_bio = profile.bio;
                    profile_specialization = profile.specialization;
                    profile_rate = profile.rate;

                    profileName.setText(profile_name);
                    profileEmail.setText(profile_email);
                    profilePhoneNo.setText(profile_phone);

                    profileBio.setText(profile_bio);
                    profileSpecialization.setText(profile_specialization);
                    profileRate.setText("R " + profile_rate + " / hour");
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        inflate menu items
        getMenuInflater().inflate(R.menu.nav_menus, menu);
        return super.onCreateOptionsMenu(menu);
    }

//    on item selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int itemID = item.getItemId();

        if (itemID == R.id.nav_home){
            Intent intent = new Intent(ProfileActivity.this, tutor_home.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else if (itemID == R.id.nav_prof) {
            startActivity(new Intent(getIntent()));
        } else if (itemID == R.id.nav_password) {
            Intent intent = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        } else if (itemID == R.id.nav_logout) {
            Toast.makeText(this, "Logged out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}