package controllers;

import database.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * BookConsultationController
 * ---------------------------------------------------------------
 * Allows a logged-in patient to book an appointment with a doctor.
 * Patient name is auto-filled and non-editable.
 * Doctor list is populated dynamically from users(role='Doctor').
 * Appointment time is selected from fixed time slots.
 */
public class BookConsultationController {

    @FXML
    private TextField txtPatientName;

    @FXML
    private ComboBox<String> comboDoctor;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TextArea txtSymptoms;

    @FXML
    private Button btnBook;

    @FXML
    private ComboBox<String> timeSlotBox;

    private int patientId;          // passed from login
    private String patientName;     // passed from login

    private ObservableList<String> doctorList = FXCollections.observableArrayList();

    /**
     * Initialize method runs automatically after FXML loading.
     */
    @FXML
    public void initialize() {
        loadDoctors();

        // Ensure patient name is non-editable and visible
        txtPatientName.setEditable(false);
        txtPatientName.setVisible(true);

        // Populate fixed time slots
        timeSlotBox.getItems().addAll(
                "09:00 AM", "10:00 AM", "11:00 AM",
                "12:00 PM", "02:00 PM", "03:00 PM", "04:00 PM"
        );
    }

    /**
     * Sets the logged-in patient information.
     * @param id
     * @param name
     */
    public void setPatientInfo(int id, String name) {
        this.patientId = id;
        this.patientName = name;
        txtPatientName.setText(name);
    }

    /**
     * Loads all doctors from the users table into the ComboBox.
     */
    private void loadDoctors() {
        String sql = "SELECT name FROM users WHERE role='Doctor'";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            doctorList.clear();
            while (rs.next()) {
                doctorList.add(rs.getString("name"));
            }
            comboDoctor.setItems(doctorList);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load doctors: " + e.getMessage());
        }
    }

    /**
     * Handles the Book Appointment button click.
     */
    @FXML
    private void handleBookAppointment(ActionEvent event) {
        String selectedDoctor = comboDoctor.getValue();
        LocalDate date = datePicker.getValue();
        String selectedTime = timeSlotBox.getValue();
        String symptoms = txtSymptoms.getText().trim();

        if (selectedDoctor == null || date == null || selectedTime == null || symptoms.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "Please fill all fields before booking.");
            return;
        }

        int doctorId = getDoctorIdByName(selectedDoctor);
        if (doctorId == -1) {
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to find selected doctor in database.");
            return;
        }

        // Convert "hh:mm a" (e.g., 10:00 AM) to LocalTime
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        LocalTime time = LocalTime.parse(selectedTime, formatter);

        String sql = "INSERT INTO bookings (patient_id, doctor_id, appointment_date, appointment_time, symptoms, status) "
                   + "VALUES (?, ?, ?, ?, ?, 'Pending')";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            ps.setInt(2, doctorId);
            ps.setDate(3, java.sql.Date.valueOf(date));
            ps.setTime(4, java.sql.Time.valueOf(time));
            ps.setString(5, symptoms);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Appointment booked successfully!");
                clearForm();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save booking: " + e.getMessage());
        }
    }

    /**
     * Retrieves the doctor_id for a given doctor's name.
     */
    private int getDoctorIdByName(String doctorName) {
        String sql = "SELECT user_id FROM users WHERE name=?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, doctorName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("user_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Navigate back to Dashboard.
     */
    @FXML
    private void goBackToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Dashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TeleHealth System - Dashboard");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Unable to return to dashboard: " + e.getMessage());
        }
    }

    /**
     * Clears form fields after successful booking.
     */
    private void clearForm() {
        comboDoctor.getSelectionModel().clearSelection();
        datePicker.setValue(null);
        timeSlotBox.getSelectionModel().clearSelection();
        txtSymptoms.clear();
    }

    /**
     * Shows an alert dialog.
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
