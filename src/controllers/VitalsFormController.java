package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.SessionData;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.util.HashMap;
import java.util.Map;

public class VitalsFormController {

    @FXML private TextField pulseField;
    @FXML private TextField temperatureField;
    @FXML private TextField respirationField;
    @FXML private TextField bpField;
    @FXML private TextField weightField;
    @FXML private TextField heightField;
    @FXML private TextField oxygenField;
    

    private Map<String, String> vitalData = new HashMap<>();
    
    
    
    private String getAlertMessage(Map<String, String> vitals) {
        StringBuilder alert = new StringBuilder();

        try {
            double pulse = Double.parseDouble(vitals.get("Pulse"));
            if (pulse < 60) alert.append("Pulse is Low Alert\n");
            else if (pulse > 100) alert.append("Pulse is High Alert\n");

            double temp = Double.parseDouble(vitals.get("Temperature"));
            if (temp < 36) alert.append("Temperature is Low Alert\n");
            else if (temp > 37.5) alert.append("Temperature is High Alert\n");

            double resp = Double.parseDouble(vitals.get("Respiration"));
            if (resp < 12) alert.append("Respiration is Low Alert\n");
            else if (resp > 20) alert.append("Respiration is High Alert\n");

            double oxygen = Double.parseDouble(vitals.get("Oxygen"));
            if (oxygen < 95) alert.append("Oxygen Saturation is Low Alert\n");

        } catch (Exception e) {
            return "⚠️ Invalid or missing vital values.";
        }

        return alert.toString().isEmpty() ? null : alert.toString();
    }
    
    @FXML
    private void handleSend(javafx.event.ActionEvent event) {
    	
    	
        vitalData.put("Pulse", pulseField.getText());
        vitalData.put("Temperature", temperatureField.getText());
        vitalData.put("Respiration", respirationField.getText());
        vitalData.put("BP", bpField.getText());
        vitalData.put("Weight", weightField.getText());
        vitalData.put("Height", heightField.getText());
        vitalData.put("Oxygen", oxygenField.getText());
        
        SessionData.vitals.clear();
        SessionData.vitals.putAll(vitalData);
        
        String alertMsg = getAlertMessage(vitalData);

        if (alertMsg != null) {
            Alert alertWarning = new Alert(Alert.AlertType.WARNING);
            alertWarning.setTitle("Vitals Alert");
            alertWarning.setHeaderText("Abnormal vital signs detected!");
            alertWarning.setContentText(alertMsg);
            alertWarning.showAndWait();
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Vitals Sent");
        alert.setHeaderText("Vitals sent to your doctor.");
        alert.setContentText("You can now view a summary chart.");

        ButtonType viewChart = new ButtonType("View Chart");
        ButtonType close = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(viewChart, close);

        alert.showAndWait().ifPresent(type -> {
            if (type == viewChart) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/VitalsChart.fxml"));
                    Parent root = loader.load();

                    VitalsChartController controller = loader.getController();
                    controller.setVitalsData(vitalData);

                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Vitals Chart");
                    stage.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
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
}
