package ell.one.clarix.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ell.one.clarix.ProfileActivity;
import ell.one.clarix.R;

public class ChangePasswordActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    EditText editCurrentPass, editNewPass, editNewConfirmPass;
    TextView verifyYou;
    Button currentPassButton, newPassButton;
    String currentPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        editCurrentPass = findViewById(R.id.current_pass);
        editNewPass = findViewById(R.id.new_pass);
        editNewConfirmPass = findViewById(R.id.confirm_new_pass);
        currentPassButton = findViewById(R.id.verify_pass);
        newPassButton = findViewById(R.id.new_pass_button);
        verifyYou = findViewById(R.id.verify_id_text);

//        disable EditText for new password, confirm new password, and change password button
        editNewPass.setEnabled(false);
        editNewConfirmPass.setEnabled(false);
        newPassButton.setEnabled(false);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

//        if firebaseUser is null, go back to profile activity
        if (firebaseUser == null){
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(ChangePasswordActivity.this, ProfileActivity.class));
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
                    editCurrentPass.setError("Please enter your current password");
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

//                                enable new password, confirm new password edittexts, and a change password button
                                editNewPass.setEnabled(true);
                                editNewConfirmPass.setEnabled(true);
                                newPassButton.setEnabled(true);

//                                change text on verify user textView
                                verifyYou.setText(R.string.verified);

                                newPassButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        changeUserPassword(firebaseUser);
                                    }
                                });
                            }else{
                                try{
                                    throw task.getException();
                                }catch (Exception e){
                                    Toast.makeText(ChangePasswordActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private void changeUserPassword(FirebaseUser firebaseUser) {
        String newPass = editNewPass.getText().toString();
        String newConfirmPass = editNewConfirmPass.getText().toString();

        if (TextUtils.isEmpty(newPass)){
            editNewPass.setError("Please enter a new password");
            editNewPass.requestFocus();
        } else if (newPass.length() < 8) {
            editNewPass.setError("Password must be at least 8 characters long");
            editNewPass.requestFocus();
        } else if (TextUtils.isEmpty(newConfirmPass)) {
            editNewConfirmPass.setError("Please confirm your new password");
            editNewConfirmPass.requestFocus();
        } else if (!newPass.matches(newConfirmPass)) {
            editNewConfirmPass.setError("Password must match");
            editNewConfirmPass.requestFocus();
        } else if (currentPass.matches(newPass)) {
            editNewPass.setError("Password cannot be the same as the old password");
            editNewPass.requestFocus();
        }else {
            firebaseUser.updatePassword(newPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(ChangePasswordActivity.this, "Password changed!", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(ChangePasswordActivity.this, ProfileActivity.class));
                        finish();
                    }else{
                        try{
                            throw task.getException();
                        }catch (Exception e){
                            Toast.makeText(ChangePasswordActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
        }
    }
}