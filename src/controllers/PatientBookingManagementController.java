package controllers;

import database.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Appointment;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Controller for managing patient bookings
 * Allows viewing, adding, editing, and canceling patient appointments
 */
public class PatientBookingManagementController {

    @FXML private TableView<Appointment> bookingsTable;
    @FXML private TableColumn<Appointment, Integer> idColumn;
    @FXML private TableColumn<Appointment, String> patientColumn;
    @FXML private TableColumn<Appointment, String> doctorColumn;
    @FXML private TableColumn<Appointment, String> dateColumn;
    @FXML private TableColumn<Appointment, String> timeColumn;
    @FXML private TableColumn<Appointment, String> symptomsColumn;
    @FXML private TableColumn<Appointment, String> statusColumn;
    
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterBox;
    @FXML private DatePicker dateFilterPicker;
    @FXML private Label statusLabel;

    private ObservableList<Appointment> bookingsList = FXCollections.observableArrayList();
    private FilteredList<Appointment> filteredBookings;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();
        loadBookings();
        
        // Set up filtered list
        filteredBookings = new FilteredList<>(bookingsList, p -> true);
        bookingsTable.setItems(filteredBookings);
        
        // Add listener for search field
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateFilters();
        });
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        patientColumn.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        doctorColumn.setCellValueFactory(new PropertyValueFactory<>("specialistName"));
        dateColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getAppointmentDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("timeSlot"));
        symptomsColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void setupFilters() {
        // Setup status filter options
        statusFilterBox.getItems().addAll("All", "Pending", "Approved", "Cancelled");
        statusFilterBox.setValue("All");
        
        // Add listener for status filter
        statusFilterBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateFilters();
        });
        
        // Add listener for date filter
        dateFilterPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateFilters();
        });
    }

    private void updateFilters() {
        filteredBookings.setPredicate(booking -> {
            // If filter text is empty, display all bookings
            String searchText = searchField.getText().toLowerCase();
            String statusFilter = statusFilterBox.getValue();
            LocalDate dateFilter = dateFilterPicker.getValue();
            
            // Filter by search text
            boolean matchesSearch = searchText == null || searchText.isEmpty() || 
                                   booking.getPatientName().toLowerCase().contains(searchText) ||
                                   booking.getSpecialistName().toLowerCase().contains(searchText);
            
            // Filter by status
            boolean matchesStatus = "All".equals(statusFilter) || 
                                   booking.getStatus().equalsIgnoreCase(statusFilter);
            
            // Filter by date
            boolean matchesDate = dateFilter == null || 
                                 booking.getAppointmentDate().equals(dateFilter);
            
            return matchesSearch && matchesStatus && matchesDate;
        });
        
        statusLabel.setText("Showing " + filteredBookings.size() + " of " + bookingsList.size() + " bookings");
    }

    private void loadBookings() {
        bookingsList.clear();
        
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT b.booking_id, p.name AS patient_name, d.name AS doctor_name, " +
                          "b.appointment_date, b.appointment_time, b.symptoms, b.status " +
                          "FROM bookings b " +
                          "JOIN users p ON b.patient_id = p.user_id " +
                          "JOIN users d ON b.doctor_id = d.user_id " +
                          "ORDER BY b.appointment_date DESC, b.appointment_time ASC";
            
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Appointment booking = new Appointment();
                booking.setAppointmentId(rs.getInt("booking_id"));
                booking.setPatientName(rs.getString("patient_name"));
                booking.setSpecialistName(rs.getString("doctor_name"));
                booking.setAppointmentDate(rs.getDate("appointment_date").toLocalDate());
                
                // Convert Time to String timeSlot
                Time sqlTime = rs.getTime("appointment_time");
                booking.setTimeSlot(sqlTime.toString());
                
                booking.setNotes(rs.getString("symptoms"));
                booking.setStatus(rs.getString("status"));
                
                bookingsList.add(booking);
            }
            
            statusLabel.setText("Loaded " + bookingsList.size() + " bookings");
            
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Error loading bookings: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddBooking() {
        showBookingDialog(null);
    }

    @FXML
    private void handleEditBooking() {
        Appointment selectedBooking = bookingsTable.getSelectionModel().getSelectedItem();
        if (selectedBooking == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a booking to edit.");
            return;
        }
        
        showBookingDialog(selectedBooking);
    }

    @FXML
    private void handleCancelBooking() {
        Appointment selectedBooking = bookingsTable.getSelectionModel().getSelectedItem();
        if (selectedBooking == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a booking to cancel.");
            return;
        }
        
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Cancel Booking");
        confirmDialog.setHeaderText("Cancel Selected Booking");
        confirmDialog.setContentText("Are you sure you want to cancel the booking for " + 
                                    selectedBooking.getPatientName() + " on " + 
                                    selectedBooking.getAppointmentDate() + "?");
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DatabaseHelper.getConnection()) {
                String query = "UPDATE bookings SET status = 'Cancelled' WHERE booking_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, selectedBooking.getAppointmentId());
                
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Booking cancelled successfully.");
                    loadBookings(); // Refresh the table
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to cancel booking.");
                }
                
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Error cancelling booking: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleViewDetails() {
        Appointment selectedBooking = bookingsTable.getSelectionModel().getSelectedItem();
        if (selectedBooking == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a booking to view details.");
            return;
        }
        
        StringBuilder details = new StringBuilder();
        details.append("Booking ID: ").append(selectedBooking.getAppointmentId()).append("\n\n");
        details.append("Patient: ").append(selectedBooking.getPatientName()).append("\n");
        details.append("Doctor: ").append(selectedBooking.getSpecialistName()).append("\n");
        details.append("Date: ").append(selectedBooking.getAppointmentDate()).append("\n");
        details.append("Time: ").append(selectedBooking.getTimeSlot()).append("\n");
        details.append("Status: ").append(selectedBooking.getStatus()).append("\n\n");
        details.append("Symptoms/Notes: ").append(selectedBooking.getNotes()).append("\n");
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Booking Details");
        alert.setHeaderText("Booking Information");
        
        TextArea textArea = new TextArea(details.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefWidth(400);
        textArea.setPrefHeight(300);
        
        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    @FXML
    private void handleApplyFilters() {
        updateFilters();
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        statusFilterBox.setValue("All");
        dateFilterPicker.setValue(null);
        updateFilters();
    }

    @FXML
    private void handleRefresh() {
        loadBookings();
        updateFilters();
    }

    @FXML
    private void handleBackToDashboard(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Dashboard.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("TeleHealth System Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Error returning to dashboard: " + e.getMessage());
        }
    }

    private void showBookingDialog(Appointment booking) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/BookingDialog.fxml"));
            Parent root = loader.load();
            
            BookingDialogController controller = loader.getController();
            if (booking != null) {
                controller.setBooking(booking);
            }
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle(booking == null ? "Add New Booking" : "Edit Booking");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(bookingsTable.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            
            // Set callback for when dialog is closed
            controller.setDialogStage(dialogStage);
            controller.setOnBookingSaved(() -> {
                loadBookings();
                updateFilters();
            });
            
            dialogStage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Dialog Error", "Error opening booking dialog: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}