//package com.example.clarix.activities;
//
//import android.content.Context;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//import android.widget.ListView;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import com.example.clarix.R;
//import com.example.clarix.data_adapters.SubjectAdapter;
//import com.example.clarix.database_handlers.FirebaseManager;
//import com.example.clarix.database_handlers.SubjectListCallback;
//
//public class SubjectView extends AppCompatActivity {
//
//    private ListView listView;
//    private Button updateButton;
//
//    private FirebaseManager manager;
//
//    private SubjectAdapter adapter;
//
//    private boolean subjectsReceived, teacherSubjectsReceived;
//
//    private List<String> subjects, teacherSubjects, selectedSubjects;
//    private Context context;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_subject_view);
//        manager = new FirebaseManager(this);
//
//        subjects = new ArrayList<>();
//        teacherSubjects = new ArrayList<>();
//        subjectsReceived = false;
//        teacherSubjectsReceived = false;
//
//        listView = findViewById(R.id.subjectList);
//        updateButton = findViewById(R.id.button);
//
//
//        context = this;
//        manager.getSubjectList(new SubjectListCallback() {
//            @Override
//            public void onSubjectListReceived(List<String> receivedSubjects) {
//                subjects = receivedSubjects;
//                subjectsReceived = true;
//                if(teacherSubjectsReceived)
//                    initializeAdapter();
//            }
//        });
//
//        manager.getTeacherSubjects(manager.getCurrentUser().getUid(),
//                new OnSuccessListener<ArrayList<String>>() {
//                    @Override
//                    public void onSuccess(ArrayList<String> receivedSubjects) {
//                        teacherSubjects = receivedSubjects;
//                        teacherSubjectsReceived = true;
//                        if(subjectsReceived)
//                            initializeAdapter();
//                    }
//                },
//                new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        e.printStackTrace();
//                    }
//                });
//        updateButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                selectedSubjects = adapter.getSelectedSubjects();
//                manager.updateSubjects(manager.getCurrentUser().getUid(), selectedSubjects,context);
//                finish();
//                }
//        });
//    }
//
//
//    private void initializeAdapter() {
//            adapter = new SubjectAdapter(this, subjects, teacherSubjects);
//            listView.setAdapter(adapter);
//        }
//    }
//
