package ell.one.clarix.database_handlers;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ell.one.clarix.HelperClass;
import ell.one.clarix.activities.LoginActivity;
import ell.one.clarix.activities.SignupActivity;
import ell.one.clarix.activities.guest;
import ell.one.clarix.activities.tutee_home;
import ell.one.clarix.activities.tutor_home;

public class FirebaseManager {

    private static final String TAG = "FirebaseManager";

    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;
    private final Context context;

    public FirebaseManager(Context context) {
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public void signOut() {
        Log.d(TAG, "signOut: Signing out user");
        mAuth.signOut();
    }

    public FirebaseUser getCurrentUser() {
        FirebaseUser user = mAuth.getCurrentUser();
        Log.d(TAG, "getCurrentUser: " + (user != null ? user.getUid() : "null"));
        return user;
    }

    public void registerUser(String email, String password, String fullName, String role) {
        Log.d(TAG, "registerUser: Registering user with email: " + email + ", role: " + role);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Log.d(TAG, "registerUser: Firebase user created: " + user.getUid());

                            Map<String, Object> userData = new HashMap<>();
                            userData.put("email", email);
                            userData.put("fullName", fullName);
                            userData.put("role", role);

                            db.collection("users").document(user.getUid())
                                    .set(userData, SetOptions.merge())
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "registerUser: User data saved to Firestore");
                                        Toast.makeText(context, "User registered successfully", Toast.LENGTH_SHORT).show();
                                        context.startActivity(new Intent(context, LoginActivity.class));
                                        ((SignupActivity) context).finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "registerUser: Failed to save user data", e);
                                        Toast.makeText(context, "Error saving user data", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Log.e(TAG, "registerUser: FirebaseUser is null after registration");
                            Toast.makeText(context, "User is null.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "registerUser: Registration failed", task.getException());
                        Toast.makeText(context, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void getUserData(String fieldName, OnDataRetrievedListener listener) {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            Log.d(TAG, "getUserData: Fetching '" + fieldName + "' for user: " + user.getUid());

            DocumentReference userRef = db.collection("users").document(user.getUid());
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String data = document.getString(fieldName);
                        Log.d(TAG, "getUserData: Retrieved data = " + data);
                        listener.onDataRetrieved(data != null ? data : "");
                    } else {
                        Log.w(TAG, "getUserData: Document does not exist");
                        listener.onDataRetrieved("");
                    }
                } else {
                    Log.e(TAG, "getUserData: Failed to fetch document", task.getException());
                    listener.onDataRetrieved("");
                }
            });
        } else {
            Log.w(TAG, "getUserData: No user signed in");
            listener.onDataRetrieved("");
        }
    }

    public interface OnDataRetrievedListener {
        void onDataRetrieved(String data);
    }


    public void navigateBasedOnRole(Context context) {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            db.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String role = documentSnapshot.getString("role");
                            Log.d(TAG, "navigateBasedOnRole: User role = " + role);

                            Intent intent;
                            switch (role) {
                                case "tutor":
                                    intent = new Intent(context, tutor_home.class);
                                    break;
                                case "tutee":
                                    intent = new Intent(context, tutee_home.class);
                                    break;
                                case "guest":
                                default:
                                    intent = new Intent(context, guest.class);
                                    break;
                            }

                            context.startActivity(intent);
                        } else {
                            Log.w(TAG, "navigateBasedOnRole: No role found for user");
                            Toast.makeText(context, "User role not found.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "navigateBasedOnRole: Failed to get user role", e);
                        Toast.makeText(context, "Failed to load user data", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.w(TAG, "navigateBasedOnRole: No user is signed in");
            Toast.makeText(context, "User not signed in", Toast.LENGTH_SHORT).show();
        }
    }


    public interface OnUserProfileRetrieved {
        void onUserProfileLoaded(HelperClass profile);
    }

    public void getUserProfile(OnUserProfileRetrieved callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            HelperClass profile = snapshot.toObject(HelperClass.class);
                            callback.onUserProfileLoaded(profile);
                        } else {
                            Log.w(TAG, "getUserProfile: Document does not exist");
                            callback.onUserProfileLoaded(null);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "getUserProfile: Failed to retrieve", e);
                        callback.onUserProfileLoaded(null);
                    });
        }
    }


    public interface OnProfileUpdateListener {
        void onUpdateSuccess();
        void onUpdateFailure(Exception e);
    }

    public void updateUserProfile(HelperClass profileData, OnProfileUpdateListener listener) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid())
                    .set(profileData, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "updateUserProfile: Update successful");
                        listener.onUpdateSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "updateUserProfile: Failed to update", e);
                        listener.onUpdateFailure(e);
                    });
        } else {
            listener.onUpdateFailure(new Exception("No user signed in"));
        }
    }


    public interface AvailabilitySaveListener {
        void onSuccess();
        void onFailure(Exception e);
    }





    public void saveAvailabilityForDay(String date, String startTime, String endTime, String meetingLink, AvailabilitySaveListener listener) {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            listener.onFailure(new Exception("User not authenticated"));
            return;
        }

        String userId = user.getUid();

        if (date == null || startTime == null || endTime == null || meetingLink == null ||
                date.isEmpty() || startTime.isEmpty() || endTime.isEmpty() || meetingLink.isEmpty()) {
            listener.onFailure(new IllegalArgumentException("Required fields are missing"));
            return;
        }

        // Build availability map
        Map<String, Object> schedule = new HashMap<>();
        schedule.put("date", date);
        schedule.put("startTime", startTime);
        schedule.put("endTime", endTime);
        schedule.put("meetingLink", meetingLink);
        schedule.put("timestamp", FieldValue.serverTimestamp());

        // Save as a new document (not overwriting by date)
        db.collection("users")
                .document(userId)
                .collection("availability")
                .add(schedule)
                .addOnSuccessListener(docRef -> {
                    Log.d(TAG, "Availability saved: " + docRef.getId());
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save availability", e);
                    listener.onFailure(e);
                });
    }


    public interface OnAvailabilityFetchedListener {
        void onSuccess(List<Map<String, Object>> availabilityList);
        void onFailure(Exception e);
    }

    public void fetchTutorAvailability(OnAvailabilityFetchedListener listener) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            listener.onFailure(new Exception("User not authenticated"));
            return;
        }

        db.collection("users")
                .document(user.getUid())
                .collection("availability")
                .orderBy("timestamp")  // Optional: ordered by saved time
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    List<Map<String, Object>> result = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshots) {
                        Map<String, Object> data = doc.getData();
                        data.put("docId", doc.getId());  // Keep track of the document ID for deletion
                        result.add(data);
                    }
                    listener.onSuccess(result);
                })
                .addOnFailureListener(listener::onFailure);
    }



    public boolean isEmailVerified() {
        FirebaseUser user = mAuth.getCurrentUser();
        return user != null && user.isEmailVerified();
    }


    public void deleteUserProfile(String uid, OnDeleteListener listener) {
        FirebaseFirestore.getInstance().collection("users")
                .document(uid)
                .delete()
                .addOnSuccessListener(unused -> {
                    if (listener != null) {
                        listener.onDeleteSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onDeleteFailure(e);
                    }
                });
    }

    public interface OnDeleteListener {
        void onDeleteSuccess();
        void onDeleteFailure(Exception e);
    }


}
