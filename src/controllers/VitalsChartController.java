package controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.fxml.FXMLLoader;

import java.util.Map;

/**
 *
 * @author USER
 */
public class VitalsChartController {

    @FXML private BarChart<String, Number> barChart;

    /**
     *
     * @param vitals
     */
    public void setVitalsData(Map<String, String> vitals) {
        XYChart.Series<String, Number> normal = new XYChart.Series<>();
        normal.setName("Normal");

        XYChart.Series<String, Number> yours = new XYChart.Series<>();
        yours.setName("You");

        // Define normal ranges (approximate values)
        Map<String, Double> normalValues = Map.of(
                "Pulse", 75.0,
                "Temperature", 37.0,
                "Respiration", 16.0,
                "Oxygen", 98.0
        );

        try {
            double pulse = Double.parseDouble(vitals.get("Pulse"));
            double temp = Double.parseDouble(vitals.get("Temperature"));
            double resp = Double.parseDouble(vitals.get("Respiration"));
            double oxy = Double.parseDouble(vitals.get("Oxygen"));

            normal.getData().add(new XYChart.Data<>("Pulse", normalValues.get("Pulse")));
            normal.getData().add(new XYChart.Data<>("Temp", normalValues.get("Temperature")));
            normal.getData().add(new XYChart.Data<>("Resp", normalValues.get("Respiration")));
            normal.getData().add(new XYChart.Data<>("Oxygen", normalValues.get("Oxygen")));

            yours.getData().add(new XYChart.Data<>("Pulse", pulse));
            yours.getData().add(new XYChart.Data<>("Temp", temp));
            yours.getData().add(new XYChart.Data<>("Resp", resp));
            yours.getData().add(new XYChart.Data<>("Oxygen", oxy));

            barChart.getData().addAll(normal, yours);
         // ✅ Set bar colors: Green for Normal, Orange for Yours
            barChart.lookupAll(".default-color0.chart-bar").forEach(n -> n.setStyle("-fx-bar-fill: #4CAF50;")); // Normal - Green
            barChart.lookupAll(".default-color1.chart-bar").forEach(n -> n.setStyle("-fx-bar-fill: #FF9800;")); // Yours - Orange

        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
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