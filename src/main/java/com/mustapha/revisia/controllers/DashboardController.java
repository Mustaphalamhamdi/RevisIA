package com.mustapha.revisia.controllers;

import com.mustapha.revisia.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Random;

public class DashboardController implements Initializable {

    @FXML
    private Label usernameLabel;

    @FXML
    private Label quoteLabel;

    @FXML
    private Label quoteAuthorLabel;

    @FXML
    private Label hoursStudiedLabel;

    @FXML
    private Label documentsLabel;

    @FXML
    private Label streakLabel;

    @FXML
    private ProgressIndicator hoursProgress;

    @FXML
    private ProgressIndicator documentsProgress;

    @FXML
    private ProgressIndicator streakProgress;

    @FXML
    private PieChart subjectDistributionChart;

    @FXML
    private TableView<TimeSlotEntry> scheduleTable;

    @FXML
    private TableColumn<TimeSlotEntry, String> subjectColumn;

    @FXML
    private TableColumn<TimeSlotEntry, String> timeColumn;

    @FXML
    private TableColumn<TimeSlotEntry, String> locationColumn;

    @FXML
    private TableColumn<TimeSlotEntry, String> statusColumn;

    @FXML
    private ListView<String> recommendationsList;

    private User currentUser;
    private final Random random = new Random();
    private Timeline quoteChangeTimeline;

    // Motivational quotes
    private final String[][] quotes = {
            {"La connaissance s'acquiert par l'expérience, tout le reste n'est que de l'information.", "Albert Einstein"},
            {"L'éducation est l'arme la plus puissante pour changer le monde.", "Nelson Mandela"},
            {"Le succès, c'est tomber sept fois et se relever huit.", "Proverbe japonais"},
            {"L'éducation est ce qui reste après qu'on a oublié ce qu'on a appris à l'école.", "Albert Einstein"},
            {"La persévérance est la clé du succès.", "Charles Chaplin"},
            {"Le meilleur moyen de prédire l'avenir est de le créer.", "Abraham Lincoln"},
            {"Le voyage de mille lieues commence par un seul pas.", "Lao Tseu"},
            {"Votre temps est limité, ne le gâchez pas en menant une existence qui n'est pas la vôtre.", "Steve Jobs"},
            {"Votre éducation ne s'arrête jamais. C'est une série de leçons, avec les plus grandes qui viennent des études.", "Carol Burnett"}
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Setup table columns
        subjectColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Load sample data
        loadSampleData();

        // Initialize with a random quote
        changeQuote();

        // Setup a timeline to change quotes every 30 seconds
        quoteChangeTimeline = new Timeline(
                new KeyFrame(Duration.seconds(30), e -> changeQuote())
        );
        quoteChangeTimeline.setCycleCount(Timeline.INDEFINITE);
        quoteChangeTimeline.play();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        usernameLabel.setText(user.getUsername());
    }

    private void changeQuote() {
        int index = random.nextInt(quotes.length);
        quoteLabel.setText(quotes[index][0]);
        quoteAuthorLabel.setText("- " + quotes[index][1]);
    }

