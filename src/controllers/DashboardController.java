package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import java.io.IOException;

public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private VBox patientSection;
    @FXML private VBox staffSection;
    @FXML private VBox doctorSection;

    @FXML private Button bookConsultationBtn;
    @FXML private Button prescriptionRefillBtn;
    @FXML private Button vitalsMonitoringBtn;
    @FXML private Button healthReportBtn;
    @FXML private Button staffBookingBtn;
    @FXML private Button doctorDiagnosisBtn;
    @FXML private Button hospitalBookingBtn;

    private int userId;
    private String username;
    private String userRole;

    // ================== Set User Info ==================
    public void setUserInfo(int id, String name, String role) {
        this.userId = id;
        this.username = name;
        this.userRole = role;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + username + "!");
        }
        applyRolePermissions();
    }

    // ================== Role Permissions ==================
    private void applyRolePermissions() {
        disableAllButtons();
        if (userRole == null) return;

        switch (userRole.toLowerCase()) {
            case "patient": enablePatientButtons(); break;
            case "doctor": enableDoctorButtons(); break;
            case "admin":
            case "staff": enableStaffButtons(); break;
            default: enablePatientButtons();
        }
    }

    private void disableAllButtons() {
        bookConsultationBtn.setDisable(true);
        prescriptionRefillBtn.setDisable(true);
        vitalsMonitoringBtn.setDisable(true);
        healthReportBtn.setDisable(true);
        staffBookingBtn.setDisable(true);
        doctorDiagnosisBtn.setDisable(true);
        hospitalBookingBtn.setDisable(true);
    }

    private void enablePatientButtons() {
        bookConsultationBtn.setDisable(false);
        prescriptionRefillBtn.setDisable(false);
        vitalsMonitoringBtn.setDisable(false);
        healthReportBtn.setDisable(false);
    }

    private void enableStaffButtons() { staffBookingBtn.setDisable(false); }
    private void enableDoctorButtons() {
        doctorDiagnosisBtn.setDisable(false);
        hospitalBookingBtn.setDisable(false);
    }

    // ================== Navigation Methods ==================
    @FXML
    private void goToBookConsultation(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/BookConsultation.fxml"));
            Parent root = loader.load();

            controllers.BookConsultationController controller = loader.getController();
            controller.setPatientInfo(userId, username, userRole);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Book Consultation");
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void goToVitalsMonitoring(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/VitalsForm.fxml"));
            Parent root = loader.load();

            VitalsFormController controller = loader.getController();
            controller.setUserInfo(userId, username, userRole);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Vitals Monitoring");
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void goToPrescriptionRefill(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/PrescriptionRefill.fxml"));
            Parent root = loader.load();

            PrescriptionRefillController controller = loader.getController();
            controller.setPatientInfo(userId, username, userRole);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Prescription Refill");
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void goToHealthReport() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/HealthReport.fxml"));
            Parent root = loader.load();

            HealthReportController controller = loader.getController();
            controller.setUserId(userId);

            Stage stage = (Stage) healthReportBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TeleHealth System - Health Report");
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void goToStaffBooking(ActionEvent event) {
        navigateToView("/views/StaffBooking.fxml", "Staff Booking Management", event);
    }

    @FXML
    private void goToDoctorDiagnosis(ActionEvent event) {
        navigateToView("/views/DoctorDiagnosis.fxml", "Doctor Diagnosis", event);
    }

    @FXML
    private void goToHospitalBooking(ActionEvent event) {
        navigateToView("/views/HospitalBooking.fxml", "Hospital Booking", event);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        navigateToView("/views/Login.fxml", "TeleHealth - Login", event);
    }

    private void navigateToView(String fxmlPath, String title, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    public void initialize() { disableAllButtons(); }
}
