package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.fxml.FXMLLoader;

import utils.SessionData;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

public class HealthReportController {

    @FXML private TextArea reportArea;

    @FXML
    public void initialize() {
        generateHealthReport();
    }

    private void generateHealthReport() {
        StringBuilder report = new StringBuilder();

        report.append("🩺 TELEHEALTH SYSTEM - HEALTH REPORT\n");
        report.append("------------------------------------------\n");
        report.append("Patient Name     : ").append(SessionData.patientName != null ? SessionData.patientName : "N/A").append("\n");
        report.append("Doctor/Specialist: Dr. ").append(SessionData.specialistName != null ? SessionData.specialistName : "N/A").append("\n");
        report.append("Appointment Date : ").append(SessionData.appointmentDate != null ? SessionData.appointmentDate.toString() : "N/A").append("\n");
        report.append("Appointment Time : ").append(SessionData.appointmentTime != null ? SessionData.appointmentTime : "N/A").append("\n");
        report.append("Report Generated : ").append(LocalDateTime.now()).append("\n\n");

        report.append("📦 PRESCRIPTION REFILL\n");
        report.append("• Medication Name : ").append(SessionData.medicationName != null ? SessionData.medicationName : "N/A").append("\n");
        report.append("• Quantity        : ").append(SessionData.medicationQuantity != null ? SessionData.medicationQuantity : "N/A").append("\n\n");

        report.append("💓 VITAL SIGNS\n");
        if (SessionData.vitals.isEmpty()) {
            report.append("No vitals recorded.\n");
        } else {
            report.append(getVitalLine("Pulse", SessionData.vitals.get("Pulse"), "60–100 bpm"));
            report.append(getVitalLine("Temperature", SessionData.vitals.get("Temperature"), "36.0–37.5 °C"));
            report.append(getVitalLine("Respiration", SessionData.vitals.get("Respiration"), "12–20 breaths/min"));
            report.append(getVitalLine("Oxygen", SessionData.vitals.get("Oxygen"), "95–100%"));
        }

        report.append("\n🩺 DOCTOR'S ADVICE\n");
        report.append(generateDoctorAdvice());

        reportArea.setText(report.toString());
    }

    private String getVitalLine(String name, String value, String normalRange) {
        if (value == null || value.isEmpty()) return String.format("• %s: Not Provided\n", name);
        double val = Double.parseDouble(value);
        String alert = switch (name) {
            case "Pulse" -> val < 60 ? "Low Alert" : (val > 100 ? "High Alert" : "Normal");
            case "Temperature" -> val < 36 ? "Low Alert" : (val > 37.5 ? "High Alert" : "Normal");
            case "Respiration" -> val < 12 ? "Low Alert" : (val > 20 ? "High Alert" : "Normal");
            case "Oxygen" -> val < 95 ? "Low Alert" : "Normal";
            default -> "Normal";
        };

        return String.format("• %s: %s (%s) → %s\n", name, value, normalRange, alert);
    }

    private String generateDoctorAdvice() {
        Map<String, String> vitals = SessionData.vitals;
        if (vitals.isEmpty()) return "No vitals submitted.";

        double temp = Double.parseDouble(vitals.getOrDefault("Temperature", "0"));
        double pulse = Double.parseDouble(vitals.getOrDefault("Pulse", "0"));
        double oxygen = Double.parseDouble(vitals.getOrDefault("Oxygen", "0"));

        if (temp > 37.5) return "You have a high temperature. Rest well and stay hydrated.";
        if (pulse > 100) return "Your pulse rate is high. Avoid physical exertion.";
        if (oxygen < 95) return "Your oxygen saturation is low. Breathe deeply and contact a doctor.";

        return "Your vital signs are within normal range. Keep maintaining a healthy lifestyle.";
    }

    @FXML
    private void handleSaveReport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Health Report");
        fileChooser.setInitialFileName("HealthReport.txt");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        File file = fileChooser.showSaveDialog(new Stage());
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(reportArea.getText());

                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Report saved successfully.", ButtonType.OK);
                alert.setTitle("Success");
                alert.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to save report.", ButtonType.OK);
                alert.showAndWait();
            }
        }
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
    
}
