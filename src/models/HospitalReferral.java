package models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * HospitalReferral model class representing external hospital/clinic referrals
 * Used when doctors need to book patients at external facilities
 */
public class HospitalReferral {
    private int referralId;
    private String patientName;
    private String referringDoctorName;
    private String hospitalName;
    private String department;
    private String specialtyRequired;
    private String reasonForReferral;
    private String urgencyLevel; // LOW, MEDIUM, HIGH, EMERGENCY
    private LocalDate referralDate;
    private LocalDate preferredAppointmentDate;
    private String status; // PENDING, CONFIRMED, COMPLETED, CANCELLED
    private String contactNumber;
    private String notes;

    // Default constructor
    public HospitalReferral() {
        this.referralDate = LocalDate.now();
        this.urgencyLevel = "MEDIUM";
        this.status = "PENDING";
    }

    // Full constructor
    public HospitalReferral(int referralId, String patientName, String referringDoctorName,
                           String hospitalName, String department, String specialtyRequired,
                           String reasonForReferral, String urgencyLevel, LocalDate referralDate,
                           LocalDate preferredAppointmentDate, String status, String contactNumber,
                           String notes) {
        this.referralId = referralId;
        this.patientName = patientName;
        this.referringDoctorName = referringDoctorName;
        this.hospitalName = hospitalName;
        this.department = department;
        this.specialtyRequired = specialtyRequired;
        this.reasonForReferral = reasonForReferral;
        this.urgencyLevel = urgencyLevel != null ? urgencyLevel : "MEDIUM";
        this.referralDate = referralDate != null ? referralDate : LocalDate.now();
        this.preferredAppointmentDate = preferredAppointmentDate;
        this.status = status != null ? status : "PENDING";
        this.contactNumber = contactNumber;
        this.notes = notes;
    }

    // Constructor for new referral (without ID)
    public HospitalReferral(String patientName, String referringDoctorName, String hospitalName,
                           String department, String reasonForReferral, String urgencyLevel) {
        this.patientName = patientName;
        this.referringDoctorName = referringDoctorName;
        this.hospitalName = hospitalName;
        this.department = department;
        this.reasonForReferral = reasonForReferral;
        this.urgencyLevel = urgencyLevel != null ? urgencyLevel : "MEDIUM";
        this.referralDate = LocalDate.now();
        this.status = "PENDING";
        this.notes = "";
    }

    // Getters
    public int getReferralId() { return referralId; }
    public String getPatientName() { return patientName; }
    public String getReferringDoctorName() { return referringDoctorName; }
    public String getHospitalName() { return hospitalName; }
    public String getDepartment() { return department; }
    public String getSpecialtyRequired() { return specialtyRequired; }
    public String getReasonForReferral() { return reasonForReferral; }
    public String getUrgencyLevel() { return urgencyLevel; }
    public LocalDate getReferralDate() { return referralDate; }
    public LocalDate getPreferredAppointmentDate() { return preferredAppointmentDate; }
    public String getStatus() { return status; }
    public String getContactNumber() { return contactNumber; }
    public String getNotes() { return notes; }

