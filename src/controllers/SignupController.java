package controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import database.DatabaseHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class SignupController {

    @FXML private TextField nameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label statusLabel;
    @FXML private ComboBox<String> comboRole;

@FXML
    public void initialize() {
        comboRole.getItems().addAll("Patient", "Doctor", "Admin");
}

    
 @FXML   
    private void handleSignup(ActionEvent event) {
        String name = nameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();
        String role = comboRole.getValue();

        if (name.isEmpty() || username.isEmpty() || password.isEmpty() || confirm.isEmpty()|| role == null) {
            statusLabel.setText("Please fill all fields and select a role.");
            return;
        }
        

        if (!password.equals(confirm)) {
            statusLabel.setText("Passwords do not match.");
            return;
        }

        try (Connection conn = DatabaseHelper.getConnection()) {
            
            String query = "INSERT INTO users (name, username, password, role) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, name);
            pstmt.setString(2, username);
            pstmt.setString(3, password);
            pstmt.setString(4, role);
            pstmt.executeUpdate();

            statusLabel.setText("Signup successful! Account created for " + role);
       
            statusLabel.setStyle("-fx-text-fill: green;");
        } catch (Exception e) {
            statusLabel.setText("Failed to create account: " + e.getMessage());
        }
    }

    @FXML
    private void goToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TeleHealth - Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}