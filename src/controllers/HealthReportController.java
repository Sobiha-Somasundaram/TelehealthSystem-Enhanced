package controllers;

import database.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.HealthReport;
import models.Diagnosis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * HealthReportController
 * ---------------------------------------------------------------
 * Shows the logged-in patient's health reports.
 * Fetches from diagnoses & health_reports tables.
 */
public class HealthReportController {

    @FXML
    private Label lblPatientName;

    @FXML
    private TableView<HealthReport> tableHealthReport;

    @FXML
    private TableColumn<HealthReport, String> colDate;

    @FXML
    private TableColumn<HealthReport, String> colDoctor;

    @FXML
    private TableColumn<HealthReport, String> colDiagnosis;

    @FXML
    private TableColumn<HealthReport, String> colPrescription;

    private int patientId;
    private String patientName;
    
    private int loggedInUserId;
    private String loggedInUserName;

    private ObservableList<HealthReport> reportList = FXCollections.observableArrayList();

    /**
     * Sets logged-in patient info from Dashboard.
     */
    public void setPatientInfo(int id, String name) {
        this.patientId = id;
        this.patientName = name;
        lblPatientName.setText(name);
        loadHealthReports();
    }

    /**
     * Loads health reports for the logged-in patient.
     */
    private void loadHealthReports() {
        reportList.clear();

        String sql = "SELECT d.diagnosis_id, u1.name AS patient_name, u2.name AS doctor_name, " +
             "d.diagnosis, d.prescription, d.referral, d.created_at AS diagnosis_date " +
             "FROM diagnoses d " +
             "JOIN users u1 ON d.patient_id = u1.user_id " +
             "JOIN users u2 ON d.doctor_id = u2.user_id " +
             "WHERE d.patient_id = ?";



        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                HealthReport report = new HealthReport(
                        rs.getString("diagnosis_date"),
                        rs.getString("doctor_name"),
                        rs.getString("diagnosis_details"),
                        rs.getString("prescription")
                );
                reportList.add(report);
            }

            // Bind table columns
            colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
            colDoctor.setCellValueFactory(new PropertyValueFactory<>("doctor"));
            colDiagnosis.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
            colPrescription.setCellValueFactory(new PropertyValueFactory<>("prescription"));

            tableHealthReport.setItems(reportList);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load health reports: " + e.getMessage());
        }
    }
    
    @FXML
private void goBackToDashboard(ActionEvent event) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Dashboard.fxml"));
        Parent root = loader.load();

        // Pass back the logged-in user details if needed
        DashboardController controller = loader.getController();
        controller.setLoggedInUser(loggedInUserId, loggedInUserName);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("TeleHealth System - Dashboard");
        stage.show();

    } catch (Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Navigation Error");
        alert.setHeaderText(null);
        alert.setContentText("Failed to return to Dashboard: " + e.getMessage());
        alert.showAndWait();
    }
}


    /**
     * Utility alert popup.
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