    private void loadSampleData() {
        // Sample schedule data
        ObservableList<TimeSlotEntry> scheduleData = FXCollections.observableArrayList(
                new TimeSlotEntry("Mathématiques", "08:30 - 10:00", "Amphi A", "Terminé"),
                new TimeSlotEntry("Java Programming", "10:15 - 11:45", "Lab B22", "En cours"),
                new TimeSlotEntry("Database Design", "14:00 - 15:30", "Room 305", "À venir")
        );
        scheduleTable.setItems(scheduleData);

        // Style the status column with colors
        statusColumn.setCellFactory(column -> new TableCell<TimeSlotEntry, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item);
                if (empty) {
                    setStyle("");
                } else {
                    switch (item) {
                        case "Terminé":
                            setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                            break;
                        case "En cours":
                            setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
                            break;
                        case "À venir":
                            setStyle("-fx-text-fill: #95a5a6;");
                            break;
                        default:
                            setStyle("");
                            break;
                    }
                }
            }
        });

        // Sample recommendations with priority indicators
        ObservableList<String> recommendations = FXCollections.observableArrayList(
                "🔴 Réviser Chapitre 5: Bases de données relationnelles (priorité élevée)",
                "🔵 Pratiquer les exercices Java sur les threads (30 minutes recommandées)",
                "🔵 Revoir les formules mathématiques du cours de lundi",
                "⚪ Préparer la présentation pour le cours de communication"
        );
        recommendationsList.setItems(recommendations);

        // Sample pie chart data for subject distribution
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Mathématiques", 30),
                new PieChart.Data("Java", 25),
                new PieChart.Data("Bases de données", 20),
                new PieChart.Data("Communication", 15),
                new PieChart.Data("Anglais", 10)
        );
        subjectDistributionChart.setData(pieChartData);

        // Apply animations to pie chart slices
        pieChartData.forEach(data -> {
            data.getNode().setOnMouseEntered(e -> {
                data.getNode().setStyle("-fx-pie-color: derive(" + data.getNode().getStyle() + ", 30%);");
            });
            data.getNode().setOnMouseExited(e -> {
                data.getNode().setStyle("");
            });
        });

        // Set sample progress values
        hoursStudiedLabel.setText("12");
        documentsLabel.setText("5");
        streakLabel.setText("3");

        hoursProgress.setProgress(0.6); // 12 out of 20 hours goal
        documentsProgress.setProgress(0.4); // 5 out of 12 documents
        streakProgress.setProgress(0.3); // 3 out of 10 days streak goal
    }

    @FXML
    private void handleLogout() {
        try {
            // Stop the timeline to prevent memory leaks
            if (quoteChangeTimeline != null) {
                quoteChangeTimeline.stop();
            }

            Parent loginRoot = FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml"));
            Scene loginScene = new Scene(loginRoot);
            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            loginScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(loginScene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de se déconnecter.");
        }
    }

    @FXML
    private void handleDashboard() {
        // Already on dashboard
    }

    @FXML
    private void handleTimetable() {
        showNotImplementedAlert("Emploi du temps");
    }

    @FXML
    private void handleDocuments() {
        showNotImplementedAlert("Gestion des documents");
    }

    @FXML
    private void handleStudy() {
        showNotImplementedAlert("Mode étude");
    }

    @FXML
    private void handleExams() {
        showNotImplementedAlert("Préparation aux examens");
    }

    @FXML
    private void handleSettings() {
        showNotImplementedAlert("Paramètres");
    }

    @FXML
    private void handleUploadPDF() {
        showNotImplementedAlert("Upload de PDF");
    }

    @FXML
    private void handleStartStudying() {
        showNotImplementedAlert("Commencer à étudier");
    }

    @FXML
    private void handlePrepareExam() {
        showNotImplementedAlert("Préparer un examen");
    }

    @FXML
    private void handleGenerateExercises() {
        showNotImplementedAlert("Générer des exercices");
    }

    @FXML
    private void handleViewStatistics() {
        showNotImplementedAlert("Voir les statistiques");
    }

    private void showNotImplementedAlert(String feature) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Fonctionnalité à venir");
        alert.setHeaderText(null);
        alert.setContentText("La fonctionnalité '" + feature + "' sera disponible prochainement!");
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Helper class for schedule table entries
    public static class TimeSlotEntry {
        private final String subject;
        private final String time;
        private final String location;
        private final String status;

        public TimeSlotEntry(String subject, String time, String location, String status) {
            this.subject = subject;
            this.time = time;
            this.location = location;
            this.status = status;
        }

        public String getSubject() {
            return subject;
        }

        public String getTime() {
            return time;
        }

        public String getLocation() {
            return location;
        }

        public String getStatus() {
            return status;
        }
    }
}