package com.example.clarix.database_handlers;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.clarix.data.classes.HelperClass;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import android.net.Uri;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import com.example.clarix.R;
import com.example.clarix.activities.LogIn;
import com.example.clarix.activities.SignUp;

import com.example.clarix.data.classes.Meeting;
import com.example.clarix.data.classes.TeacherClass;
import com.example.clarix.data.classes.Term;

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
    public void registerUser(String email, String password, String name, String surname, boolean isTeacher) {
        Log.d(TAG, "Attempting to register user: " + email);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Log.d(TAG, "User created: " + user.getUid());

                            Map<String, Object> userData = new HashMap<>();
                            userData.put("email", email);
                            userData.put("name", name);
                            userData.put("surname", surname);
                            userData.put("userType", isTeacher ? "teacher" : "student");
                            userData.put("picture", R.drawable.annonym);

                            db.collection("users").document(user.getUid())
                                    .set(userData, SetOptions.merge())
                                    .addOnCompleteListener(task12 -> {
                                        if (task12.isSuccessful()) {
                                            Log.d(TAG, "User profile saved to Firestore.");
                                            if (isTeacher) {
                                                Map<String, Object> teacherData = new HashMap<>();
                                                teacherData.put("subjects", new ArrayList<>());
                                                teacherData.put("price", 0);

                                                db.collection("users").document(user.getUid())
                                                        .set(teacherData, SetOptions.merge())
                                                        .addOnCompleteListener(task1 -> {
                                                            if (task1.isSuccessful()) {
                                                                Log.d(TAG, "Teacher details saved.");
                                                                Toast.makeText(context, "Account created and user info added.", Toast.LENGTH_SHORT).show();
                                                                context.startActivity(new Intent(context, LogIn.class));
                                                                ((SignUp) context).finish();
                                                            } else {
                                                                Log.e(TAG, "Failed to save teacher info.", task1.getException());
                                                                Toast.makeText(context, "Failed to save teacher info.", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            } else {
                                                Log.d(TAG, "Student registration completed.");
                                                Toast.makeText(context, "Account created.", Toast.LENGTH_SHORT).show();
                                                context.startActivity(new Intent(context, LogIn.class));
                                                ((SignUp) context).finish();
                                            }
                                        } else {
                                            Log.e(TAG, "Failed to write user to Firestore.", task12.getException());
                                            Toast.makeText(context, "Account created, but Firestore failed.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Log.e(TAG, "User object is null after sign-up.");
                            Toast.makeText(context, "User is null.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Firebase sign-up failed", task.getException());
                        Toast.makeText(context, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

    }

    public void updateTeacherProfile(String uid, String name, String surname, String phone, String bio, String subject, int rate) {
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("name", name);
        updateMap.put("surname", surname);
        updateMap.put("phoneNumber", phone);
        updateMap.put("bio", bio);
        updateMap.put("subjects", Arrays.asList(subject));
        updateMap.put("price", rate);

        db.collection("users").document(uid)
                .set(updateMap, SetOptions.merge())
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    Toast.makeText(context, "Error updating profile", Toast.LENGTH_SHORT).show();
                });
    }

    public void uploadProfileImage(String userId, Uri imageUri, OnSuccessListener<String> callback) {
        if (imageUri == null || userId == null || userId.isEmpty()) {
            Log.e(TAG, "uploadProfileImage: Invalid input - URI or User ID is null/empty");
            Toast.makeText(context, "Invalid image or user ID", Toast.LENGTH_SHORT).show();
            return;
        }

        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("profile_images/" + userId + ".jpg");

        Log.d(TAG, "Starting upload to Firebase Storage...");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(TAG, "Upload successful. Fetching download URL...");
                    storageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String downloadUrl = uri.toString();
                                Log.d(TAG, "Download URL received: " + downloadUrl);

                                db.collection("users").document(userId)
                                        .update("profileImageUrl", downloadUrl)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "Image URL saved to Firestore.");
                                            callback.onSuccess(downloadUrl);
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Failed to save image URL to Firestore", e);
                                            Toast.makeText(context, "Failed to save image URL", Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to retrieve download URL", e);
                                Toast.makeText(context, "Image URL retrieval failed", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Image upload failed", e);
                    Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show();
                });
    }


//    public void getTeacherList(final TeacherListCallback callback) {
//        ArrayList<TeacherClass> teachers = new ArrayList<>();
//        db.collection("users")
//                .whereEqualTo("userType", "teacher")
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
//                        String id = document.getId();
//                        String email = document.getString("email");
//                        String name = document.getString("name");
//                        String surname = document.getString("surname");
//                        String phoneNumber = document.getString("phoneNumber");
//                        String bio = document.getString("bio");
//                        List<String> subjects = (List<String>) document.get("subjects");
//
//                        Map<String, Object> ratesMap = (Map<String, Object>) document.get("ratings");
//                        List<Integer> rates = new ArrayList<>();
//                        if (ratesMap != null) {
//                            for (Object rate : ratesMap.values()) {
//                                if (rate instanceof Number) {
//                                    rates.add(((Number) rate).intValue());
//                                }
//                            }
//                        }
//
//                        int price = Objects.requireNonNull(document.getLong("price")).intValue();
//                        int picture = Objects.requireNonNull(document.getLong("picture")).intValue();
//
//                        TeacherClass teacher = new TeacherClass(id, email, name, surname, subjects, rates, price, picture, phoneNumber, bio);
//                        teachers.add(teacher);
//                    }
//                    callback.onTeacherListReceived(teachers);
//                })
//                .addOnFailureListener(Throwable::printStackTrace);
//    }




//    public void registerUser(String email, String password, String fullName, String role) {
//        Log.d(TAG, "registerUser: Registering user with email: " + email + ", role: " + role);
//
//        mAuth.createUserWithEmailAndPassword(email, password)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        FirebaseUser user = mAuth.getCurrentUser();
//                        if (user != null) {
//                            Log.d(TAG, "registerUser: Firebase user created: " + user.getUid());
//
//                            Map<String, Object> userData = new HashMap<>();
//                            userData.put("email", email);
//                            userData.put("fullName", fullName);
//                            userData.put("role", role);
//
//                            db.collection("users").document(user.getUid())
//                                    .set(userData, SetOptions.merge())
//                                    .addOnSuccessListener(aVoid -> {
//                                        Log.d(TAG, "registerUser: User data saved to Firestore");
//                                        Toast.makeText(context, "User registered successfully", Toast.LENGTH_SHORT).show();
//                                        context.startActivity(new Intent(context, LoginActivity.class));
//                                        ((SignupActivity) context).finish();
//                                    })
//                                    .addOnFailureListener(e -> {
//                                        Log.e(TAG, "registerUser: Failed to save user data", e);
//                                        Toast.makeText(context, "Error saving user data", Toast.LENGTH_SHORT).show();
//                                    });
//                        } else {
//                            Log.e(TAG, "registerUser: FirebaseUser is null after registration");
//                            Toast.makeText(context, "User is null.", Toast.LENGTH_SHORT).show();
//                        }
//                    } else {
//                        Log.e(TAG, "registerUser: Registration failed", task.getException());
//                        Toast.makeText(context, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }

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


//    public void navigateBasedOnRole(Context context) {
//        FirebaseUser user = mAuth.getCurrentUser();
//
//        if (user != null) {
//            db.collection("users").document(user.getUid())
//                    .get()
//                    .addOnSuccessListener(documentSnapshot -> {
//                        if (documentSnapshot.exists()) {
//                            String role = documentSnapshot.getString("role");
//                            Log.d(TAG, "navigateBasedOnRole: User role = " + role);
//
//                            Intent intent;
//                            switch (role) {
//                                case "tutor":
//                                    intent = new Intent(context, tutor_home.class);
//                                    break;
//                                case "tutee":
//                                    intent = new Intent(context, tutee_home.class);
//                                    break;
//                                case "guest":
//                                default:
//                                    intent = new Intent(context, guest.class);
//                                    break;
//                            }
//
//                            context.startActivity(intent);
//                        } else {
//                            Log.w(TAG, "navigateBasedOnRole: No role found for user");
//                            Toast.makeText(context, "User role not found.", Toast.LENGTH_SHORT).show();
//                        }
//                    })
//                    .addOnFailureListener(e -> {
//                        Log.e(TAG, "navigateBasedOnRole: Failed to get user role", e);
//                        Toast.makeText(context, "Failed to load user data", Toast.LENGTH_SHORT).show();
//                    });
//        } else {
//            Log.w(TAG, "navigateBasedOnRole: No user is signed in");
//            Toast.makeText(context, "User not signed in", Toast.LENGTH_SHORT).show();
//        }
//    }


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

//    public void updateUserProfile(HelperClass profileData, OnProfileUpdateListener listener) {
//        FirebaseUser user = mAuth.getCurrentUser();
//        if (user != null) {
//            db.collection("users").document(user.getUid())
//                    .set(profileData, SetOptions.merge())
//                    .addOnSuccessListener(aVoid -> {
//                        Log.d(TAG, "updateUserProfile: Update successful");
//                        listener.onUpdateSuccess();
//                    })
//                    .addOnFailureListener(e -> {
//                        Log.e(TAG, "updateUserProfile: Failed to update", e);
//                        listener.onUpdateFailure(e);
//                    });
//        } else {
//            listener.onUpdateFailure(new Exception("No user signed in"));
//        }
//    }


    public interface AvailabilitySaveListener {
        void onSuccess();
        void onFailure(Exception e);
    }



    public void saveAvailabilityForDay(String date, String startTime, String endTime, AvailabilitySaveListener listener) {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            listener.onFailure(new Exception("User not authenticated"));
            return;
        }

        String userId = user.getUid();

        // Validate inputs (optional)
        if (date == null || startTime == null || endTime == null || date.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
            listener.onFailure(new IllegalArgumentException("Date, start time, or end time is missing"));
            return;
        }

        // Create schedule map
        Map<String, Object> schedule = new HashMap<>();
        schedule.put("date", date);
        schedule.put("startTime", startTime);
        schedule.put("endTime", endTime);
        schedule.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

        // Save to Firestore under availability subcollection
        db.collection("users")
                .document(userId)
                .collection("availability")
                .document(date)  // Date as document ID
                .set(schedule)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Availability saved for " + date);
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save availability for " + date, e);
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




}

