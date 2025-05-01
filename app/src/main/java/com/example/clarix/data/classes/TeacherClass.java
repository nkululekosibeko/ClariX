package com.example.clarix.data.classes;

import java.io.Serializable;
import java.util.List;

public class TeacherClass implements Serializable {
    private String id;
    private String email;
    private String name;
    private String surname;
    private int picture;
    private List<String> subjects;
    private int price;
    private final List<Integer> rates;

    public TeacherClass(String id, String email, String name, String surname, List<String> subjects, List<Integer> rates, int price,int picture) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.subjects = subjects;
        this.rates = rates;
        this.price = price;
        this.picture = picture;
    }

    public String getStringPrice() {
        return Integer.toString(price);
    }
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    public String getSurname() {
        return surname;
    }
    public List<String> getSubjects() {
        return subjects;
    }

    public int getPrice() {
        return price;
    }

    public float getRate() {
        if (rates.size() == 0)
            return 0;
        float sum = 0;
        for (int rate : rates) {
            sum += rate;
        }
        return sum / rates.size();
    }

    public String getRateString() {
        return String.format("%.2f", getRate());
    }

    public int getPicture() {
        return picture;
    }


}
