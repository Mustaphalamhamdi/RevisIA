package com.mustapha.revisia.controllers;

import com.mustapha.revisia.models.Document;
import com.mustapha.revisia.models.StudyRecommendation;
import com.mustapha.revisia.models.StudySession;
import com.mustapha.revisia.models.Subject;
import com.mustapha.revisia.models.User;
import com.mustapha.revisia.services.AIStudyRecommendationService;
import com.mustapha.revisia.services.DocumentService;
import com.mustapha.revisia.services.DocumentServiceImpl;
import com.mustapha.revisia.services.QuizService;
import com.mustapha.revisia.services.QuizServiceImpl;
import com.mustapha.revisia.services.StudySessionService;
import com.mustapha.revisia.services.StudySessionServiceImpl;
import com.mustapha.revisia.services.SubjectService;
import com.mustapha.revisia.services.SubjectServiceImpl;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

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
    @FXML
    private Label aiPoweredLabel;

    private User currentUser;
    private final Random random = new Random();
    private Timeline quoteChangeTimeline;

    private final StudySessionService studySessionService = new StudySessionServiceImpl();
    private final SubjectService subjectService = new SubjectServiceImpl();
    private final DocumentService documentService = new DocumentServiceImpl();
    private final QuizService quizService = new QuizServiceImpl();
    private AIStudyRecommendationService aiRecommendationService;
    private List<StudyRecommendation> recommendations;

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
        subjectColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("subject"));
        timeColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("time"));
        locationColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("location"));
        statusColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("status"));

        // Setup AI badge tooltip
        Tooltip aiTooltip = new Tooltip("Recommandations générées par intelligence artificielle");
        Tooltip.install(aiPoweredLabel, aiTooltip);

        // Initialize the AI recommendation service
        aiRecommendationService = new AIStudyRecommendationService(studySessionService, quizService);

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

        // Load study statistics
        loadStudyStatistics();

        // Generate and display AI recommendations
        loadRecommendations();

        // Load sample schedule data
        loadSampleData();
    }

    private void changeQuote() {
        int index = random.nextInt(quotes.length);
        quoteLabel.setText(quotes[index][0]);
        quoteAuthorLabel.setText("- " + quotes[index][1]);
    }

    private void loadStudyStatistics() {
        try {
            // Get all study sessions for the user
            List<StudySession> sessions = studySessionService.getStudySessionsByUser(currentUser);

            // Calculate total study time in hours
            int totalMinutes = sessions.stream().mapToInt(StudySession::getDurationMinutes).sum();
            int totalHours = totalMinutes / 60;
            hoursStudiedLabel.setText(String.valueOf(totalHours));

            // Set a goal of 20 hours per month
            double hoursGoal = 20.0;
            hoursProgress.setProgress(Math.min(1.0, totalHours / hoursGoal));

            // Calculate number of documents studied
            long documentCount = sessions.stream()
                    .filter(s -> s.getDocument() != null)
                    .map(s -> s.getDocument().getId())
                    .distinct()
                    .count();
            documentsLabel.setText(String.valueOf(documentCount));

            // Set a goal of 10 documents
            double documentsGoal = 10.0;
            documentsProgress.setProgress(Math.min(1.0, documentCount / documentsGoal));

            // Calculate streak (consecutive days with study sessions)
            int streak = calculateStreak(sessions);
            streakLabel.setText(String.valueOf(streak));

            // Set a goal of 7 days streak
            double streakGoal = 7.0;
            streakProgress.setProgress(Math.min(1.0, streak / streakGoal));

            // Create pie chart data for subject distribution
            updateSubjectDistributionChart(sessions);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading study statistics: " + e.getMessage());
        }
    }

    private int calculateStreak(List<StudySession> sessions) {
        // This is a simplified implementation. In a real app, you would:
        // 1. Group sessions by date
        // 2. Check for consecutive days
        // 3. Calculate the current streak

        // For now, return a random value between 1 and 5
        return 1 + random.nextInt(5);
    }

    private void updateSubjectDistributionChart(List<StudySession> sessions) {
        // Group session minutes by subject
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        // Get all subjects
        List<Subject> subjects = subjectService.getSubjectsByUser(currentUser);

        // For each subject, calculate study time
        for (Subject subject : subjects) {
            int subjectMinutes = sessions.stream()
                    .filter(s -> s.getSubject().getId().equals(subject.getId()))
                    .mapToInt(StudySession::getDurationMinutes)
                    .sum();

            // Only add to chart if there is study time
            if (subjectMinutes > 0) {
                pieChartData.add(new PieChart.Data(subject.getName(), subjectMinutes));
            }
        }

        // If no data, add placeholder
        if (pieChartData.isEmpty()) {
            pieChartData.add(new PieChart.Data("Pas encore de données", 1));
        }

        subjectDistributionChart.setData(pieChartData);

        // Apply animations to pie chart slices
        AtomicInteger i = new AtomicInteger(0);
        pieChartData.forEach(data -> {
            data.getNode().setStyle("-fx-pie-color: " + getColorForIndex(i.getAndIncrement()) + ";");

            data.getNode().setOnMouseEntered(e -> {
                data.getNode().setStyle("-fx-scale-x: 1.1; -fx-scale-y: 1.1; " + data.getNode().getStyle());
            });

            data.getNode().setOnMouseExited(e -> {
                data.getNode().setStyle(data.getNode().getStyle().replace("-fx-scale-x: 1.1; -fx-scale-y: 1.1; ", ""));
            });
        });
    }

    private String getColorForIndex(int index) {
        String[] colors = {
                "#3498db", "#2ecc71", "#e74c3c", "#f39c12", "#9b59b6",
                "#1abc9c", "#d35400", "#34495e", "#16a085", "#c0392b"
        };
        return colors[index % colors.length];
    }

    private void loadRecommendations() {
        try {
            // Generate recommendations
            recommendations = aiRecommendationService.generateRecommendations(currentUser);

            // Display recommendations in the UI
            if (recommendations != null && !recommendations.isEmpty()) {
                ObservableList<String> recommendationItems = FXCollections.observableArrayList();

                for (StudyRecommendation rec : recommendations) {
                    String priorityIndicator = "";
                    switch(rec.getPriority()) {
                        case 5: priorityIndicator = "🔴 "; break; // Highest priority
                        case 4: priorityIndicator = "🟠 "; break;
                        case 3: priorityIndicator = "🟡 "; break;
                        case 2: priorityIndicator = "🔵 "; break;
                        default: priorityIndicator = "⚪ "; break;
                    }

                    String item = priorityIndicator + rec.getSubject().getName() + " - " +
                            rec.getRecommendedMinutes() + " minutes";

                    if (rec.getDocument() != null) {
                        item += " - Document: " + rec.getDocument().getTitle();
                    }

                    recommendationItems.add(item);
                }

                recommendationsList.setItems(recommendationItems);

                // Set tooltip to show the reason for recommendation
                recommendationsList.setCellFactory(param -> new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty || item == null) {
                            setText(null);
                            setTooltip(null);
                        } else {
                            setText(item);

                            // Find the corresponding recommendation
                            int index = getIndex();
                            if (index >= 0 && index < recommendations.size()) {
                                StudyRecommendation rec = recommendations.get(index);
                                Tooltip tooltip = new Tooltip(rec.getReason());
                                setTooltip(tooltip);
                            }
                        }
                    }
                });
            } else {
                // If no recommendations yet, show default tips
                ObservableList<String> defaultTips = FXCollections.observableArrayList(
                        "⚪ Commencez par étudier régulièrement pour recevoir des recommandations personnalisées",
                        "⚪ Complétez des quiz pour améliorer vos recommandations",
                        "⚪ Évaluez votre confiance après chaque session d'étude"
                );
                recommendationsList.setItems(defaultTips);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error generating recommendations: " + e.getMessage());
        }
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
        statusColumn.setCellFactory(column -> new javafx.scene.control.TableCell<TimeSlotEntry, String>() {
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
    }

    @FXML
    private void handleDashboard() {
        // Already on dashboard
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
    private void handleUploadPDF() {
        navigateTo("/fxml/DocumentsView.fxml");
    }

    @FXML
    private void handleStartStudying() {
        navigateTo("/fxml/StudyView.fxml");
    }

    @FXML
    private void handlePrepareExam() {
        navigateTo("/fxml/ExamView.fxml");
    }

    @FXML
    private void handleGenerateExercises() {
        navigateTo("/fxml/ExamView.fxml");
    }

    @FXML
    private void handleViewStatistics() {
        showNotImplementedAlert("Statistiques détaillées");
    }

    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof TimetableController) {
                ((TimetableController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof DocumentsController) {
                ((DocumentsController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof StudyController) {
                ((StudyController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof ExamController) {
                ((ExamController) controller).setCurrentUser(currentUser);
            }

            // Stop the timeline to prevent memory leaks
            if (quoteChangeTimeline != null) {
                quoteChangeTimeline.stop();
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