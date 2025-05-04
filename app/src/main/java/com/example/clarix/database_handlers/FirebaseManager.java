package com.example.clarix.database_handlers;

import android.content.Context;
import android.content.Intent;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.example.clarix.R;
import com.example.clarix.activities.LogIn;
import com.example.clarix.activities.SignUp;

import com.example.clarix.data.classes.Meeting;
import com.example.clarix.data.classes.TeacherClass;
import com.example.clarix.data.classes.Term;

public class FirebaseManager {

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
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Create a user object with additional details
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("email", email);
                            userData.put("name", name);
                            userData.put("surname", surname);
                            userData.put("userType", isTeacher ? "teacher" : "student");
                            userData.put("picture", R.drawable.annonym);

                            // Store the user in Firestore
                            db.collection("users").document(user.getUid())
                                    .set(userData, SetOptions.merge())
                                    .addOnCompleteListener(task12 -> {
                                        if (task12.isSuccessful()) {
                                            if (isTeacher) {
                                                Map<String, Object> teacherData = new HashMap<>();
                                                teacherData.put("subjects", new ArrayList<>());
                                                teacherData.put("price", 0);
                                                db.collection("users").document(user.getUid())
                                                        .set(teacherData, SetOptions.merge())
                                                        .addOnCompleteListener(task1 -> {
                                                            if (task1.isSuccessful()) {
                                                                Toast.makeText(context, "Account created and user information added to Firestore.", Toast.LENGTH_SHORT).show();
                                                                Intent intent = new Intent(context, LogIn.class);
                                                                context.startActivity(intent);
                                                                ((SignUp) context).finish();
                                                            } else {
                                                                Toast.makeText(context, "Account created, but failed to add teacher information to Firestore.", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            } else {
                                                // Student registration
                                                Toast.makeText(context, "Account created and user information added to Firestore.", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(context, LogIn.class);
                                                context.startActivity(intent);
                                                ((SignUp) context).finish();
                                            }
                                        } else {
                                            Toast.makeText(context, "Account created, but failed to add user information to Firestore.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(context, "User is null.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
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
                    for(QueryDocumentSnapshot document : queryDocumentSnapshots){
                        String subject = document.getId();
                        subjects.add(subject);
                    }
                    callback.onSubjectListReceived(subjects);
                });
    }

    public void getTeacherList(final TeacherListCallback callback) {
        ArrayList<TeacherClass> teachers = new ArrayList<>();
        // Handle failure
        db.collection("users")
                .whereEqualTo("userType", "teacher")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String id = document.getId();
                        String email = document.getString("email");
                        String name = document.getString("name");
                        String surname = document.getString("surname");
                        List<String> subjects = (List<String>) document.get("subjects");

                        // Retrieve the "rates" field as a map of strings to numbers
                        Map<String, Object> ratesMap = (Map<String, Object>) document.get("ratings");
                        List<Integer> rates = new ArrayList<>();
                        if (ratesMap != null) {
                            for (Object rate : ratesMap.values()) {
                                // Assuming rates are stored as numbers
                                if (rate instanceof Number) {
                                    rates.add(((Number) rate).intValue());
                                }
                            }
                        }

                        int price = Objects.requireNonNull(document.getLong("price")).intValue();
                        int picture = Objects.requireNonNull(document.getLong("picture")).intValue();
                        TeacherClass teacher = new TeacherClass(id, email, name, surname, subjects, rates, price, picture);
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
                    if (documentSnapshot.exists() && documentSnapshot.getString("userType").equals("teacher")) {
                        String id = documentSnapshot.getId();
                        String email = documentSnapshot.getString("email");
                        String name = documentSnapshot.getString("name");
                        String surname = documentSnapshot.getString("surname");
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
                        TeacherClass teacher = new TeacherClass(id, email, name, surname, subjects, rates, price, picture);
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

}

