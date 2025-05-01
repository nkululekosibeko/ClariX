package com.example.clarix.database_handlers;
import java.util.List;

import SandZ.Tutors.data.classes.TeacherClass;

public interface TeacherListCallback {
    void onTeacherListReceived(List<TeacherClass> teachers);
}
