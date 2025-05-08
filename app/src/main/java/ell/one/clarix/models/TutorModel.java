package ell.one.clarix.models;

/**
 * Represents a Tutor's profile data including unique ID, name, specialization, hourly rate, and bio.
 */
public class TutorModel {
    private String tutorId;
    private String name;
    private String specialization;
    private String rate;
    private String bio;

    // Default constructor required for Firestore deserialization
    public TutorModel() {}

    // Constructor with all fields
    public TutorModel(String tutorId, String name, String specialization, String rate, String bio) {
        this.tutorId = tutorId;
        this.name = name;
        this.specialization = specialization;
        this.rate = rate;
        this.bio = bio;
    }

    public String getTutorId() {
        return tutorId;
    }

    public void setTutorId(String tutorId) {
        this.tutorId = tutorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
