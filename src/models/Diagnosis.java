package models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Diagnosis model class representing doctor's diagnosis and treatment records
 * Used for recording patient diagnosis, prescriptions, and treatment plans
 */
public class Diagnosis {
    private int diagnosisId;
    private int appointmentId;
    private String patientName;
    private String doctorName;
    private String diagnosisText;
    private String symptoms;
    private String prescriptionDetails;
    private String treatmentPlan;
    private String followUpInstructions;
    private LocalDate recordedDate;
    private String severity; // MILD, MODERATE, SEVERE
    private String status; // ACTIVE, RESOLVED, ONGOING

    // Default constructor
    public Diagnosis() {
        this.recordedDate = LocalDate.now();
        this.severity = "MODERATE";
        this.status = "ACTIVE";
    }

    // Full constructor
    public Diagnosis(int diagnosisId, int appointmentId, String patientName, String doctorName,
                    String diagnosisText, String symptoms, String prescriptionDetails,
                    String treatmentPlan, String followUpInstructions, LocalDate recordedDate,
                    String severity, String status) {
        this.diagnosisId = diagnosisId;
        this.appointmentId = appointmentId;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.diagnosisText = diagnosisText;
        this.symptoms = symptoms;
        this.prescriptionDetails = prescriptionDetails;
        this.treatmentPlan = treatmentPlan;
        this.followUpInstructions = followUpInstructions;
        this.recordedDate = recordedDate != null ? recordedDate : LocalDate.now();
        this.severity = severity != null ? severity : "MODERATE";
        this.status = status != null ? status : "ACTIVE";
    }

    // Constructor for new diagnosis (without ID)
    public Diagnosis(int appointmentId, String patientName, String doctorName,
                    String diagnosisText, String symptoms, String prescriptionDetails,
                    String treatmentPlan) {
        this.appointmentId = appointmentId;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.diagnosisText = diagnosisText;
        this.symptoms = symptoms;
        this.prescriptionDetails = prescriptionDetails;
        this.treatmentPlan = treatmentPlan;
        this.recordedDate = LocalDate.now();
        this.severity = "MODERATE";
        this.status = "ACTIVE";
        this.followUpInstructions = "";
    }

    // Getters
    public int getDiagnosisId() { return diagnosisId; }
    public int getAppointmentId() { return appointmentId; }
    public String getPatientName() { return patientName; }
    public String getDoctorName() { return doctorName; }
    public String getDiagnosisText() { return diagnosisText; }
    public String getSymptoms() { return symptoms; }
    public String getPrescriptionDetails() { return prescriptionDetails; }
    public String getTreatmentPlan() { return treatmentPlan; }
    public String getFollowUpInstructions() { return followUpInstructions; }
    public LocalDate getRecordedDate() { return recordedDate; }
    public String getSeverity() { return severity; }
    public String getStatus() { return status; }

    // Setters
    public void setDiagnosisId(int diagnosisId) { this.diagnosisId = diagnosisId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public void setDiagnosisText(String diagnosisText) { this.diagnosisText = diagnosisText; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }
    public void setPrescriptionDetails(String prescriptionDetails) { this.prescriptionDetails = prescriptionDetails; }
    public void setTreatmentPlan(String treatmentPlan) { this.treatmentPlan = treatmentPlan; }
    public void setFollowUpInstructions(String followUpInstructions) { this.followUpInstructions = followUpInstructions; }
    public void setRecordedDate(LocalDate recordedDate) { this.recordedDate = recordedDate; }
    public void setSeverity(String severity) { this.severity = severity; }
    public void setStatus(String status) { this.status = status; }

    // Business logic methods
    public boolean isRecent() {
        return recordedDate != null && recordedDate.isAfter(LocalDate.now().minusWeeks(2));
    }

    public boolean requiresFollowUp() {
        return followUpInstructions != null && !followUpInstructions.trim().isEmpty();
    }

    public boolean isSevere() {
        return "SEVERE".equalsIgnoreCase(severity);
    }

    public boolean isResolved() {
        return "RESOLVED".equalsIgnoreCase(status);
    }

    public void markAsResolved() {
        this.status = "RESOLVED";
    }

    public void markAsOngoing() {
        this.status = "ONGOING";
    }

    // Formatted date for display
    public String getFormattedDate() {
        if (recordedDate != null) {
            return recordedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        return "No date recorded";
    }

    // Get summary for quick display
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Patient: ").append(patientName != null ? patientName : "Unknown");
        summary.append(" | Date: ").append(getFormattedDate());
        summary.append(" | Status: ").append(status);
        summary.append(" | Severity: ").append(severity);
        return summary.toString();
    }

    // Custom toString method for detailed display
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Diagnosis Record:\n");
        sb.append("ID: ").append(diagnosisId).append("\n");
        sb.append("Patient: ").append(patientName != null ? patientName : "N/A").append("\n");
        sb.append("Doctor: Dr. ").append(doctorName != null ? doctorName : "N/A").append("\n");
        sb.append("Date: ").append(getFormattedDate()).append("\n");
        sb.append("Status: ").append(status).append(" | Severity: ").append(severity).append("\n\n");
        
        if (symptoms != null && !symptoms.trim().isEmpty()) {
            sb.append("Symptoms:\n").append(symptoms).append("\n\n");
        }
        
        if (diagnosisText != null && !diagnosisText.trim().isEmpty()) {
            sb.append("Diagnosis:\n").append(diagnosisText).append("\n\n");
        }
        
        if (prescriptionDetails != null && !prescriptionDetails.trim().isEmpty()) {
            sb.append("Prescription:\n").append(prescriptionDetails).append("\n\n");
        }
        
        if (treatmentPlan != null && !treatmentPlan.trim().isEmpty()) {
            sb.append("Treatment Plan:\n").append(treatmentPlan).append("\n\n");
        }
        
        if (followUpInstructions != null && !followUpInstructions.trim().isEmpty()) {
            sb.append("Follow-up Instructions:\n").append(followUpInstructions).append("\n");
        }
        
        return sb.toString();
    }

    // Equals and hashCode for object comparison
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Diagnosis diagnosis = (Diagnosis) obj;
        return diagnosisId == diagnosis.diagnosisId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(diagnosisId);
    }
}