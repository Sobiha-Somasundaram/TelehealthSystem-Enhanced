package models;

public class HealthReport {
   
    private String doctor;
    private String diagnosis;
    private String prescription;
    private String diagnosisDate;  // or createdAt


    public HealthReport(String date, String doctor, String diagnosis, String prescription) {
        this.diagnosisDate= date;
        this.doctor = doctor;
        this.diagnosis = diagnosis;
        this.prescription = prescription;
    }

    public String getDate() { return diagnosisDate; }
    public String getDoctor() { return doctor; }
    public String getDiagnosis() { return diagnosis; }
    public String getPrescription() { return prescription; }
}
