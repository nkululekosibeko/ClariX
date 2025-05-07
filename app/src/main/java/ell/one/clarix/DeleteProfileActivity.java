package ell.one.clarix;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class DeleteProfileActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    EditText editCurrentPass;
    TextView verifyYou;
    Button currentPassButton, deleteProfileButton;
    String currentPass;
    private static final String TAG = "DeleteProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_profile);

//        set title on the action bar of this activity
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.delete_profile);

        editCurrentPass = findViewById(R.id.current_pass);
        currentPassButton = findViewById(R.id.verify_pass);
        deleteProfileButton = findViewById(R.id.delete_profile_button);
        verifyYou = findViewById(R.id.verify_id_text);

//        disable delete profile button
        deleteProfileButton.setEnabled(false);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

//        if firebaseUser is null, go back to profile activity
        if (firebaseUser == null){
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(DeleteProfileActivity.this, ProfileActivity.class));
            finish();
        }else {
            verifyUser(firebaseUser);
        }
    }

    private void verifyUser(FirebaseUser firebaseUser) {
        currentPassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPass = editCurrentPass.getText().toString();

                if (TextUtils.isEmpty(currentPass)){
                    editCurrentPass.setError("Please enter your password");
                    editCurrentPass.requestFocus();
                }else {
//                    verify user
                    AuthCredential authCredential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), currentPass);
                    firebaseUser.reauthenticate(authCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
//                                disable current password edittext and password
                                editCurrentPass.setEnabled(false);
                                currentPassButton.setEnabled(false);

//                                enable the delete button
                                deleteProfileButton.setEnabled(true);

//                                change text on verify user textView
                                verifyYou.setText(R.string.verified);

                                deleteProfileButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        displayDialogBox();
                                    }
                                });
                            }else{
                                try{
                                    throw task.getException();
                                }catch (Exception e){
                                    Toast.makeText(DeleteProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private void displayDialogBox() {

//        set alert box
        AlertDialog.Builder builder = new AlertDialog.Builder(DeleteProfileActivity.this);
        builder.setTitle(R.string.delete_profile);
        builder.setMessage("You are about to delete your account. Please note that this action cannot be undone.");

//        delete account
        builder.setPositiveButton("Delete anyway", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteUserAccount(firebaseUser);
            }
        });

//        return to profile activity if user clicks cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(DeleteProfileActivity.this, ProfileActivity.class));
                finish();
            }
        });
//        create dialog box
        AlertDialog alertDialog = builder.create();

//        set color of text on delete button
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.red));
            }
        });
        alertDialog.show();
    }

    private void deleteUserAccount(FirebaseUser firebaseUser) {
        firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    deleteUserAccountData();
                    firebaseAuth.signOut();
                    Toast.makeText(DeleteProfileActivity.this, "Profile has been deleted", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(DeleteProfileActivity.this, tutor_home.class));
                    finish();
                }else{
                    try{
                        throw task.getException();
                    }catch (Exception e){
                        Toast.makeText(DeleteProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

//    delete data from realtime database
    private void deleteUserAccountData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        databaseReference.child(firebaseUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "OnSuccess: User data deleted");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.getMessage());
                Toast.makeText(DeleteProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}