package com.example.clarix.database_handlers;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

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

    private final FirebaseUser user;

    public FirebaseManager(Context context) {
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();
    }



    public void signOut() {
        mAuth.signOut();
    }

    public FirebaseUser getCurrentUser() {
        return user;
    }


    public void getUserData(String fieldName, OnDataRetrievedListener listener) {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            DocumentReference userRef = db.collection("users").document(user.getUid());
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String data = document.getString(fieldName);
                        listener.onDataRetrieved(data != null ? data : "");
                    } else {
                        listener.onDataRetrieved("");
                    }
                } else {
                    listener.onDataRetrieved("");
                }
            });
        } else {
            listener.onDataRetrieved("");
        }
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


    public void getMeetingsForCurrentUser(OnSuccessListener<ArrayList<Meeting>> successListener, OnFailureListener failureListener) {
        ArrayList<Meeting> meetings_objects = new ArrayList<>();

        db.collection("users").document(user.getUid()).collection("meetings")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Date startDate = document.getDate("date");
                        String link = document.getString("link");
                        String student = document.getString("student");
                        String teacher = document.getString("teacher");
                        Meeting spotkanie = new Meeting(startDate, link, student, teacher);
                        meetings_objects.add(spotkanie);
                    }
                    successListener.onSuccess(meetings_objects);
                })
                .addOnFailureListener(failureListener);
    }
    public void getTermsForTeacher(String userID, OnSuccessListener<ArrayList<Term>> successListener) {
        ArrayList<Term> terms_objects = new ArrayList<>();

        db.collection("users").document(userID).collection("terms")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for(QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Timestamp timestamp = document.getTimestamp("date");
                        boolean isBooked = Boolean.TRUE.equals(document.getBoolean("isBooked"));
                        String link = document.getString("link");
                        Term term = new Term(timestamp, isBooked,link);
                        terms_objects.add(term);
                    }
                    successListener.onSuccess(terms_objects);
                })
                .addOnFailureListener(e -> Toast.makeText(context,"Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public void addTermToFirebase(String userID, Timestamp timestamp, boolean isBooked, String link) {
        Map<String, Object> termData = new HashMap<>();
        termData.put("date", timestamp);
        termData.put("isBooked", isBooked);
        termData.put("link", link);

        db.collection("users").document(userID).collection("terms")
                .add(termData)
                .addOnSuccessListener(documentReference -> Toast.makeText(context, "Term added", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context,"Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public void getSubjectList(final SubjectListCallback callback) {
        ArrayList<String> subjects = new ArrayList<>();
        db.collection("subjects")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String subject = document.getId();
                        subjects.add(subject);
                    }
                    callback.onSubjectListReceived(subjects);
                });
    }

    public void getTeacherList(final TeacherListCallback callback) {
        ArrayList<TeacherClass> teachers = new ArrayList<>();
        db.collection("users")
                .whereEqualTo("userType", "teacher")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String id = document.getId();
                        String email = document.getString("email");
                        String name = document.getString("name");
                        String surname = document.getString("surname");
                        String phoneNumber = document.getString("phoneNumber");
                        String bio = document.getString("bio");
                        List<String> subjects = (List<String>) document.get("subjects");

                        Map<String, Object> ratesMap = (Map<String, Object>) document.get("ratings");
                        List<Integer> rates = new ArrayList<>();
                        if (ratesMap != null) {
                            for (Object rate : ratesMap.values()) {
                                if (rate instanceof Number) {
                                    rates.add(((Number) rate).intValue());
                                }
                            }
                        }

                        int price = Objects.requireNonNull(document.getLong("price")).intValue();
                        int picture = Objects.requireNonNull(document.getLong("picture")).intValue();

                        TeacherClass teacher = new TeacherClass(id, email, name, surname, subjects, rates, price, picture, phoneNumber, bio);
                        teachers.add(teacher);
                    }
                    callback.onTeacherListReceived(teachers);
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }



    public void updateSubjects(String userID, List<String> newSubjects, Context context) {
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("subjects", newSubjects);

        db.collection("users").document(userID).set(updateMap, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Subjects actualized successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    Toast.makeText(context, "Error during actualization", Toast.LENGTH_SHORT).show();
                });
    }

    public void getTeacherSubjects(String teacherID, OnSuccessListener<ArrayList<String>> successListener, OnFailureListener failureListener) {
        db.collection("users").document(teacherID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    ArrayList<String> teacherSubjects = new ArrayList<>();
                    if (documentSnapshot.exists()) {
                        teacherSubjects = (ArrayList<String>) documentSnapshot.get("subjects");
                    }
                    successListener.onSuccess(teacherSubjects);
                })
                .addOnFailureListener(e -> failureListener.onFailure(e));
    }

    public void addMeetingForTeacherAndStudent(String teacherID, String studentID, Timestamp date, String link, String teacher, String student) {
        Map<String, Object> meetingData = new HashMap<>();
        meetingData.put("date", date);
        meetingData.put("link", link);
        meetingData.put("teacher", teacher);
        meetingData.put("student", student);

        addMeetingToCollection(teacherID, meetingData);
        addMeetingToCollection(studentID, meetingData);

        updateTermsStatus(teacherID, date);
    }

    private void addMeetingToCollection(String userID, Map<String, Object> meetingData) {
        db.collection("users").document(userID).collection("meetings")
                .add(meetingData)
                .addOnSuccessListener(documentReference -> Toast.makeText(context, "Meeting added", Toast.LENGTH_SHORT).show());
    }

    private void updateTermsStatus(String userID, Timestamp date) {
        db.collection("users").document(userID).collection("terms")
                .whereEqualTo("date", date)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().update("isBooked", true);
                    }
                });
    }

    public void updatePrice(String teacherId, int price){
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("price", price);

        db.collection("users").document(teacherId).set(updateMap, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Price actualized successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    Toast.makeText(context, "Error during updating price", Toast.LENGTH_SHORT).show();
                });
    }
    public void addRate(String teacherId, String studentId, int rate) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> newRating = new HashMap<>();
        newRating.put("studentId", studentId);
        newRating.put("value", rate);


        db.collection("users").document(teacherId)
                .update("ratings." + studentId, rate)
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Rate added successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    Toast.makeText(context, "\"Error during adding rate", Toast.LENGTH_SHORT).show();
                });
    }

    public void getTeacherById(String userId, final TeacherCallback callback) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && "teacher".equals(documentSnapshot.getString("userType"))) {
                        String id = documentSnapshot.getId();
                        String email = documentSnapshot.getString("email");
                        String name = documentSnapshot.getString("name");
                        String surname = documentSnapshot.getString("surname");
                        String phoneNumber = documentSnapshot.getString("phoneNumber");
                        String bio = documentSnapshot.getString("bio");
                        List<String> subjects = (List<String>) documentSnapshot.get("subjects");

                        Map<String, Object> ratesMap = (Map<String, Object>) documentSnapshot.get("ratings");
                        List<Integer> rates = new ArrayList<>();
                        if (ratesMap != null) {
                            for (Object rate : ratesMap.values()) {
                                if (rate instanceof Number) {
                                    rates.add(((Number) rate).intValue());
                                }
                            }
                        }

                        int price = Objects.requireNonNull(documentSnapshot.getLong("price")).intValue();
                        int picture = Objects.requireNonNull(documentSnapshot.getLong("picture")).intValue();

                        TeacherClass teacher = new TeacherClass(id, email, name, surname, subjects, rates, price, picture, phoneNumber, bio);
                        callback.onTeacherReceived(teacher);
                    } else {
                        callback.onTeacherReceived(null);
                    }
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    callback.onTeacherReceived(null);
                });
    }


    public void getImage(String userID, OnSuccessListener<Integer> successListener, OnFailureListener failureListener) {
        db.collection("users").document(userID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    int picture = Objects.requireNonNull(documentSnapshot.getLong("picture")).intValue();
                    successListener.onSuccess(picture);
                })
                .addOnFailureListener(failureListener);
    }

    public void setImage(String userID, int picture) {
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("picture", picture);

        db.collection("users").document(userID).set(updateMap, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Image updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    Toast.makeText(context, "Error during updating image", Toast.LENGTH_SHORT).show();
                });
    }

    public void addAvailabilitySlot(String tutorId, Timestamp startTime, Timestamp endTime) {
        Map<String, Object> availability = new HashMap<>();
        availability.put("tutorId", tutorId);
        availability.put("startTime", startTime);
        availability.put("endTime", endTime);
        availability.put("isBooked", false); // Optional flag for booking status

        db.collection("availability")
                .add(availability)
                .addOnSuccessListener(documentReference -> {
                    Log.d("FirebaseManager", "Availability added with ID: " + documentReference.getId());
                    Toast.makeText(context, "Availability added successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    Toast.makeText(context, "Failed to add availability", Toast.LENGTH_SHORT).show();
                });
    }


}

