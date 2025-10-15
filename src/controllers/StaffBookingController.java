package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.fxml.FXMLLoader;

import models.Appointment;
import database.DatabaseHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import javafx.scene.layout.GridPane;

/**
 * Controller for healthcare staff to manage patient bookings
 * Allows viewing, modifying, and cancelling appointments
 */
public class StaffBookingController {

    @FXML private TableView<Appointment> appointmentsTable;
    @FXML private TableColumn<Appointment, String> patientColumn;
    @FXML private TableColumn<Appointment, String> specialistColumn;
    @FXML private TableColumn<Appointment, String> dateColumn;
    @FXML private TableColumn<Appointment, String> timeColumn;
    @FXML private TableColumn<Appointment, String> statusColumn;
    
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterBox;
    @FXML private DatePicker dateFilterPicker;
    @FXML private Label statusLabel;

    private ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();
    private ObservableList<Appointment> filteredList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();
        loadAppointments();
    }

    private void setupTableColumns() {
        patientColumn.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        specialistColumn.setCellValueFactory(new PropertyValueFactory<>("specialistName"));
        dateColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFormattedDate()));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("timeSlot"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Add row selection listener
        appointmentsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                // Enable/disable buttons based on selection
            });

        appointmentsTable.setItems(filteredList);
    }

    private void setupFilters() {
        statusFilterBox.getItems().addAll("ALL", "SCHEDULED", "COMPLETED", "CANCELLED", "RESCHEDULED");
        statusFilterBox.setValue("ALL");

        // Add listeners for real-time filtering
        searchField.textProperty().addListener((obs, oldText, newText) -> applyFilters());
        statusFilterBox.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        dateFilterPicker.valueProperty().addListener((obs, oldDate, newDate) -> applyFilters());
    }

    private void loadAppointments() {
        appointmentList.clear();
        
        try (Connection conn = DatabaseHelper.connect()) {
            String query = "SELECT * FROM appointments ORDER BY appointment_date, time_slot";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Appointment appointment = new Appointment(
                    rs.getInt("id"),
                    rs.getString("patient_name"),
                    rs.getString("specialist_name"),
                    LocalDate.parse(rs.getString("appointment_date")),
                    rs.getString("time_slot"),
                    rs.getString("status"),
                    rs.getString("consultation_type"),
                    rs.getString("notes")
                );
                appointmentList.add(appointment);
            }
            
            applyFilters();
            statusLabel.setText("Loaded " + appointmentList.size() + " appointments");
            statusLabel.setStyle("-fx-text-fill: green;");
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load appointments: " + e.getMessage());
        }
    }

    private void applyFilters() {
        filteredList.clear();
        
        String searchText = searchField.getText().toLowerCase().trim();
        String statusFilter = statusFilterBox.getValue();
        LocalDate dateFilter = dateFilterPicker.getValue();

        for (Appointment appointment : appointmentList) {
            boolean matchesSearch = searchText.isEmpty() ||
                appointment.getPatientName().toLowerCase().contains(searchText) ||
                appointment.getSpecialistName().toLowerCase().contains(searchText);
            
            boolean matchesStatus = "ALL".equals(statusFilter) ||
                appointment.getStatus().equals(statusFilter);
            
            boolean matchesDate = dateFilter == null ||
                appointment.getAppointmentDate().equals(dateFilter);

            if (matchesSearch && matchesStatus && matchesDate) {
                filteredList.add(appointment);
            }
        }
    }

    @FXML
    private void handleModifyBooking() {
        Appointment selected = appointmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select an appointment to modify.");
            return;
        }

        if (!selected.canBeRescheduled()) {
            showAlert(Alert.AlertType.WARNING, "Cannot Modify", "This appointment cannot be modified.");
            return;
        }

        // Create dialog for modification
        Dialog<Appointment> dialog = createModifyDialog(selected);
        Optional<Appointment> result = dialog.showAndWait();

        result.ifPresent(this::updateAppointment);
    }

    @FXML
    private void handleCancelBooking() {
        Appointment selected = appointmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select an appointment to cancel.");
            return;
        }

        if (!selected.canBeCancelled()) {
            showAlert(Alert.AlertType.WARNING, "Cannot Cancel", "This appointment cannot be cancelled.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Cancel Appointment");
        confirmation.setHeaderText("Confirm Cancellation");
        confirmation.setContentText("Are you sure you want to cancel this appointment for " + selected.getPatientName() + "?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            cancelAppointment(selected);
        }
    }

    @FXML
    private void handleRefresh() {
        loadAppointments();
    }

    @FXML
    private void handleViewDetails() {
        Appointment selected = appointmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select an appointment to view details.");
            return;
        }

        showAppointmentDetails(selected);
    }

    private Dialog<Appointment> createModifyDialog(Appointment appointment) {
        Dialog<Appointment> dialog = new Dialog<>();
        dialog.setTitle("Modify Appointment");
        dialog.setHeaderText("Modify appointment for " + appointment.getPatientName());

        // Create form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        DatePicker datePicker = new DatePicker(appointment.getAppointmentDate());
        ComboBox<String> timeBox = new ComboBox<>();
        timeBox.getItems().addAll("10:00 AM", "11:00 AM", "12:00 PM", "2:00 PM", "4:00 PM");
        timeBox.setValue(appointment.getTimeSlot());
        
        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("SCHEDULED", "RESCHEDULED", "COMPLETED", "CANCELLED");
        statusBox.setValue(appointment.getStatus());

        TextArea notesArea = new TextArea(appointment.getNotes());
        notesArea.setPrefRowCount(3);

        grid.add(new Label("Date:"), 0, 0);
        grid.add(datePicker, 1, 0);
        grid.add(new Label("Time:"), 0, 1);
        grid.add(timeBox, 1, 1);
        grid.add(new Label("Status:"), 0, 2);
        grid.add(statusBox, 1, 2);
        grid.add(new Label("Notes:"), 0, 3);
        grid.add(notesArea, 1, 3);

        dialog.getDialogPane().setContent(grid);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                appointment.setAppointmentDate(datePicker.getValue());
                appointment.setTimeSlot(timeBox.getValue());
                appointment.setStatus(statusBox.getValue());
                appointment.setNotes(notesArea.getText());
                return appointment;
            }
            return null;
        });

        return dialog;
    }

    private void updateAppointment(Appointment appointment) {
        try (Connection conn = DatabaseHelper.connect()) {
            String query = """
                UPDATE appointments SET 
                appointment_date = ?, time_slot = ?, status = ?, notes = ?
                WHERE id = ?
            """;
            
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, appointment.getAppointmentDate().toString());
            pstmt.setString(2, appointment.getTimeSlot());
            pstmt.setString(3, appointment.getStatus());
            pstmt.setString(4, appointment.getNotes());
            pstmt.setInt(5, appointment.getAppointmentId());

            int updated = pstmt.executeUpdate();
            if (updated > 0) {
                loadAppointments(); // Refresh the table
                statusLabel.setText("Appointment updated successfully");
                statusLabel.setStyle("-fx-text-fill: green;");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update appointment: " + e.getMessage());
        }
    }

    private void cancelAppointment(Appointment appointment) {
        appointment.markAsCancelled();
        updateAppointment(appointment);
    }

    private void showAppointmentDetails(Appointment appointment) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Appointment Details");
        alert.setHeaderText("Full Appointment Information");

        TextArea textArea = new TextArea(appointment.toString());
        textArea.setWrapText(true);
        textArea.setEditable(false);
        textArea.setPrefHeight(300);

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    @FXML
    private void goBackToDashboard(javafx.event.ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Dashboard.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TeleHealth - Dashboard");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}