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

import database.DatabaseHelper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * @author USER
 */
public class PrescriptionRefillController {

    @FXML private TextField patientNameField;
    @FXML private TextField medicationField;
    @FXML private TextField quantityField;
    @FXML private TextArea noteField;
    @FXML private Label statusLabel;

    private String lastRefillDetails = "";
    private int userId; // Store logged-in user ID

    // ===== SET PATIENT INFO FROM DASHBOARD =====

    /**
     *
     * @param id
     * @param name
     */
    public void setPatientInfo(int id, String name) {
        this.userId = id;
        this.patientNameField.setText(name); // Auto-populate
        this.patientNameField.setDisable(true); // Prevent editing
    }

    // ============ HANDLE SUBMIT ============
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

        // ===== INSERT INTO DATABASE WITH user_id =====
        String insertSQL = """
            INSERT INTO prescription_refills 
            (user_id, patient_name, medication_name, quantity, notes, status)
            VALUES (?, ?, ?, ?, ?, 'Pending')
        """;

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            pstmt.setInt(1, userId); // Store user_id
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

    // ============ HELPER: SAVE DETAILS ============
    private void saveLastRefillDetails(String patient, String medication, String quantity, String note) {
        lastRefillDetails = """
            ✅ Prescription Refill Submitted!

            • Patient Name: %s
            • Medication: %s
            • Quantity: %s
            • Note: %s
        """.formatted(patient, medication, quantity, note.isEmpty() ? "N/A" : note);
    }

    // ============ HELPER: CLEAR FORM ============
    private void clearForm() {
        medicationField.clear();
        quantityField.clear();
        noteField.clear();
    }

    // ============ POPUP CONFIRMATION ============
    private void showConfirmationPopup() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Refill Submitted");
        alert.setHeaderText("Your prescription refill request has been saved.");

        ButtonType viewBtn = new ButtonType("View Request");
        ButtonType okBtn = new ButtonType("OK", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(viewBtn, okBtn);

        alert.showAndWait().ifPresent(type -> {
            if (type == viewBtn) {
                showRefillDetailsPopup();
            }
        });
    }

    // ============ SHOW DETAILS POPUP ============
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

    // ============ NAVIGATION ============
    @FXML
    private void goBackToDashboard(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TeleHealth - Dashboard");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
