package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.SessionData;
import javafx.scene.Node;
import javafx.fxml.FXMLLoader;



public class PrescriptionRefillController {

    @FXML private TextField patientNameField;
    @FXML private TextField medicationField;
    @FXML private TextField quantityField;
    @FXML private TextArea noteField;
    @FXML private Label statusLabel;

    private String lastRefillDetails = "";

    @FXML
    private void handleSubmit() {
        String patient = patientNameField.getText();
        String medication = medicationField.getText();
        String quantity = quantityField.getText();
        String note = noteField.getText();

        if (patient.isEmpty() || medication.isEmpty() || quantity.isEmpty()) {
            statusLabel.setText("Please fill all required fields.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // Save to SessionData
        SessionData.patientName = patient; // in case not set before
        SessionData.medicationName = medication;
        SessionData.medicationQuantity = quantity;

        lastRefillDetails = """
            ✅ Prescription Refill Submitted!

            • Patient Name: %s
            • Medication: %s
            • Quantity: %s
            • Note: %s
        """.formatted(
                SessionData.patientName,
                SessionData.medicationName,
                SessionData.medicationQuantity,
                note.isEmpty() ? "N/A" : note
        );

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Refill Submitted");
        alert.setHeaderText("Your request has been submitted.");

        ButtonType viewBtn = new ButtonType("View Request");
        ButtonType okBtn = new ButtonType("OK", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(viewBtn, okBtn);

        alert.showAndWait().ifPresent(type -> {
            if (type == viewBtn) {
                showRefillDetailsPopup();
            }
        });

        statusLabel.setText("Refill submitted.");
        statusLabel.setStyle("-fx-text-fill: green;");
    }

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