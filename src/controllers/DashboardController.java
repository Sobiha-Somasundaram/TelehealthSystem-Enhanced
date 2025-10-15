package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import java.io.IOException;

/**
 * Enhanced Dashboard Controller with navigation to all Assessment 2 features
 * Includes patient features, staff management, and doctor functionality
 */
public class DashboardController {

    @FXML
    private Label welcomeLabel;

    private String username;

    // This method is required
    public void setUsername(String name) {
        this.username = name;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + username + "!");
        }
    }
    
    // Patient Features
    @FXML
    private void goToBookConsultation(javafx.event.ActionEvent event) {
        navigateToView("/views/BookConsultation.fxml", "Book Consultation", event);
    }

    @FXML
    private void goToVitalsMonitoring(javafx.event.ActionEvent event) {
        navigateToView("/views/VitalsForm.fxml", "Vitals Monitoring", event);
    }

    @FXML
    private void goToPrescriptionRefill(javafx.event.ActionEvent event) {
        navigateToView("/views/PrescriptionRefill.fxml", "Prescription Refill", event);
    }
    
    @FXML
    private void goToHealthReport(javafx.event.ActionEvent event) {
        navigateToView("/views/HealthReport.fxml", "Health Report", event);
    }

    // Staff Features (NEW for Assessment 2)
    @FXML
    private void goToStaffBooking(javafx.event.ActionEvent event) {
        navigateToView("/views/StaffBooking.fxml", "Staff Booking Management", event);
    }

    // Doctor Features (NEW for Assessment 2)
    @FXML
    private void goToDoctorDiagnosis(javafx.event.ActionEvent event) {
        navigateToView("/views/DoctorDiagnosis.fxml", "Doctor Diagnosis", event);
    }

    @FXML
    private void goToHospitalBooking(javafx.event.ActionEvent event) {
        navigateToView("/views/HospitalBooking.fxml", "Hospital Booking", event);
    }

    // System Functions
    @FXML
    private void handleLogout(ActionEvent event) {
        navigateToView("/views/Login.fxml", "TeleHealth - Login", event);
    }

    // Helper method for navigation
    private void navigateToView(String fxmlPath, String title, javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load view: " + fxmlPath);
        }
    }
    
    @FXML
    public void initialize() {
        if (username != null) {
            welcomeLabel.setText("Welcome, " + username + "!");
        }
    }
}