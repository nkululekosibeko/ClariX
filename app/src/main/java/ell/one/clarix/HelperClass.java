package ell.one.clarix;

public class HelperClass {

    String name, email,phoneNo, studentNo, username, bio, specialization, rate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
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

    public HelperClass(String name, String email, String phoneNo, String bio, String specialization, String rate) {
        this.name = name;
        this.email = email;
        this.phoneNo = phoneNo;
        this.bio = bio;
        this.specialization = specialization;
        this.rate = rate;
    }

    public HelperClass() {
    }
}
