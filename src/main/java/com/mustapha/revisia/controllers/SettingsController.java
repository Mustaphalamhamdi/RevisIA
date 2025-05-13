package com.mustapha.revisia.controllers;

import com.mustapha.revisia.config.APIConfig;
import com.mustapha.revisia.models.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    @FXML private Label usernameLabel;
    @FXML private CheckBox openAIEnabledCheckbox;
    @FXML private PasswordField apiKeyField;
    @FXML private Label openAIStatusLabel;

    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Load current settings
        loadSettings();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        usernameLabel.setText(user.getUsername());
    }

    private void loadSettings() {
        try {
            // Load OpenAI settings
            boolean openAIEnabled = APIConfig.isOpenAIEnabled();
            String apiKey = APIConfig.getOpenAIApiKey();

            openAIEnabledCheckbox.setSelected(openAIEnabled);

            if (apiKey != null && !apiKey.isEmpty() && !apiKey.equals("your_openai_api_key_here")) {
                apiKeyField.setText(apiKey);
                // Mask the key
                apiKeyField.setPromptText("*".repeat(8));

                if (openAIEnabled) {
                    openAIStatusLabel.setText("Activé");
                    openAIStatusLabel.setStyle("-fx-text-fill: #27ae60;"); // Green
                } else {
                    openAIStatusLabel.setText("Désactivé");
                    openAIStatusLabel.setStyle("-fx-text-fill: #e74c3c;"); // Red
                }
            } else {
                openAIStatusLabel.setText("Non configuré");
                openAIStatusLabel.setStyle("-fx-text-fill: #e74c3c;"); // Red
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les paramètres: " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenAIToggle() {
        boolean enabled = openAIEnabledCheckbox.isSelected();

        if (enabled) {
            // Check if API key is configured
            String apiKey = apiKeyField.getText();
            if (apiKey == null || apiKey.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Configuration requise",
                        "Veuillez configurer votre clé API OpenAI avant d'activer cette fonctionnalité.");
                openAIEnabledCheckbox.setSelected(false);
                return;
            }
        }

        // Update status label
        if (enabled) {
            openAIStatusLabel.setText("Activé");
            openAIStatusLabel.setStyle("-fx-text-fill: #27ae60;"); // Green
        } else {
            openAIStatusLabel.setText("Désactivé");
            openAIStatusLabel.setStyle("-fx-text-fill: #e74c3c;"); // Red
        }
    }

    @FXML
    private void handleSaveAPIKey() {
        String apiKey = apiKeyField.getText();
        if (apiKey == null || apiKey.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez saisir une clé API valide.");
            return;
        }

        try {
            saveSettings();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Clé API sauvegardée avec succès.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de sauvegarder la clé API: " + e.getMessage());
        }
    }

    @FXML
    private void handleSaveSettings() {
        try {
            saveSettings();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Paramètres sauvegardés avec succès.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de sauvegarder les paramètres: " + e.getMessage());
        }
    }

    private void saveSettings() throws IOException {
        Properties properties = new Properties();

        // Load existing properties first
        java.io.InputStream inputStream = getClass().getClassLoader().getResourceAsStream("api.properties");
        if (inputStream != null) {
            properties.load(inputStream);
            inputStream.close();
        }

        // Set updated properties
        properties.setProperty("openai.enabled", String.valueOf(openAIEnabledCheckbox.isSelected()));

        String apiKey = apiKeyField.getText();
        if (apiKey != null && !apiKey.isEmpty()) {
            properties.setProperty("openai.api.key", apiKey);
        }

        // Save properties to file
        String resourcePath = getClass().getClassLoader().getResource("api.properties").getPath();
        OutputStream outputStream = new FileOutputStream(resourcePath);
        properties.store(outputStream, "Updated OpenAI settings");
        outputStream.close();
    }

    // Navigation methods

    @FXML
    private void handleDashboard() {
        navigateTo("/fxml/DashboardView.fxml");
    }

    @FXML
    private void handleTimetable() {
        navigateTo("/fxml/TimetableView.fxml");
    }

    @FXML
    private void handleDocuments() {
        navigateTo("/fxml/DocumentsView.fxml");
    }

    @FXML
    private void handleStudy() {
        navigateTo("/fxml/StudyView.fxml");
    }

    @FXML
    private void handleExams() {
        navigateTo("/fxml/ExamView.fxml");
    }

    @FXML
    private void handleSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SettingsView.fxml"));
            Parent settingsRoot = loader.load();

            SettingsController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Scene settingsScene = new Scene(settingsRoot);
            settingsScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            stage.setScene(settingsScene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Impossible de charger les paramètres.");
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml"));
            Scene loginScene = new Scene(loginRoot);
            loginScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            stage.setScene(loginScene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de se déconnecter: " + e.getMessage());
        }
    }

    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof DashboardController) {
                ((DashboardController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof TimetableController) {
                ((TimetableController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof DocumentsController) {
                ((DocumentsController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof StudyController) {
                ((StudyController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof ExamController) {
                ((ExamController) controller).setCurrentUser(currentUser);
            }

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation",
                    "Impossible de naviguer vers l'écran demandé: " + e.getMessage());
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