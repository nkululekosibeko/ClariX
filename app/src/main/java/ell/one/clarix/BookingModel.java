package ell.one.clarix;

public class BookingModel {
    private String bookingId;
    private String tutorId;
    private String tutorName;
    private String tuteeId;
    private String tuteeName;
    private String date;
    private String startTime;
    private String endTime;
    private String status;
    private String meetingLink;

    // Default constructor (required for Firebase)
    public BookingModel() {}

    // Full parameterized constructor (including meetingLink)
    public BookingModel(String bookingId, String tutorId, String tutorName,
                        String tuteeId, String tuteeName,
                        String date, String startTime, String endTime,
                        String status, String meetingLink) {
        this.bookingId = bookingId;
        this.tutorId = tutorId;
        this.tutorName = tutorName;
        this.tuteeId = tuteeId;
        this.tuteeName = tuteeName;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.meetingLink = meetingLink;
    }

    // Getters
    public String getBookingId() {
        return bookingId;
    }

    public String getTutorId() {
        return tutorId;
    }

    public String getTutorName() {
        return tutorName;
    }

    public String getTuteeId() {
        return tuteeId;
    }

    public String getTuteeName() {
        return tuteeName;
    }

    public String getDate() {
        return date;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getStatus() {
        return status;
    }

    public String getMeetingLink() {
        return meetingLink;
    }

    // Setters (for local updates)
    public void setStatus(String status) {
        this.status = status;
    }

    public void setMeetingLink(String meetingLink) {
        this.meetingLink = meetingLink;
    }
}
