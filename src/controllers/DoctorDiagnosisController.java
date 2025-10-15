package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.fxml.FXMLLoader;

import models.Diagnosis;
import models.Appointment;
import database.DatabaseHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Controller for doctors to record diagnosis, prescription, and treatment information
 * Allows recording diagnosis results for patient consultations
 */
public class DoctorDiagnosisController {

    @FXML private ComboBox<String> patientComboBox;
    @FXML private TextField appointmentIdField;
    @FXML private TextField doctorNameField;
    @FXML private TextArea symptomsArea;
    @FXML private TextArea diagnosisArea;
    @FXML private TextArea prescriptionArea;
    @FXML private TextArea treatmentPlanArea;
    @FXML private TextArea followUpArea;
    @FXML private ComboBox<String> severityBox;
    @FXML private ComboBox<String> statusBox;
    @FXML private Label statusLabel;
    @FXML private ListView<String> recentDiagnosesView;

    @FXML
    public void initialize() {
        setupComboBoxes();
        loadPatients();
        loadRecentDiagnoses();
        
        // Set default values
        doctorNameField.setText("Dr. System"); // Default doctor name
        appointmentIdField.setText("0"); // Will be updated when patient is selected
    }

    private void setupComboBoxes() {
        // Severity levels
        severityBox.setItems(FXCollections.observableArrayList(
            "MILD", "MODERATE", "SEVERE"
        ));
        severityBox.setValue("MODERATE");

        // Status options
        statusBox.setItems(FXCollections.observableArrayList(
            "ACTIVE", "ONGOING", "RESOLVED"
        ));
        statusBox.setValue("ACTIVE");

        // Add listener for patient selection
        patientComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                loadPatientInfo(newValue);
            }
        });
    }

    private void loadPatients() {
        try (Connection conn = DatabaseHelper.connect()) {
            String query = """
                SELECT DISTINCT patient_name FROM appointments 
                WHERE status = 'SCHEDULED' OR status = 'COMPLETED'
                ORDER BY patient_name
            """;
            
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                patientComboBox.getItems().add(rs.getString("patient_name"));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading patients");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void loadPatientInfo(String patientName) {
        try (Connection conn = DatabaseHelper.connect()) {
            String query = """
                SELECT id FROM appointments 
                WHERE patient_name = ? AND (status = 'SCHEDULED' OR status = 'COMPLETED')
                ORDER BY appointment_date DESC LIMIT 1
            """;
            
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, patientName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                appointmentIdField.setText(String.valueOf(rs.getInt("id")));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadRecentDiagnoses() {
        recentDiagnosesView.getItems().clear();
        
        try (Connection conn = DatabaseHelper.connect()) {
            String query = """
                SELECT patient_name, diagnosis_text, recorded_date 
                FROM diagnoses 
                ORDER BY recorded_date DESC LIMIT 10
            """;
            
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String item = String.format("%s - %s (%s)", 
                    rs.getString("patient_name"),
                    rs.getString("diagnosis_text").substring(0, Math.min(30, rs.getString("diagnosis_text").length())) + "...",
                    rs.getString("recorded_date")
                );
                recentDiagnosesView.getItems().add(item);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSaveDiagnosis() {
        if (!validateInput()) {
            return;
        }

        try {
            Diagnosis diagnosis = createDiagnosisFromForm();
            saveDiagnosisToDatabase(diagnosis);
            
            showSuccessAlert();
            clearForm();
            loadRecentDiagnoses();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save diagnosis: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        if (patientComboBox.getValue() == null || patientComboBox.getValue().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a patient.");
            return false;
        }

        if (doctorNameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter doctor name.");
            return false;
        }

        if (diagnosisArea.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a diagnosis.");
            return false;
        }

        return true;
    }

    private Diagnosis createDiagnosisFromForm() {
        return new Diagnosis(
            Integer.parseInt(appointmentIdField.getText()),
            patientComboBox.getValue(),
            doctorNameField.getText(),
            diagnosisArea.getText(),
            symptomsArea.getText(),
            prescriptionArea.getText(),
            treatmentPlanArea.getText()
        );
    }

    private void saveDiagnosisToDatabase(Diagnosis diagnosis) throws Exception {
        try (Connection conn = DatabaseHelper.connect()) {
            String query = """
                INSERT INTO diagnoses (
                    appointment_id, patient_name, doctor_name, diagnosis_text, 
                    symptoms, prescription_details, treatment_plan, 
                    follow_up_instructions, recorded_date, severity, status
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, diagnosis.getAppointmentId());
            pstmt.setString(2, diagnosis.getPatientName());
            pstmt.setString(3, diagnosis.getDoctorName());
            pstmt.setString(4, diagnosis.getDiagnosisText());
            pstmt.setString(5, diagnosis.getSymptoms());
            pstmt.setString(6, diagnosis.getPrescriptionDetails());
            pstmt.setString(7, diagnosis.getTreatmentPlan());
            pstmt.setString(8, followUpArea.getText());
            pstmt.setString(9, LocalDate.now().toString());
            pstmt.setString(10, severityBox.getValue());
            pstmt.setString(11, statusBox.getValue());

            pstmt.executeUpdate();
        }
    }

    @FXML
    private void handleClearForm() {
        clearForm();
    }

    private void clearForm() {
        patientComboBox.setValue(null);
        appointmentIdField.setText("0");
        symptomsArea.clear();
        diagnosisArea.clear();
        prescriptionArea.clear();
        treatmentPlanArea.clear();
        followUpArea.clear();
        severityBox.setValue("MODERATE");
        statusBox.setValue("ACTIVE");
        statusLabel.setText("");
    }

    @FXML
    private void handleGeneratePrescription() {
        if (diagnosisArea.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "Please enter a diagnosis first.");
            return;
        }

        // Generate basic prescription template based on diagnosis
        String diagnosis = diagnosisArea.getText().toLowerCase();
        String prescription = generatePrescriptionSuggestion(diagnosis);
        
        if (!prescription.isEmpty()) {
            prescriptionArea.setText(prescription);
            statusLabel.setText("Prescription template generated");
            statusLabel.setStyle("-fx-text-fill: blue;");
        }
    }

    private String generatePrescriptionSuggestion(String diagnosis) {
        // Simple prescription generation based on keywords
        StringBuilder prescription = new StringBuilder();
        
        if (diagnosis.contains("fever") || diagnosis.contains("temperature")) {
            prescription.append("• Paracetamol 500mg - Take 1 tablet every 6 hours as needed\n");
        }
        
        if (diagnosis.contains("cough") || diagnosis.contains("cold")) {
            prescription.append("• Cough syrup - 10ml three times daily\n");
            prescription.append("• Throat lozenges - As needed for throat irritation\n");
        }
        
        if (diagnosis.contains("pain") || diagnosis.contains("ache")) {
            prescription.append("• Ibuprofen 400mg - Take 1 tablet twice daily with food\n");
        }
        
        if (diagnosis.contains("infection") || diagnosis.contains("bacterial")) {
            prescription.append("• Antibiotic (consult pharmacy) - As per standard dosage\n");
        }
        
        if (prescription.length() == 0) {
            prescription.append("• Medication to be prescribed based on specific diagnosis\n");
            prescription.append("• Follow up consultation recommended\n");
        }
        
        prescription.append("\nNote: Take medications as prescribed and complete full course.");
        
        return prescription.toString();
    }

    @FXML
    private void handleGenerateTreatmentPlan() {
        if (diagnosisArea.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "Please enter a diagnosis first.");
            return;
        }

        String treatmentPlan = generateTreatmentPlan();
        treatmentPlanArea.setText(treatmentPlan);
        
        statusLabel.setText("Treatment plan template generated");
        statusLabel.setStyle("-fx-text-fill: blue;");
    }

    private String generateTreatmentPlan() {
        StringBuilder plan = new StringBuilder();
        
        plan.append("TREATMENT PLAN:\n\n");
        plan.append("1. IMMEDIATE CARE:\n");
        plan.append("   • Rest and adequate sleep\n");
        plan.append("   • Stay hydrated\n");
        plan.append("   • Follow prescribed medication\n\n");
        
        plan.append("2. FOLLOW-UP CARE:\n");
        plan.append("   • Monitor symptoms daily\n");
        plan.append("   • Return if symptoms worsen\n");
        plan.append("   • Schedule follow-up in 1 week\n\n");
        
        plan.append("3. LIFESTYLE RECOMMENDATIONS:\n");
        plan.append("   • Maintain healthy diet\n");
        plan.append("   • Avoid strenuous activities\n");
        plan.append("   • Practice good hygiene\n\n");
        
        plan.append("4. WARNING SIGNS:\n");
        plan.append("   • Contact immediately if severe symptoms develop\n");
        plan.append("   • Emergency care if condition deteriorates\n");
        
        return plan.toString();
    }

    @FXML
    private void handleViewPatientHistory() {
        String selectedPatient = patientComboBox.getValue();
        if (selectedPatient == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a patient first.");
            return;
        }

        showPatientHistory(selectedPatient);
    }

    private void showPatientHistory(String patientName) {
        try (Connection conn = DatabaseHelper.connect()) {
            String query = """
                SELECT * FROM diagnoses 
                WHERE patient_name = ? 
                ORDER BY recorded_date DESC
            """;
            
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, patientName);
            ResultSet rs = pstmt.executeQuery();

            StringBuilder history = new StringBuilder();
            history.append("PATIENT MEDICAL HISTORY: ").append(patientName).append("\n\n");

            while (rs.next()) {
                history.append("Date: ").append(rs.getString("recorded_date")).append("\n");
                history.append("Doctor: ").append(rs.getString("doctor_name")).append("\n");
                history.append("Diagnosis: ").append(rs.getString("diagnosis_text")).append("\n");
                history.append("Status: ").append(rs.getString("status")).append("\n");
                history.append("Severity: ").append(rs.getString("severity")).append("\n");
                history.append("-".repeat(50)).append("\n\n");
            }

            if (history.toString().equals("PATIENT MEDICAL HISTORY: " + patientName + "\n\n")) {
                history.append("No previous diagnoses found for this patient.");
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Patient History");
            alert.setHeaderText("Medical History for " + patientName);

            TextArea textArea = new TextArea(history.toString());
            textArea.setWrapText(true);
            textArea.setEditable(false);
            textArea.setPrefHeight(400);

            alert.getDialogPane().setContent(textArea);
            alert.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load patient history: " + e.getMessage());
        }
    }

    private void showSuccessAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText("Diagnosis Saved");
        alert.setContentText("The diagnosis has been successfully recorded for " + patientComboBox.getValue());

        ButtonType viewBtn = new ButtonType("View Record");
        ButtonType closeBtn = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(viewBtn, closeBtn);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == viewBtn) {
            showPatientHistory(patientComboBox.getValue());
        }

        statusLabel.setText("Diagnosis saved successfully");
        statusLabel.setStyle("-fx-text-fill: green;");
    }

    @FXML
    private void goBackToDashboard(javafx.event.ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Dashboard.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TeleHealth - Dashboard");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}