package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.fxml.FXMLLoader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import database.DatabaseHelper;
import java.sql.*;
import javafx.event.ActionEvent;
import javafx.beans.property.SimpleStringProperty;

/**
 * PrescriptionRefillController
 * ---------------------------------------------------------------
 * Allows a logged-in patient to submit prescription refill requests.
 * Displays existing refill history for that patient.
 */
public class PrescriptionRefillController {

    @FXML private TextField patientNameField;
    @FXML private TextField medicationField;
    @FXML private TextField quantityField;
    @FXML private TextArea noteField;
    @FXML private Label statusLabel;

    // TableView and Columns
    @FXML private TableView<RefillRecord> refillHistoryTable;
    @FXML private TableColumn<RefillRecord, String> colMedication;
    @FXML private TableColumn<RefillRecord, String> colQuantity;
    @FXML private TableColumn<RefillRecord, String> colNotes;
    @FXML private TableColumn<RefillRecord, String> colStatus;
    @FXML private TableColumn<RefillRecord, String> colDate;

    private ObservableList<RefillRecord> refillHistory = FXCollections.observableArrayList();

    private String lastRefillDetails = "";
    private int userId;        
    private String patientName;
    private String userRole;

    // ================== SET PATIENT INFO ==================
    public void setPatientInfo(int id, String name, String role) {
        this.userId = id;
        this.patientName = name;
        this.userRole = role;
        this.patientNameField.setText(name);
        this.patientNameField.setDisable(true);
        loadRefillHistory(); // ✅ Load refill history when patient logs in
    }

    // ================== HANDLE SUBMIT ==================
    @FXML
    private void handleSubmit() {
        String medication = medicationField.getText().trim();
        String quantity = quantityField.getText().trim();
        String note = noteField.getText().trim();

        if (medication.isEmpty() || quantity.isEmpty()) {
            statusLabel.setText("⚠️ Please fill all required fields.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(quantity);
        } catch (NumberFormatException e) {
            statusLabel.setText("⚠️ Quantity must be a valid number.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // ✅ Fixed: use request_date (not created_at)
        String insertSQL = """
            INSERT INTO prescription_refills 
            (user_id, patient_name, medication_name, quantity, notes, status, request_date)
            VALUES (?, ?, ?, ?, ?, 'Pending', NOW())
        """;

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, patientNameField.getText());
            pstmt.setString(3, medication);
            pstmt.setInt(4, qty);
            pstmt.setString(5, note);

            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                statusLabel.setText("✅ Prescription refill submitted successfully!");
                statusLabel.setStyle("-fx-text-fill: green;");
                saveLastRefillDetails(patientNameField.getText(), medication, quantity, note);
                showConfirmationPopup();
                clearForm();
                loadRefillHistory(); // ✅ Refresh the table
            } else {
                statusLabel.setText("❌ Failed to save data. Try again.");
                statusLabel.setStyle("-fx-text-fill: red;");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("⚠️ Database error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    // ================== LOAD REFILL HISTORY ==================
    private void loadRefillHistory() {
        refillHistory.clear();

        // ✅ Fixed: use request_date instead of created_at
        String query = """
            SELECT medication_name, quantity, notes, status, request_date
            FROM prescription_refills
            WHERE user_id = ?
            ORDER BY request_date DESC
        """;

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                refillHistory.add(new RefillRecord(
                        rs.getString("medication_name"),
                        String.valueOf(rs.getInt("quantity")),
                        rs.getString("notes"),
                        rs.getString("status"),
                        rs.getString("request_date")
                ));
            }

            setupTable();
            refillHistoryTable.setItems(refillHistory);

        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("⚠️ Failed to load refill history.");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    // ================== SETUP TABLE COLUMNS ==================
    private void setupTable() {
        if (colMedication.getCellValueFactory() == null) {
            colMedication.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().medication()));
            colQuantity.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().quantity()));
            colNotes.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().notes()));
            colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().status()));
            colDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().date()));
        }
    }

    // ================== HELPER: SAVE DETAILS ==================
    private void saveLastRefillDetails(String patient, String medication, String quantity, String note) {
        lastRefillDetails = """
            ✅ Prescription Refill Submitted!

            • Patient Name: %s
            • Medication: %s
            • Quantity: %s
            • Note: %s
        """.formatted(patient, medication, quantity, note.isEmpty() ? "N/A" : note);
    }

    // ================== HELPER: CLEAR FORM ==================
    private void clearForm() {
        medicationField.clear();
        quantityField.clear();
        noteField.clear();
    }

    // ================== POPUP CONFIRMATION ==================
    private void showConfirmationPopup() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Refill Submitted");
        alert.setHeaderText("Your prescription refill request has been saved.");

        ButtonType viewBtn = new ButtonType("View Request");
        ButtonType okBtn = new ButtonType("OK", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(viewBtn, okBtn);

        alert.showAndWait().ifPresent(type -> {
            if (type == viewBtn) showRefillDetailsPopup();
        });
    }

    // ================== SHOW DETAILS POPUP ==================
    private void showRefillDetailsPopup() {
        Alert viewAlert = new Alert(Alert.AlertType.INFORMATION);
        viewAlert.setTitle("Refill Details");
        viewAlert.setHeaderText("Prescription Refill Information:");

        TextArea textArea = new TextArea(lastRefillDetails);
        textArea.setWrapText(true);
        textArea.setEditable(false);
        textArea.setStyle("-fx-font-size: 13px;");
        textArea.setPrefHeight(180);

        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane content = new GridPane();
        content.setMaxWidth(Double.MAX_VALUE);
        content.add(textArea, 0, 0);

        viewAlert.getDialogPane().setContent(content);
        viewAlert.showAndWait();
    }

    // ================== BACK TO DASHBOARD ==================
    @FXML
    private void goBackToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Dashboard.fxml"));
            Parent root = loader.load();

            DashboardController controller = loader.getController();
            controller.setUserInfo(userId, patientName, userRole);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TeleHealth System - Dashboard");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("⚠️ Navigation error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    // ================== INNER CLASS (RECORD MODEL) ==================
    public static class RefillRecord {
        private final String medication;
        private final String quantity;
        private final String notes;
        private final String status;
        private final String date;

        public RefillRecord(String medication, String quantity, String notes, String status, String date) {
            this.medication = medication;
            this.quantity = quantity;
            this.notes = notes;
            this.status = status;
            this.date = date;
        }

        public String medication() { return medication; }
        public String quantity() { return quantity; }
        public String notes() { return notes; }
        public String status() { return status; }
        public String date() { return date; }
    }
}
