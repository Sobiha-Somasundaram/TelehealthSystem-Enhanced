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
import models.Appointment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

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

    // ===== Consultation Mode =====
    @FXML
    private RadioButton rbVideo;
    @FXML
    private RadioButton rbAudio;
    @FXML
    private ToggleGroup consultationModeGroup;

    // ===== Upcoming Appointments Table =====
    @FXML
    private TableView<Appointment> upcomingAppointmentsTable;
    @FXML
    private TableColumn<Appointment, String> colDoctor;
    @FXML
    private TableColumn<Appointment, String> colDate;
    @FXML
    private TableColumn<Appointment, String> colTime;
    @FXML
    private TableColumn<Appointment, String> colMode;
    @FXML
    private TableColumn<Appointment, String> colStatus;

    private int patientId;         
    private String patientName;     
    private String userRole;        

    private ObservableList<String> doctorList = FXCollections.observableArrayList();

    // ================== Initialization ==================
    @FXML
    public void initialize() {
        loadDoctors();

        txtPatientName.setEditable(false);
        txtPatientName.setVisible(true);

        // Fixed time slots
        timeSlotBox.getItems().addAll(
                "09:00 AM", "10:00 AM", "11:00 AM",
                "12:00 PM", "02:00 PM", "03:00 PM", "04:00 PM"
        );

        // Initialize ToggleGroup
        consultationModeGroup = new ToggleGroup();
        rbVideo.setToggleGroup(consultationModeGroup);
        rbAudio.setToggleGroup(consultationModeGroup);
        rbVideo.setSelected(true); // default

        // ===== Initialize TableView columns =====
        colDoctor.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getSpecialistName()));
        colDate.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getAppointmentDate() != null ? cell.getValue().getFormattedDate() : "N/A"));
        colTime.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getTimeSlot()));
        colMode.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getConsultationType()));
        colStatus.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getStatus()));
    }

    // ================== Set Patient Info ==================
    public void setPatientInfo(int id, String name, String role) {
        this.patientId = id;
        this.patientName = name;
        this.userRole = role;
        txtPatientName.setText(name);

        // Load upcoming appointments after patient info is set
        loadUpcomingAppointments();
    }

    // ================== Load Doctors ==================
    private void loadDoctors() {
        String sql = "SELECT name FROM users WHERE role='Doctor'";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            doctorList.clear();
            while (rs.next()) {
                doctorList.add(rs.getString("name"));
            }
            comboDoctor.setItems(doctorList);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load doctors: " + e.getMessage());
        }
    }

    // ================== Book Appointment ==================
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

        // ===== Parse AM/PM time to 24-hour LocalTime =====
        LocalTime time;
        try {
            String normalizedTime = selectedTime.trim().toUpperCase();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a", java.util.Locale.ENGLISH);
            time = LocalTime.parse(normalizedTime, formatter);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Time",
                    "Selected time is invalid. Please choose a valid time slot.");
            return;
        }

        // ===== Get Selected Consultation Mode =====
        String consultationMode = ((RadioButton) consultationModeGroup.getSelectedToggle()).getText();

        String sql = "INSERT INTO bookings (patient_id, doctor_id, appointment_date, appointment_time, symptoms, consultation_mode, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, 'Pending')";

        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            ps.setInt(2, doctorId);
            ps.setDate(3, java.sql.Date.valueOf(date));
            ps.setTime(4, java.sql.Time.valueOf(time));
            ps.setString(5, symptoms);
            ps.setString(6, consultationMode);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Appointment booked successfully!");
                clearForm();
                loadUpcomingAppointments();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save booking: " + e.getMessage());
        }
    }

    // ================== Helper: Get Doctor ID ==================
    private int getDoctorIdByName(String doctorName) {
        String sql = "SELECT user_id FROM users WHERE name=?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

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

    // ================== Load Upcoming Appointments ==================
    private void loadUpcomingAppointments() {
        String sql = """
        SELECT b.booking_id,
               d.name AS doctor_name,
               b.appointment_date,
               b.appointment_time,
               b.consultation_mode,
               b.status
        FROM bookings b
        JOIN users d ON b.doctor_id = d.user_id
        WHERE b.patient_id = ?
        ORDER BY b.appointment_date DESC, b.appointment_time DESC
    """;

        ObservableList<Appointment> appointments = FXCollections.observableArrayList();

        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                appointments.add(new Appointment(
                        rs.getInt("booking_id"),
                        null, 
                        rs.getString("doctor_name"),
                        rs.getDate("appointment_date").toLocalDate(),
                        rs.getTime("appointment_time").toString(),
                        rs.getString("status"),
                        rs.getString("consultation_mode"),
                        null // notes
                ));
            }

            upcomingAppointmentsTable.setItems(appointments);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Failed to load upcoming appointments: " + e.getMessage());
        }
    }

    // ================== Back to Dashboard ==================
    @FXML
    private void goBackToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Dashboard.fxml"));
            Parent root = loader.load();

            DashboardController controller = loader.getController();
            controller.setUserInfo(patientId, patientName, userRole);

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TeleHealth System - Dashboard");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Unable to return to dashboard: " + e.getMessage());
        }
    }

    // ================== Clear Form ==================
    private void clearForm() {
        comboDoctor.getSelectionModel().clearSelection();
        datePicker.setValue(null);
        timeSlotBox.getSelectionModel().clearSelection();
        txtSymptoms.clear();
        rbVideo.setSelected(true);
    }

    // ================== Show Alert ==================
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
