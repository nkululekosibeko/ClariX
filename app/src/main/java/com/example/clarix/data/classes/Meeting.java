package com.example.clarix.data.classes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Meeting {
    private Date date;
    private String link;
    private String student;
    private String teacher;

    public Meeting(Date date, String link, String student, String teacher) {
        this.date = date;
        this.link = link;
        this.student = student;
        this.teacher = teacher;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getStudent() {
        return student;
    }

    public void setStudent(String student) {
        this.student = student;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }
    public String getDateString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault());
        return dateFormat.format(date);
    }


}
