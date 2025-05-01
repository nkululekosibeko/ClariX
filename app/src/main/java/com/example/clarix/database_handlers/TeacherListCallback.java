package com.example.clarix.database_handlers;
import java.util.List;

import com.example.clarix.data.classes.TeacherClass;

public interface TeacherListCallback {
    void onTeacherListReceived(List<TeacherClass> teachers);
}
