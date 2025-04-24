package com.mustapha.revisia;

import com.mustapha.revisia.util.HibernateUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize Hibernate - this will create tables
        try {
            System.out.println("Initializing Hibernate...");
            HibernateUtil.getSessionFactory();
            System.out.println("Hibernate initialized successfully!");
        } catch (Exception e) {
            System.err.println("Error initializing Hibernate:");
            e.printStackTrace();
        }

        // Continue with normal startup
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            primaryStage.setTitle("RévisIA - Assistant d'Étude");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace(); 
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}