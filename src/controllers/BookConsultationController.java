package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import javafx.stage.Stage;





import java.time.LocalDate;
import utils.SessionData;



public class BookConsultationController {

    @FXML private TextField patientNameField;
    @FXML private TextField specialistField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> timeSlotBox;
    @FXML private Label statusLabel;
    

    private String lastConfirmationDetails = "";

    @FXML
    public void initialize() {
        // Populate time slots
        timeSlotBox.getItems().addAll("10:00 AM", "11:00 AM", "12:00 PM", "2:00 PM", "4:00 PM");
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

    @FXML
    private void handleBook() {
        // Declare local variables first
        String patient = patientNameField.getText();
        String specialist = specialistField.getText();
        LocalDate date = datePicker.getValue();
        String time = timeSlotBox.getValue();

        // Validation
        if (patient.isEmpty() || specialist.isEmpty() || date == null || time == null) {
            statusLabel.setText("Please fill all fields.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // ✅ Now assign values to SessionData after the variables are declared
        SessionData.patientName = patient;
        SessionData.specialistName = specialist;
        SessionData.appointmentDate = date;
        SessionData.appointmentTime = time;

        // Generate confirmation string
        lastConfirmationDetails = """
            ✅ Booking Confirmed!

            • Patient Name: %s
            • Specialist: Dr. %s
            • Date: %s
            • Time: %s
        """.formatted(SessionData.patientName, SessionData.specialistName, SessionData.appointmentDate, SessionData.appointmentTime);

        // Confirmation alert
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Appointment Booked");
        alert.setHeaderText("Booking Successful");
        alert.setContentText("Your appointment has been booked.");

        ButtonType viewBtn = new ButtonType("View Appointment");
        ButtonType okBtn = new ButtonType("OK", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(viewBtn, okBtn);

        alert.showAndWait().ifPresent(type -> {
            if (type == viewBtn) {
                showAppointmentPopup();
            }
        });

        statusLabel.setText("Appointment booked.");
        statusLabel.setStyle("-fx-text-fill: green;");
    }

    private void showAppointmentPopup() {
        Alert viewAlert = new Alert(Alert.AlertType.INFORMATION);
        viewAlert.setTitle("Appointment Details");
        viewAlert.setHeaderText("Here is your booking information:");

        TextArea textArea = new TextArea(lastConfirmationDetails);
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
   
}