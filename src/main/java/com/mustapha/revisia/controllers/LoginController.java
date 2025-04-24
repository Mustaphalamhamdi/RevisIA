package com.mustapha.revisia.controllers;

import com.mustapha.revisia.models.User;
import com.mustapha.revisia.services.UserService;
import com.mustapha.revisia.services.UserServiceImpl;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    private final UserService userService = new UserServiceImpl();

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur de connexion", "Veuillez remplir tous les champs.");
            return;
        }

        boolean authenticated = userService.authenticateUser(username, password);

        if (authenticated) {
            try {
                // Load the Dashboard FXML
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DashboardView.fxml"));
                Parent dashboardRoot = loader.load();

                // Get the controller and set the current user
                DashboardController dashboardController = loader.getController();
                User user = userService.getUserByUsername(username);
                dashboardController.setCurrentUser(user);

                // Switch to the dashboard scene
                Scene dashboardScene = new Scene(dashboardRoot);
                dashboardScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(dashboardScene);
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Impossible de charger le tableau de bord.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur de connexion", "Nom d'utilisateur ou mot de passe incorrect.");
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        try {
            Parent registerRoot = FXMLLoader.load(getClass().getResource("/fxml/RegisterView.fxml"));
            Scene registerScene = new Scene(registerRoot);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            registerScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(registerScene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Impossible de charger l'écran d'inscription.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}