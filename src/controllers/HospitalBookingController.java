package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.fxml.FXMLLoader;

import models.HospitalReferral;
import database.DatabaseHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Controller for doctors to book patients at external hospitals/clinics
 * Manages hospital referrals and external appointment bookings
 */
public class HospitalBookingController {

    @FXML private ComboBox<String> patientComboBox;
    @FXML private TextField doctorNameField;
    @FXML private ComboBox<String> hospitalComboBox;
    @FXML private ComboBox<String> departmentComboBox;
    @FXML private TextField specialtyField;
    @FXML private TextArea reasonArea;
    @FXML private ComboBox<String> urgencyBox;
    @FXML private DatePicker preferredDatePicker;
    @FXML private TextField contactNumberField;
    @FXML private TextArea notesArea;
    @FXML private Label statusLabel;
    @FXML private ListView<String> recentReferralsView;

    @FXML
    public void initialize() {
        setupComboBoxes();
        loadPatients();
        loadRecentReferrals();
        
        // Set default values
        doctorNameField.setText("Dr. System");
        preferredDatePicker.setValue(LocalDate.now().plusDays(7)); // Default to next week
    }

    private void setupComboBoxes() {
        // Hospital options
        hospitalComboBox.setItems(FXCollections.observableArrayList(
            "City General Hospital",
            "Regional Medical Center", 
            "St. Mary's Hospital",
            "University Medical Center",
            "Children's Hospital",
            "Heart Specialist Center",
            "Orthopedic Institute",
            "Cancer Treatment Center"
        ));

        // Department options
        departmentComboBox.setItems(FXCollections.observableArrayList(
            "Emergency Department",
            "Cardiology",
            "Neurology", 
            "Orthopedics",
            "Pediatrics",
            "Oncology",
            "Surgery",
            "Radiology",
            "Dermatology",
            "Psychiatry"
        ));

        // Urgency levels
        urgencyBox.setItems(FXCollections.observableArrayList(
            "LOW", "MEDIUM", "HIGH", "EMERGENCY"
        ));
        urgencyBox.setValue("MEDIUM");

        // Update department options based on hospital selection
        hospitalComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            updateDepartmentOptions(newValue);
        });
    }

    private void updateDepartmentOptions(String hospital) {
        if (hospital == null) return;
        
        // Customize departments based on hospital specialization
        switch (hospital) {
            case "Children's Hospital":
                departmentComboBox.setItems(FXCollections.observableArrayList(
                    "Pediatrics", "Pediatric Surgery", "Neonatology", "Child Psychology"
                ));
                break;
            case "Heart Specialist Center":
                departmentComboBox.setItems(FXCollections.observableArrayList(
                    "Cardiology", "Cardiac Surgery", "Interventional Cardiology"
                ));
                break;
            case "Cancer Treatment Center":
                departmentComboBox.setItems(FXCollections.observableArrayList(
                    "Oncology", "Radiation Therapy", "Chemotherapy", "Surgical Oncology"
                ));
                break;
            default:
                // General hospital departments
                departmentComboBox.setItems(FXCollections.observableArrayList(
                    "Emergency Department", "Cardiology", "Neurology", "Orthopedics",
                    "Surgery", "Radiology", "Dermatology"
                ));
        }
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

    private void loadRecentReferrals() {
        recentReferralsView.getItems().clear();
        
        try (Connection conn = DatabaseHelper.connect()) {
            String query = """
                SELECT patient_name, hospital_name, department, urgency_level, status, referral_date 
                FROM hospital_referrals 
                ORDER BY referral_date DESC LIMIT 10
            """;
            
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String item = String.format("%s -> %s (%s) [%s] - %s", 
                    rs.getString("patient_name"),
                    rs.getString("hospital_name"),
                    rs.getString("department"),
                    rs.getString("urgency_level"),
                    rs.getString("referral_date")
                );
                recentReferralsView.getItems().add(item);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBookHospital() {
        if (!validateInput()) {
            return;
        }

        try {
            HospitalReferral referral = createReferralFromForm();
            saveReferralToDatabase(referral);
            
            showSuccessAlert();
            clearForm();
            loadRecentReferrals();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to create hospital referral: " + e.getMessage());
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

        if (hospitalComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a hospital.");
            return false;
        }

        if (departmentComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a department.");
            return false;
        }

        if (reasonArea.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter reason for referral.");
            return false;
        }

        return true;
    }

    private HospitalReferral createReferralFromForm() {
        HospitalReferral referral = new HospitalReferral(
            patientComboBox.getValue(),
            doctorNameField.getText(),
            hospitalComboBox.getValue(),
            departmentComboBox.getValue(),
            reasonArea.getText(),
            urgencyBox.getValue()
        );

        referral.setSpecialtyRequired(specialtyField.getText());
        referral.setPreferredAppointmentDate(preferredDatePicker.getValue());
        referral.setContactNumber(contactNumberField.getText());
        referral.setNotes(notesArea.getText());

        return referral;
    }

    private void saveReferralToDatabase(HospitalReferral referral) throws Exception {
        try (Connection conn = DatabaseHelper.connect()) {
            String query = """
                INSERT INTO hospital_referrals (
                    patient_name, referring_doctor_name, hospital_name, department,
                    specialty_required, reason_for_referral, urgency_level,
                    referral_date, preferred_appointment_date, status,
                    contact_number, notes
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, referral.getPatientName());
            pstmt.setString(2, referral.getReferringDoctorName());
            pstmt.setString(3, referral.getHospitalName());
            pstmt.setString(4, referral.getDepartment());
            pstmt.setString(5, referral.getSpecialtyRequired());
            pstmt.setString(6, referral.getReasonForReferral());
            pstmt.setString(7, referral.getUrgencyLevel());
            pstmt.setString(8, referral.getReferralDate().toString());
            pstmt.setString(9, referral.getPreferredAppointmentDate() != null ? 
                            referral.getPreferredAppointmentDate().toString() : null);
            pstmt.setString(10, referral.getStatus());
            pstmt.setString(11, referral.getContactNumber());
            pstmt.setString(12, referral.getNotes());

            pstmt.executeUpdate();
        }
    }

    @FXML
    private void handleClearForm() {
        clearForm();
    }

    private void clearForm() {
        patientComboBox.setValue(null);
        hospitalComboBox.setValue(null);
        departmentComboBox.setValue(null);
        specialtyField.clear();
        reasonArea.clear();
        urgencyBox.setValue("MEDIUM");
        preferredDatePicker.setValue(LocalDate.now().plusDays(7));
        contactNumberField.clear();
        notesArea.clear();
        statusLabel.setText("");
    }

    @FXML
    private void handleGenerateReferralLetter() {
        if (patientComboBox.getValue() == null || hospitalComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", 
                     "Please select patient and hospital first.");
            return;
        }

        String referralLetter = generateReferralLetter();
        showReferralLetter(referralLetter);
    }

    private String generateReferralLetter() {
        StringBuilder letter = new StringBuilder();
        
        letter.append("HOSPITAL REFERRAL LETTER\n");
        letter.append("=".repeat(50)).append("\n\n");
        letter.append("Date: ").append(LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n\n");
        letter.append("To: ").append(hospitalComboBox.getValue()).append("\n");
        letter.append("Department: ").append(departmentComboBox.getValue()).append("\n\n");
        letter.append("From: ").append(doctorNameField.getText()).append("\n");
        letter.append("TeleHealth System\n\n");
        letter.append("Dear Colleague,\n\n");
        letter.append("RE: ").append(patientComboBox.getValue()).append("\n\n");
        letter.append("I am referring the above patient for your specialist opinion and management.\n\n");
        letter.append("REASON FOR REFERRAL:\n");
        letter.append(reasonArea.getText()).append("\n\n");
        
        if (!specialtyField.getText().trim().isEmpty()) {
            letter.append("SPECIALTY REQUIRED: ").append(specialtyField.getText()).append("\n\n");
        }
        
        letter.append("URGENCY LEVEL: ").append(urgencyBox.getValue()).append("\n");
        
        if (preferredDatePicker.getValue() != null) {
            letter.append("PREFERRED APPOINTMENT DATE: ").append(preferredDatePicker.getValue()).append("\n");
        }
        
        if (!contactNumberField.getText().trim().isEmpty()) {
            letter.append("PATIENT CONTACT: ").append(contactNumberField.getText()).append("\n");
        }
        
        letter.append("\nThank you for your assistance with this patient's care.\n\n");
        letter.append("Yours sincerely,\n");
        letter.append(doctorNameField.getText()).append("\n");
        letter.append("TeleHealth System");
        
        return letter.toString();
    }

    private void showReferralLetter(String letter) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Referral Letter");
        alert.setHeaderText("Generated Referral Letter");

        TextArea textArea = new TextArea(letter);
        textArea.setWrapText(true);
        textArea.setEditable(true); // Allow editing
        textArea.setPrefHeight(500);
        textArea.setPrefWidth(600);

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    @FXML
    private void handleViewReferralStatus() {
        String selectedPatient = patientComboBox.getValue();
        if (selectedPatient == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a patient first.");
            return;
        }

        showReferralHistory(selectedPatient);
    }

    private void showReferralHistory(String patientName) {
        try (Connection conn = DatabaseHelper.connect()) {
            String query = """
                SELECT * FROM hospital_referrals 
                WHERE patient_name = ? 
                ORDER BY referral_date DESC
            """;
            
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, patientName);
            ResultSet rs = pstmt.executeQuery();

            StringBuilder history = new StringBuilder();
            history.append("HOSPITAL REFERRAL HISTORY: ").append(patientName).append("\n\n");

            while (rs.next()) {
                history.append("Referral Date: ").append(rs.getString("referral_date")).append("\n");
                history.append("Hospital: ").append(rs.getString("hospital_name")).append("\n");
                history.append("Department: ").append(rs.getString("department")).append("\n");
                history.append("Urgency: ").append(rs.getString("urgency_level")).append("\n");
                history.append("Status: ").append(rs.getString("status")).append("\n");
                history.append("Reason: ").append(rs.getString("reason_for_referral")).append("\n");
                history.append("-".repeat(50)).append("\n\n");
            }

            if (history.toString().equals("HOSPITAL REFERRAL HISTORY: " + patientName + "\n\n")) {
                history.append("No previous referrals found for this patient.");
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Referral History");
            alert.setHeaderText("Hospital Referral History for " + patientName);

            TextArea textArea = new TextArea(history.toString());
            textArea.setWrapText(true);
            textArea.setEditable(false);
            textArea.setPrefHeight(400);

            alert.getDialogPane().setContent(textArea);
            alert.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load referral history: " + e.getMessage());
        }
    }

    @FXML
    private void handleEmergencyReferral() {
        urgencyBox.setValue("EMERGENCY");
        preferredDatePicker.setValue(LocalDate.now()); // Today
        
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Emergency Referral");
        alert.setHeaderText("Emergency Referral Mode");
        alert.setContentText("This referral has been marked as EMERGENCY priority. " +
                           "Please ensure all required fields are completed and submit immediately.");
        alert.showAndWait();
        
        statusLabel.setText("Emergency referral mode activated");
        statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
    }

    private void showSuccessAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText("Hospital Referral Created");
        alert.setContentText(String.format(
            "Hospital referral has been successfully created for %s at %s (%s department)",
            patientComboBox.getValue(),
            hospitalComboBox.getValue(),
            departmentComboBox.getValue()
        ));

        ButtonType generateLetterBtn = new ButtonType("Generate Letter");
        ButtonType closeBtn = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(generateLetterBtn, closeBtn);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == generateLetterBtn) {
            String letter = generateReferralLetter();
            showReferralLetter(letter);
        }

        statusLabel.setText("Hospital referral created successfully");
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