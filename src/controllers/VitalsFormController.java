package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;

import database.DatabaseHelper;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class VitalsFormController {

    @FXML private TextField pulseField;
    @FXML private TextField temperatureField;
    @FXML private TextField respirationField;
    @FXML private TextField bpField;
    @FXML private TextField weightField;
    @FXML private TextField heightField;
    @FXML private TextField oxygenField;

    @FXML private TableView<VitalRecord> vitalsTable;
    @FXML private TableColumn<VitalRecord, String> colPulse;
    @FXML private TableColumn<VitalRecord, String> colTemp;
    @FXML private TableColumn<VitalRecord, String> colResp;
    @FXML private TableColumn<VitalRecord, String> colBP;
    @FXML private TableColumn<VitalRecord, String> colWeight;
    @FXML private TableColumn<VitalRecord, String> colHeight;
    @FXML private TableColumn<VitalRecord, String> colOxygen;
    @FXML private TableColumn<VitalRecord, String> colRecordedAt;

    private int userId;
    private String username;
    private String userRole;

    private Map<String, String> vitalData = new HashMap<>();
    private ObservableList<VitalRecord> vitalsList = FXCollections.observableArrayList();

    // ================== SET USER INFO ==================
    public void setUserInfo(int id, String name, String role) {
        this.userId = id;
        this.username = name;
        this.userRole = role;

        loadPreviousVitals();
    }

    // ================== HANDLE SEND ==================
    @FXML
    private void handleSend(javafx.event.ActionEvent event) {
        vitalData.put("Pulse", pulseField.getText());
        vitalData.put("Temperature", temperatureField.getText());
        vitalData.put("Respiration", respirationField.getText());
        vitalData.put("BP", bpField.getText());
        vitalData.put("Weight", weightField.getText());
        vitalData.put("Height", heightField.getText());
        vitalData.put("Oxygen", oxygenField.getText());

        String insertSQL = """
            INSERT INTO vitals_records
            (user_id, pulse, temperature, respiration, blood_pressure, weight, height, oxygen)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, Integer.parseInt(pulseField.getText()));
            pstmt.setBigDecimal(3, new BigDecimal(temperatureField.getText()));
            pstmt.setInt(4, Integer.parseInt(respirationField.getText()));
            pstmt.setString(5, bpField.getText());
            pstmt.setBigDecimal(6, new BigDecimal(weightField.getText()));
            pstmt.setBigDecimal(7, new BigDecimal(heightField.getText()));
            pstmt.setBigDecimal(8, new BigDecimal(oxygenField.getText()));

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        loadPreviousVitals();

        // Navigate to VitalsChart with user info
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/VitalsChart.fxml"));
            Parent root = loader.load();

            VitalsChartController controller = loader.getController();
            controller.setVitalsData(vitalData);
            controller.setUserInfo(userId, username, userRole);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Vitals Chart");
            stage.show();

        } catch (Exception e) { e.printStackTrace(); }

        clearForm();
    }

    private void clearForm() {
        pulseField.clear();
        temperatureField.clear();
        respirationField.clear();
        bpField.clear();
        weightField.clear();
        heightField.clear();
        oxygenField.clear();
    }

    // ================== BACK TO DASHBOARD ==================
    @FXML
    private void goBackToDashboard(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Dashboard.fxml"));
            Parent root = loader.load();

            DashboardController controller = loader.getController();
            controller.setUserInfo(userId, username, userRole);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TeleHealth System - Dashboard");
            stage.show();

        } catch (Exception e) { e.printStackTrace(); }
    }

    // ================== LOAD PREVIOUS VITALS ==================
    private void loadPreviousVitals() {
        vitalsList.clear();

        String query = """
            SELECT pulse, temperature, respiration, blood_pressure, weight, height, oxygen, recorded_at
            FROM vitals_records
            WHERE user_id = ?
            ORDER BY recorded_at DESC
        """;

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                vitalsList.add(new VitalRecord(
                        rs.getString("pulse"),
                        rs.getString("temperature"),
                        rs.getString("respiration"),
                        rs.getString("blood_pressure"),
                        rs.getString("weight"),
                        rs.getString("height"),
                        rs.getString("oxygen"),
                        rs.getString("recorded_at")
                ));
            }

            setupTable();
            vitalsTable.setItems(vitalsList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================== SETUP TABLE ==================
    private void setupTable() {
        colPulse.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().pulse()));
        colTemp.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().temperature()));
        colResp.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().respiration()));
        colBP.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().bloodPressure()));
        colWeight.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().weight()));
        colHeight.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().height()));
        colOxygen.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().oxygen()));
        colRecordedAt.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().recordedAt()));
    }

    // ================== INNER CLASS FOR TABLE RECORD ==================
    public static class VitalRecord {
        private final String pulse;
        private final String temperature;
        private final String respiration;
        private final String bloodPressure;
        private final String weight;
        private final String height;
        private final String oxygen;
        private final String recordedAt;

        public VitalRecord(String pulse, String temperature, String respiration, String bloodPressure,
                           String weight, String height, String oxygen, String recordedAt) {
            this.pulse = pulse;
            this.temperature = temperature;
            this.respiration = respiration;
            this.bloodPressure = bloodPressure;
            this.weight = weight;
            this.height = height;
            this.oxygen = oxygen;
            this.recordedAt = recordedAt;
        }

        public String pulse() { return pulse; }
        public String temperature() { return temperature; }
        public String respiration() { return respiration; }
        public String bloodPressure() { return bloodPressure; }
        public String weight() { return weight; }
        public String height() { return height; }
        public String oxygen() { return oxygen; }
        public String recordedAt() { return recordedAt; }
    }
}
