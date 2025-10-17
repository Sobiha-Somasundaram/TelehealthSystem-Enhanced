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
import database.DatabaseHelper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Map;

public class HealthReportController {

    @FXML
    private TextArea reportArea;

    private Connection connection;
    private int userId;
    private int latestVitalsId;
    private int latestBookingId;
    private int latestPrescriptionId;
    private int generatedByDoctorId = 0;

    private String patientName = "N/A";
    private String doctorName = "N/A";
    private String appointmentDate = "N/A";
    private String appointmentTime = "N/A";

    @FXML
    public void initialize() {
        try {
            connection = DatabaseHelper.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Unable to connect to the database.");
        }
    }


    public void setUserId(int id) {
        this.userId = id;
        generateHealthReport();
    }


    private void generateHealthReport() {
        StringBuilder report = new StringBuilder();

        try {
            fetchPatientDetails(userId);

            fetchLatestRecords(userId);

            report.append("🩺 TELEHEALTH SYSTEM - HEALTH REPORT\n");
            report.append("------------------------------------------\n");
            report.append("Patient Name     : ").append(patientName).append("\n");
            report.append("Doctor/Specialist: Dr. ").append(doctorName).append("\n");
            report.append("Appointment Date : ").append(appointmentDate).append("\n");
            report.append("Appointment Time : ").append(appointmentTime).append("\n");
            report.append("Report Generated : ").append(LocalDateTime.now()).append("\n\n");

            // Prescription Section
            report.append("📦 PRESCRIPTION REFILL\n");
            if (latestPrescriptionId > 0) {
                PreparedStatement ps = connection.prepareStatement(
                        "SELECT medication_name, quantity, status FROM prescription_refills WHERE refill_id = ?");
                ps.setInt(1, latestPrescriptionId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    report.append("• Medication Name : ").append(rs.getString("medication_name")).append("\n");
                    report.append("• Quantity        : ").append(rs.getInt("quantity")).append("\n");
                    report.append("• Status          : ").append(rs.getString("status")).append("\n\n");
                }
            } else {
                report.append("No prescription refill found.\n\n");
            }

            // Vitals Section
            report.append("💓 VITAL SIGNS\n");
            if (latestVitalsId > 0) {
                PreparedStatement ps = connection.prepareStatement(
                        "SELECT pulse, temperature, respiration, blood_pressure, oxygen FROM vitals_records WHERE vitals_id = ?");
                ps.setInt(1, latestVitalsId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    report.append(getVitalLine("Pulse", rs.getString("pulse"), "60–100 bpm"));
                    report.append(getVitalLine("Temperature", rs.getString("temperature"), "36.0–37.5 °C"));
                    report.append(getVitalLine("Respiration", rs.getString("respiration"), "12–20 breaths/min"));
                    report.append(getVitalLine("Blood Pressure", rs.getString("blood_pressure"), "120/80 mmHg"));
                    report.append(getVitalLine("Oxygen", rs.getString("oxygen"), "95–100%"));
                }
            } else {
                report.append("No vitals recorded.\n");
            }

            report.append("\n🩺 DOCTOR'S ADVICE\n");
            report.append(generateDoctorAdvice());

            reportArea.setText(report.toString());

        } catch (Exception e) {
            e.printStackTrace();
            reportArea.setText("⚠️ Failed to generate report. Check database connection or data consistency.");
        }
    }

    private void fetchPatientDetails(int userId) {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DatabaseHelper.getConnection();
            }

            PreparedStatement userStmt = connection.prepareStatement(
                    "SELECT name FROM users WHERE user_id = ?");
            userStmt.setInt(1, userId);
            ResultSet userRs = userStmt.executeQuery();
            if (userRs.next()) {
                patientName = userRs.getString("name");
            }

