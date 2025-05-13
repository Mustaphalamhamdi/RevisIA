package com.mustapha.revisia;

import com.mustapha.revisia.util.HibernateUtil;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
         // Show loading screen
        VBox loadingBox = new VBox(10);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.getChildren().addAll(
                new Label("Chargement de RévisIA..."),
                new ProgressIndicator()
        );
        Scene loadingScene = new Scene(loadingBox, 300, 200);
        primaryStage.setScene(loadingScene);
        primaryStage.show();

        // Initialize app in background
        Task<Parent> loadTask = new Task<>() {
            @Override
            protected Parent call() throws Exception {
                // Initialize Hibernate
                HibernateUtil.getSessionFactory();

                // Load the main UI
                return FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml"));
            }
        };

        loadTask.setOnSucceeded(event -> {
            Scene scene = new Scene((Parent) loadTask.getValue());
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            primaryStage.setTitle("RévisIA - Assistant d'Étude");
            primaryStage.setScene(scene);
        });

        new Thread(loadTask).start();
    }

    public static void main(String[] args) {
        launch(args);
    }

}