    // Setters
    public void setReferralId(int referralId) { this.referralId = referralId; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public void setReferringDoctorName(String referringDoctorName) { this.referringDoctorName = referringDoctorName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }
    public void setDepartment(String department) { this.department = department; }
    public void setSpecialtyRequired(String specialtyRequired) { this.specialtyRequired = specialtyRequired; }
    public void setReasonForReferral(String reasonForReferral) { this.reasonForReferral = reasonForReferral; }
    public void setUrgencyLevel(String urgencyLevel) { this.urgencyLevel = urgencyLevel; }
    public void setReferralDate(LocalDate referralDate) { this.referralDate = referralDate; }
    public void setPreferredAppointmentDate(LocalDate preferredAppointmentDate) { this.preferredAppointmentDate = preferredAppointmentDate; }
    public void setStatus(String status) { this.status = status; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public void setNotes(String notes) { this.notes = notes; }

    // Business logic methods
    public boolean isUrgent() {
        return "HIGH".equalsIgnoreCase(urgencyLevel) || "EMERGENCY".equalsIgnoreCase(urgencyLevel);
    }

    public boolean isEmergency() {
        return "EMERGENCY".equalsIgnoreCase(urgencyLevel);
    }

    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(status);
    }

    public boolean isConfirmed() {
        return "CONFIRMED".equalsIgnoreCase(status);
    }

    public boolean isCompleted() {
        return "COMPLETED".equalsIgnoreCase(status);
    }

    public boolean canBeCancelled() {
        return isPending() || isConfirmed();
    }

    public void markAsConfirmed() {
        this.status = "CONFIRMED";
    }

    public void markAsCompleted() {
        this.status = "COMPLETED";
    }

    public void markAsCancelled() {
        this.status = "CANCELLED";
    }

    public boolean isOverdue() {
        return preferredAppointmentDate != null && 
               preferredAppointmentDate.isBefore(LocalDate.now()) && 
               isPending();
    }

    // Formatted dates for display
    public String getFormattedReferralDate() {
        if (referralDate != null) {
            return referralDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        return "No date set";
    }

    public String getFormattedPreferredDate() {
        if (preferredAppointmentDate != null) {
            return preferredAppointmentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        return "Not specified";
    }

    // Get priority level for sorting/display
    public int getPriorityLevel() {
        return switch (urgencyLevel.toUpperCase()) {
            case "EMERGENCY" -> 4;
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            case "LOW" -> 1;
            default -> 2; // Default to medium
        };
    }

    // Get status color for UI display
    public String getStatusColor() {
        return switch (status.toUpperCase()) {
            case "PENDING" -> "#FFA500"; // Orange
            case "CONFIRMED" -> "#4CAF50"; // Green
            case "COMPLETED" -> "#2196F3"; // Blue
            case "CANCELLED" -> "#F44336"; // Red
            default -> "#999999"; // Gray
        };
    }

    // Get urgency color for UI display
    public String getUrgencyColor() {
        return switch (urgencyLevel.toUpperCase()) {
            case "EMERGENCY" -> "#FF0000"; // Red
            case "HIGH" -> "#FF6600"; // Dark Orange
            case "MEDIUM" -> "#FFA500"; // Orange
            case "LOW" -> "#4CAF50"; // Green
            default -> "#999999"; // Gray
        };
    }

    // Get summary for quick display
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Patient: ").append(patientName != null ? patientName : "Unknown");
        summary.append(" | Hospital: ").append(hospitalName != null ? hospitalName : "N/A");
        summary.append(" | Urgency: ").append(urgencyLevel);
        summary.append(" | Status: ").append(status);
        return summary.toString();
    }

    // Custom toString method for detailed display
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Hospital Referral:\n");
        sb.append("ID: ").append(referralId).append("\n");
        sb.append("Patient: ").append(patientName != null ? patientName : "N/A").append("\n");
        sb.append("Referring Doctor: Dr. ").append(referringDoctorName != null ? referringDoctorName : "N/A").append("\n");
        sb.append("Hospital: ").append(hospitalName != null ? hospitalName : "N/A").append("\n");
        sb.append("Department: ").append(department != null ? department : "N/A").append("\n");
        sb.append("Specialty: ").append(specialtyRequired != null ? specialtyRequired : "N/A").append("\n");
        sb.append("Referral Date: ").append(getFormattedReferralDate()).append("\n");
        sb.append("Preferred Date: ").append(getFormattedPreferredDate()).append("\n");
        sb.append("Urgency: ").append(urgencyLevel).append(" | Status: ").append(status).append("\n\n");
        
        if (reasonForReferral != null && !reasonForReferral.trim().isEmpty()) {
            sb.append("Reason for Referral:\n").append(reasonForReferral).append("\n\n");
        }
        
        if (contactNumber != null && !contactNumber.trim().isEmpty()) {
            sb.append("Contact: ").append(contactNumber).append("\n");
        }
        
        if (notes != null && !notes.trim().isEmpty()) {
            sb.append("Notes: ").append(notes).append("\n");
        }
        
        return sb.toString();
    }

    // Equals and hashCode for object comparison
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        HospitalReferral that = (HospitalReferral) obj;
        return referralId == that.referralId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(referralId);
    }
}