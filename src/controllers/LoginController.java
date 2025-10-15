package controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import database.DatabaseHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle("Login Status");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please fill all fields.");
            return;
        }

        try (Connection conn = DatabaseHelper.connect()) {
            String query = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // Login success - go to dashboard
            	FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Dashboard.fxml"));
            	Parent root = loader.load(); // load the FXML

            	DashboardController controller = loader.getController(); // get controller instance
            	controller.setUsername(rs.getString("name")); // pass name to dashboard

            	Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            	stage.setScene(new Scene(root));
            	stage.setTitle("TeleHealth - Dashboard");
            	stage.show();
            } else {
                errorLabel.setText("Invalid credentials.");
            }
        } catch (Exception e) {
            e.printStackTrace();  // ✅ Add this for debugging
            showAlert(Alert.AlertType.ERROR, "Login failed:\n" + e.getMessage());
        }
    }

    @FXML
    private void goToSignup(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Signup.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TeleHealth - Signup");
        } catch (Exception e) {
            e.printStackTrace();  // ✅ Add this for debugging
            showAlert(Alert.AlertType.ERROR, "Login failed:\n" + e.getMessage());
        }
    }
}