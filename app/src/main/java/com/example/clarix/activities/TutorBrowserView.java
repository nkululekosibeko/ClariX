//package com.example.clarix.activities;
//
//import android.app.AlertDialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.AdapterView;
//import android.widget.Button;
//import android.widget.ImageButton;
//import android.widget.ListView;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.List;
//
//import com.example.clarix.R;
//import com.example.clarix.data.classes.TeacherClass;
//import com.example.clarix.data_adapters.TutorListAdapter;
//import com.example.clarix.database_handlers.FirebaseManager;
//import com.example.clarix.database_handlers.SubjectListCallback;
//import com.example.clarix.database_handlers.TeacherListCallback;
//
//public class TutorBrowserView extends AppCompatActivity {
//    private FirebaseManager manager;
//    private Button subjectButton;
//    private ImageButton rateButton, priceButton, alphabeticalButton;
//    private ListView tutorListView;
//    private TutorListAdapter adapter;
//    private Context context;
//    private List<TeacherClass> all_teachers, filtered_teachers;
//    private List<String> subjects;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_search_for_tutor);
//
//        manager = new FirebaseManager(this);
//        subjects = new ArrayList<>();
//        subjectButton = findViewById(R.id.subject_button);
//        rateButton = findViewById(R.id.sortByRating);
//        priceButton = findViewById(R.id.sortByPrice);
//        alphabeticalButton = findViewById(R.id.sortAlphabetically);
//
//        tutorListView = findViewById(R.id.tutorListView);
//        context = this;
//        all_teachers = new ArrayList<TeacherClass>();
//        filtered_teachers = new ArrayList<TeacherClass>();
//
//        subjectButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showFilterDialog();
//            }
//        });
//
//        manager.getSubjectList(new SubjectListCallback() {
//            @Override
//            public void onSubjectListReceived(List<String> receivedSubjects) {
//                subjects = receivedSubjects;
//                subjects.add(0, "All");
//            }
//        });
//        manager.getTeacherList(new TeacherListCallback() {
//            @Override
//            public void onTeacherListReceived(List<TeacherClass> receivedTeachers) {
//                all_teachers = receivedTeachers;
//                filtered_teachers = receivedTeachers;
//                adapter = new TutorListAdapter(context, filtered_teachers);
//                tutorListView.setAdapter(adapter);
//                tutorListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                        TeacherClass selectedTeacher = (TeacherClass) parent.getItemAtPosition(position);
//                        if (selectedTeacher != null) {
//                            Intent intent = new Intent(TutorBrowserView.this, TeacherAccountView.class);
//                            intent.putExtra("teacher", selectedTeacher);
//                            startActivity(intent);
//                        }
//                    }
//                });
//            }
//        });
//        alphabeticalButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Collections.sort(filtered_teachers, new Comparator<TeacherClass>() {
//                    @Override
//                    public int compare(TeacherClass teacher1, TeacherClass teacher2) {
//                        return teacher1.getSurname().compareToIgnoreCase(teacher2.getSurname());
//                    }
//                });
//                adapter.notifyDataSetChanged();
//            }
//        });
//        priceButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // Sort the list of teachers by price
//                Collections.sort(filtered_teachers, new Comparator<TeacherClass>() {
//                    @Override
//                    public int compare(TeacherClass teacher1, TeacherClass teacher2) {
//                        return teacher1.getPrice() - teacher2.getPrice();
//                    }
//                });
//                adapter.notifyDataSetChanged();
//            }
//        });
//        rateButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Collections.sort(filtered_teachers, new Comparator<TeacherClass>() {
//                    @Override
//                    public int compare(TeacherClass teacher1, TeacherClass teacher2) {
//                        float rate1 = teacher1.getRate();
//                        float rate2 = teacher2.getRate();
//                        return Float.compare(rate2, rate1);
//                    }
//                });
//                adapter.notifyDataSetChanged();
//            }
//        });
//
//
//    }
//
//
//
//    private void showFilterDialog() {
//        if (subjects == null)
//            return;
//        final List<String> filterOptions = subjects;
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Choose subject")
//                .setItems(filterOptions.toArray(new CharSequence[0]), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        String selectedOption = filterOptions.get(i);
//                        subjectButton.setText(selectedOption);
//                        if(selectedOption.equals("All")){
//                            filtered_teachers = all_teachers;
//                            adapter = new TutorListAdapter(context, filtered_teachers);
//                            tutorListView.setAdapter(adapter);
//                            return;
//                        }
//                        List<TeacherClass> filteredTeachers = new ArrayList<>();
//                        for (TeacherClass teacher : filtered_teachers) {
//                            if (teacher.getSubjects().contains(selectedOption)) {
//                                filteredTeachers.add(teacher);
//                            }
//                        }
//                        adapter = new TutorListAdapter(context, filteredTeachers);
//                        tutorListView.setAdapter(adapter);
//                    }
//                });
//
//        builder.create().show();
//    }
//}
