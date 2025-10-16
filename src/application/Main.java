package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import database.DatabaseHelper;

public class Main extends Application {
    public void start(Stage primaryStage) {
        try {
            //DatabaseHelper.initializeDatabase(); // Create DB on launch

            Parent root = FXMLLoader.load(getClass().getResource("/views/Login.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

            primaryStage.setTitle("TeleHealth System - Login");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);  // Allow resizing (this enables maximize button)
            primaryStage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}