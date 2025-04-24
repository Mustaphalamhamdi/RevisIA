package com.mustapha.revisia.controllers;

import com.mustapha.revisia.services.UserService;
import com.mustapha.revisia.services.UserServiceImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    private final UserService userService = new UserServiceImpl();

    @FXML
    private void handleRegister(ActionEvent event) {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Simple validation
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur d'inscription", "Veuillez remplir tous les champs.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Erreur d'inscription", "Les mots de passe ne correspondent pas.");
            return;
        }

        if (userService.getUserByUsername(username) != null) {
            showAlert(Alert.AlertType.ERROR, "Erreur d'inscription", "Ce nom d'utilisateur est déjà pris.");
            return;
        }

        // Register the user
        userService.registerUser(username, password, email);

        showAlert(Alert.AlertType.INFORMATION, "Inscription réussie", "Votre compte a été créé avec succès.");

        // Navigate back to login
        navigateToLogin(event);
    }

    @FXML
    private void handleBackToLogin(ActionEvent event) {
        navigateToLogin(event);
    }

    private void navigateToLogin(ActionEvent event) {
        try {
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml"));
            Scene loginScene = new Scene(loginRoot);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            loginScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(loginScene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Impossible de charger l'écran de connexion.");
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