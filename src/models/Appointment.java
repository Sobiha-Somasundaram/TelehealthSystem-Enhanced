package models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Appointment model class representing patient consultation bookings
 * Used for both patient bookings and staff management of appointments
 */
public class Appointment {
    private int appointmentId;
    private String patientName;
    private String specialistName;
    private LocalDate appointmentDate;
    private String timeSlot;
    private String status; // SCHEDULED, COMPLETED, CANCELLED, RESCHEDULED
    private String consultationType; // AUDIO, VIDEO, IN_PERSON
    private String notes;

    // Default constructor

    /**
     *
     */
    public Appointment() {
        this.status = "SCHEDULED";
        this.consultationType = "VIDEO";
    }

    // Full constructor

    /**
     *
     * @param appointmentId
     * @param patientName
     * @param specialistName
     * @param appointmentDate
     * @param timeSlot
     * @param status
     * @param consultationType
     * @param notes
     */
    public Appointment(int appointmentId, String patientName, String specialistName, 
                      LocalDate appointmentDate, String timeSlot, String status, 
                      String consultationType, String notes) {
        this.appointmentId = appointmentId;
        this.patientName = patientName;
        this.specialistName = specialistName;
        this.appointmentDate = appointmentDate;
        this.timeSlot = timeSlot;
        this.status = status != null ? status : "SCHEDULED";
        this.consultationType = consultationType != null ? consultationType : "VIDEO";
        this.notes = notes;
    }

    // Constructor without ID (for new appointments)

    /**
     *
     * @param patientName
     * @param specialistName
     * @param appointmentDate
     * @param timeSlot
     * @param consultationType
     */
    public Appointment(String patientName, String specialistName, LocalDate appointmentDate, 
                      String timeSlot, String consultationType) {
        this.patientName = patientName;
        this.specialistName = specialistName;
        this.appointmentDate = appointmentDate;
        this.timeSlot = timeSlot;
        this.status = "SCHEDULED";
        this.consultationType = consultationType != null ? consultationType : "VIDEO";
        this.notes = "";
    }

    // Getters

    /**
     *
     * @return
     */
    public int getAppointmentId() { return appointmentId; }

    /**
     *
     * @return
     */
    public String getPatientName() { return patientName; }

    /**
     *
     * @return
     */
    public String getSpecialistName() { return specialistName; }

    /**
     *
     * @return
     */
    public LocalDate getAppointmentDate() { return appointmentDate; }

    /**
     *
     * @return
     */
    public String getTimeSlot() { return timeSlot; }

    /**
     *
     * @return
     */
    public String getStatus() { return status; }

    /**
     *
     * @return
     */
    public String getConsultationType() { return consultationType; }

    /**
     *
     * @return
     */
    public String getNotes() { return notes; }

    // Setters

    /**
     *
     * @param appointmentId
     */
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }

    /**
     *
     * @param patientName
     */
    public void setPatientName(String patientName) { this.patientName = patientName; }

    /**
     *
     * @param specialistName
     */
    public void setSpecialistName(String specialistName) { this.specialistName = specialistName; }

    /**
     *
     * @param appointmentDate
     */
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }

    /**
     *
     * @param timeSlot
     */
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }

    /**
     *
     * @param status
     */
    public void setStatus(String status) { this.status = status; }

    /**
     *
     * @param consultationType
     */
    public void setConsultationType(String consultationType) { this.consultationType = consultationType; }

    /**
     *
     * @param notes
     */
    public void setNotes(String notes) { this.notes = notes; }

    // Business logic methods

    /**
     *
     * @return
     */
    public boolean isUpcoming() {
        return appointmentDate != null && appointmentDate.isAfter(LocalDate.now());
    }

    /**
     *
     * @return
     */
    public boolean canBeCancelled() {
        return "SCHEDULED".equals(status) && isUpcoming();
    }

    /**
     *
     * @return
     */
    public boolean canBeRescheduled() {
        return "SCHEDULED".equals(status) || "RESCHEDULED".equals(status);
    }

    /**
     *
     */
    public void markAsCompleted() {
        this.status = "COMPLETED";
    }

    /**
     *
     */
    public void markAsCancelled() {
        this.status = "CANCELLED";
    }

    /**
     *
     */
    public void markAsRescheduled() {
        this.status = "RESCHEDULED";
    }

    // Formatted date for display

    /**
     *
     * @return
     */
    public String getFormattedDate() {
        if (appointmentDate != null) {
            return appointmentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        return "No date set";
    }

    // Custom toString method for display

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Appointment Details:\n");
        sb.append("ID: ").append(appointmentId).append("\n");
        sb.append("Patient: ").append(patientName != null ? patientName : "N/A").append("\n");
        sb.append("Specialist: Dr. ").append(specialistName != null ? specialistName : "N/A").append("\n");
        sb.append("Date: ").append(getFormattedDate()).append("\n");
        sb.append("Time: ").append(timeSlot != null ? timeSlot : "N/A").append("\n");
        sb.append("Status: ").append(status).append("\n");
        sb.append("Type: ").append(consultationType).append("\n");
        if (notes != null && !notes.trim().isEmpty()) {
            sb.append("Notes: ").append(notes).append("\n");
        }
        return sb.toString();
    }

    // Equals and hashCode for object comparison

    /**
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Appointment that = (Appointment) obj;
        return appointmentId == that.appointmentId;
    }

    /**
     *
     * @return
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(appointmentId);
    }
}