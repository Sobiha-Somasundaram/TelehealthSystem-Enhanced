package controllers;

import database.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Appointment;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class BookingDialogController {

    @FXML private Label dialogTitleLabel;
    @FXML private ComboBox<String> patientComboBox;
    @FXML private ComboBox<String> doctorComboBox;
    @FXML private DatePicker appointmentDatePicker;
    @FXML private ComboBox<String> timeSlotComboBox;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private TextArea symptomsTextArea;
    @FXML private Label validationMessageLabel;

    private Stage dialogStage;
    private Appointment booking;
    private boolean isEditMode = false;
    private Runnable onBookingSaved;
    
    // Maps to store user IDs
    private final java.util.Map<String, Integer> patientIdMap = new java.util.HashMap<>();
    private final java.util.Map<String, Integer> doctorIdMap = new java.util.HashMap<>();

    @FXML
    public void initialize() {
        setupComboBoxes();
        loadPatients();
        loadDoctors();
        
        appointmentDatePicker.setValue(LocalDate.now().plusDays(1));
        statusComboBox.setValue("Pending");
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setOnBookingSaved(Runnable callback) {
        this.onBookingSaved = callback;
    }

    public void setBooking(Appointment booking) {
        this.booking = booking;
        this.isEditMode = true;
        
        dialogTitleLabel.setText("Edit Booking");
        
        patientComboBox.setValue(booking.getPatientName());
        doctorComboBox.setValue(booking.getSpecialistName());
        appointmentDatePicker.setValue(booking.getAppointmentDate());
        timeSlotComboBox.setValue(booking.getTimeSlot());
        statusComboBox.setValue(booking.getStatus());
        symptomsTextArea.setText(booking.getNotes());
        
        patientComboBox.setDisable(true);
    }

    private void setupComboBoxes() {
        // Setup time slots
        List<String> timeSlots = new ArrayList<>();
        LocalTime startTime = LocalTime.of(8, 0);
        LocalTime endTime = LocalTime.of(17, 0);
        
        while (startTime.isBefore(endTime)) {
            timeSlots.add(startTime.format(DateTimeFormatter.ofPattern("HH:mm")));
            startTime = startTime.plusMinutes(30);
        }
        
        timeSlotComboBox.setItems(FXCollections.observableArrayList(timeSlots));
        
        statusComboBox.setItems(FXCollections.observableArrayList("Pending", "Approved", "Cancelled"));
    }

    private void loadPatients() {
        ObservableList<String> patients = FXCollections.observableArrayList();
        
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT user_id, name FROM users WHERE role = 'Patient'";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String name = rs.getString("name");
                int id = rs.getInt("user_id");
                patients.add(name);
                patientIdMap.put(name, id);
            }
            
            patientComboBox.setItems(patients);
            
        } catch (SQLException e) {
            e.printStackTrace();
            showValidationMessage("Error loading patients: " + e.getMessage());
        }
    }

    private void loadDoctors() {
        ObservableList<String> doctors = FXCollections.observableArrayList();
        
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT user_id, name FROM users WHERE role = 'Doctor'";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String name = rs.getString("name");
                int id = rs.getInt("user_id");
                doctors.add(name);
                doctorIdMap.put(name, id);
            }
            
            doctorComboBox.setItems(doctors);
            
        } catch (SQLException e) {
            e.printStackTrace();
            showValidationMessage("Error loading doctors: " + e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        if (validateInput()) {
            if (isEditMode) {
                updateBooking();
            } else {
                createBooking();
            }
        }
    }

    private boolean validateInput() {
        if (patientComboBox.getValue() == null) {
            showValidationMessage("Please select a patient");
            return false;
        }
        
        if (doctorComboBox.getValue() == null) {
            showValidationMessage("Please select a doctor");
            return false;
        }
        
        if (appointmentDatePicker.getValue() == null) {
            showValidationMessage("Please select a date");
            return false;
        }
        
        if (timeSlotComboBox.getValue() == null) {
            showValidationMessage("Please select a time slot");
            return false;
        }
        
        if (statusComboBox.getValue() == null) {
            showValidationMessage("Please select a status");
            return false;
        }
        
        if (symptomsTextArea.getText().trim().isEmpty()) {
            showValidationMessage("Please enter symptoms or notes");
            return false;
        }
        
        if (appointmentDatePicker.getValue().isBefore(LocalDate.now())) {
            showValidationMessage("Appointment date cannot be in the past");
            return false;
        }
        
        validationMessageLabel.setText("");
        return true;
    }

    private void createBooking() {
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "INSERT INTO bookings (patient_id, doctor_id, appointment_date, appointment_time, symptoms, status) " +
                          "VALUES (?, ?, ?, ?, ?, ?)";
            
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, patientIdMap.get(patientComboBox.getValue()));
            pstmt.setInt(2, doctorIdMap.get(doctorComboBox.getValue()));
            pstmt.setDate(3, java.sql.Date.valueOf(appointmentDatePicker.getValue()));
            
            // Convert time string to SQL Time
            String timeStr = timeSlotComboBox.getValue() + ":00";
            pstmt.setTime(4, java.sql.Time.valueOf(timeStr));
            
            pstmt.setString(5, symptomsTextArea.getText().trim());
            pstmt.setString(6, statusComboBox.getValue());
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                if (onBookingSaved != null) {
                    onBookingSaved.run();
                }
                dialogStage.close();
            } else {
                showValidationMessage("Failed to create booking");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showValidationMessage("Error creating booking: " + e.getMessage());
        }
    }

    private void updateBooking() {
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "UPDATE bookings SET doctor_id = ?, appointment_date = ?, " +
                          "appointment_time = ?, symptoms = ?, status = ? " +
                          "WHERE booking_id = ?";
            
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, doctorIdMap.get(doctorComboBox.getValue()));
            pstmt.setDate(2, java.sql.Date.valueOf(appointmentDatePicker.getValue()));
            
            // Convert time string to SQL Time
            String timeStr = timeSlotComboBox.getValue() + ":00";
            pstmt.setTime(3, java.sql.Time.valueOf(timeStr));
            
            pstmt.setString(4, symptomsTextArea.getText().trim());
            pstmt.setString(5, statusComboBox.getValue());
            pstmt.setInt(6, booking.getAppointmentId());
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                if (onBookingSaved != null) {
                    onBookingSaved.run();
                }
                dialogStage.close();
            } else {
                showValidationMessage("Failed to update booking");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showValidationMessage("Error updating booking: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private void showValidationMessage(String message) {
        validationMessageLabel.setText(message);
    }
}