            PreparedStatement bookingStmt = connection.prepareStatement("""
                SELECT b.appointment_date, b.appointment_time, b.doctor_id, u.name AS doctor_name
                FROM bookings b
                JOIN users u ON b.doctor_id = u.user_id
                WHERE b.patient_id = ?
                ORDER BY b.booking_id DESC
                LIMIT 1
            """);
            bookingStmt.setInt(1, userId);
            ResultSet bookingRs = bookingStmt.executeQuery();
            if (bookingRs.next()) {
                appointmentDate = bookingRs.getString("appointment_date");
                appointmentTime = bookingRs.getString("appointment_time");
                doctorName = bookingRs.getString("doctor_name");
                generatedByDoctorId = bookingRs.getInt("doctor_id");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("⚠️ Failed to fetch patient details from database.");
        }
    }

    /**
     * Fetch latest record IDs for vitals, booking, and prescription refill
     */
    private void fetchLatestRecords(int userId) throws SQLException {
        latestVitalsId = getLatestId("vitals_records", "vitals_id", "user_id", userId);
        latestBookingId = getLatestId("bookings", "booking_id", "patient_id", userId);
        latestPrescriptionId = getLatestId("prescription_refills", "refill_id", "user_id", userId);
    }

    /**
     * Utility: get latest record ID by user_id
     */
    private int getLatestId(String table, String idCol, String userCol, int userId) throws SQLException {
        String sql = "SELECT " + idCol + " FROM " + table + " WHERE " + userCol + " = ? ORDER BY " + idCol + " DESC LIMIT 1";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getInt(1) : 0;
    }

    /**
     * Format vitals line and highlight abnormal values
     */
    private String getVitalLine(String name, String value, String normalRange) {
        if (value == null || value.isEmpty()) {
            return String.format("• %s: Not Provided\n", name);
        }
        double val = 0;
        try {
            val = Double.parseDouble(value);
        } catch (Exception ignored) {
        }

        String alert = switch (name) {
            case "Pulse" ->
                val < 60 ? "Low Alert" : (val > 100 ? "High Alert" : "Normal");
            case "Temperature" ->
                val < 36 ? "Low Alert" : (val > 37.5 ? "High Alert" : "Normal");
            case "Respiration" ->
                val < 12 ? "Low Alert" : (val > 20 ? "High Alert" : "Normal");
            case "Oxygen" ->
                val < 95 ? "Low Alert" : "Normal";
            default ->
                "Normal";
        };

        return String.format("• %s: %s (%s) → %s\n", name, value, normalRange, alert);
    }

    /**
     * Generate simple health advice based on vitals
     */
    private String generateDoctorAdvice() {
        Map<String, String> vitals = SessionData.vitals;
        if (vitals == null || vitals.isEmpty()) {
            return "No vitals submitted.";
        }

        double temp = Double.parseDouble(vitals.getOrDefault("Temperature", "0"));
        double pulse = Double.parseDouble(vitals.getOrDefault("Pulse", "0"));
        double oxygen = Double.parseDouble(vitals.getOrDefault("Oxygen", "0"));

        if (temp > 37.5) {
            return "You have a high temperature. Rest well and stay hydrated.";
        }
        if (pulse > 100) {
            return "Your pulse rate is high. Avoid physical exertion.";
        }
        if (oxygen < 95) {
            return "Your oxygen saturation is low. Breathe deeply and contact a doctor.";
        }

        return "Your vital signs are within normal range. Keep maintaining a healthy lifestyle.";
    }

    /**
     * Save the generated report as text + to database
     */
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
                saveReportToDatabase();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Report saved successfully.");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to save report.");
            }
        }
    }

    /**
     * Insert report text into health_reports table
     */
    private void saveReportToDatabase() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DatabaseHelper.getConnection();
            }

            String sql = """
                INSERT INTO health_reports 
                (user_id, generated_by, summary, latest_vitals_id, latest_booking_id, latest_prescription_id) 
                VALUES (?, ?, ?, ?, ?, ?)
                """;
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setObject(2, generatedByDoctorId > 0 ? generatedByDoctorId : null);
            ps.setString(3, reportArea.getText());
            ps.setObject(4, latestVitalsId > 0 ? latestVitalsId : null);
            ps.setObject(5, latestBookingId > 0 ? latestBookingId : null);
            ps.setObject(6, latestPrescriptionId > 0 ? latestPrescriptionId : null);

            System.out.println("DEBUG: Saving report for user_id = " + userId + ", generated_by = " + generatedByDoctorId);

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save report to the database.");
        }
    }

    /**
     * Utility: show alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }

    /**
     * Go back to dashboard
     */
    @FXML
    private void goBackToDashboard(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Dashboard.fxml"));
            Parent root = loader.load();

            // Pass logged-in user info to DashboardController
            DashboardController controller = loader.getController();
            controller.setUserInfo(userId, patientName, "patient");

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TeleHealth - Dashboard");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Unable to return to dashboard: " + e.getMessage());
        }
    }
}
