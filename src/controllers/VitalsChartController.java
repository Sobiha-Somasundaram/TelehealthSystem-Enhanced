package controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.fxml.FXMLLoader;

import java.util.Map;

public class VitalsChartController {

    @FXML private BarChart<String, Number> barChart;

    private int userId;
    private String userName;
    private String userRole;

    public void setVitalsData(Map<String, String> vitals) {
        barChart.getData().clear();

        XYChart.Series<String, Number> normal = new XYChart.Series<>();
        normal.setName("Normal");

        XYChart.Series<String, Number> yours = new XYChart.Series<>();
        yours.setName("You");

        Map<String, Double> normalValues = Map.of(
                "Pulse", 75.0,
                "Temperature", 37.0,
                "Respiration", 16.0,
                "Oxygen", 98.0
        );

        try {
            double pulse = Double.parseDouble(vitals.getOrDefault("Pulse", "0"));
            double temp = Double.parseDouble(vitals.getOrDefault("Temperature", "0"));
            double resp = Double.parseDouble(vitals.getOrDefault("Respiration", "0"));
            double oxy = Double.parseDouble(vitals.getOrDefault("Oxygen", "0"));

            normal.getData().add(new XYChart.Data<>("Pulse", normalValues.get("Pulse")));
            normal.getData().add(new XYChart.Data<>("Temp", normalValues.get("Temperature")));
            normal.getData().add(new XYChart.Data<>("Resp", normalValues.get("Respiration")));
            normal.getData().add(new XYChart.Data<>("Oxygen", normalValues.get("Oxygen")));

            yours.getData().add(new XYChart.Data<>("Pulse", pulse));
            yours.getData().add(new XYChart.Data<>("Temp", temp));
            yours.getData().add(new XYChart.Data<>("Resp", resp));
            yours.getData().add(new XYChart.Data<>("Oxygen", oxy));

            barChart.getData().addAll(normal, yours);

            barChart.lookupAll(".default-color0.chart-bar")
                    .forEach(n -> n.setStyle("-fx-bar-fill: #4CAF50;"));
            barChart.lookupAll(".default-color1.chart-bar")
                    .forEach(n -> n.setStyle("-fx-bar-fill: #FF9800;"));

        } catch (NumberFormatException e) { e.printStackTrace(); }
    }

    public void setUserInfo(int id, String name, String role) {
        this.userId = id;
        this.userName = name;
        this.userRole = role;
    }

    @FXML
    private void goBackToDashboard(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Dashboard.fxml"));
            Parent root = loader.load();

            DashboardController controller = loader.getController();
            controller.setUserInfo(userId, userName, userRole);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TeleHealth - Dashboard");
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }
}
