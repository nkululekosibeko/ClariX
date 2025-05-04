package com.example.clarix.data.classes;

import java.io.Serializable;
import java.util.List;

public class TeacherClass implements Serializable {
    private String id;
    private String email;
    private String name;
    private String surname;
    private String phoneNumber;
    private String bio;
    private String profileImageUrl;
    private List<String> subjects;
    private int price;
    private final List<Integer> rates;

    public TeacherClass(String id, String email, String name, String surname,
                        String phoneNumber, String bio, List<String> subjects,
                        List<Integer> rates, int price, String profileImageUrl) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.phoneNumber = phoneNumber;
        this.bio = bio;
        this.subjects = subjects;
        this.rates = rates;
        this.price = price;
        this.profileImageUrl = profileImageUrl;
    }

    // Secondary constructor used by FirebaseManager (for backward compatibility)
    public TeacherClass(String id, String email, String name, String surname,
                        List<String> subjects, List<Integer> rates, int price,
                        int ignoredPicture, String phoneNumber, String bio) {
        this(id, email, name, surname, phoneNumber, bio, subjects, rates, price, null);
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getBio() {
        return bio;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public List<String> getSubjects() {
        return subjects;
    }

    public int getPrice() {
        return price;
    }

    public float getRate() {
        if (rates == null || rates.isEmpty()) return 0;
        float sum = 0;
        for (int rate : rates) sum += rate;
        return sum / rates.size();
    }

    public String getRateString() {
        return String.format("%.2f", getRate());
    }

    public String getStringPrice() {
        return Integer.toString(price);
    }
}
