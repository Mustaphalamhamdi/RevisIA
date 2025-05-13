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

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class StudyController implements Initializable {

    @FXML private VBox studySetupPane;
    @FXML private VBox studySessionPane;
    @FXML private VBox studyCompletedPane;

    @FXML private Label usernameLabel;
    @FXML private ComboBox<Subject> subjectComboBox;
    @FXML private ComboBox<Document> documentComboBox;
    @FXML private Slider focusTimeSlider;
    @FXML private Label focusTimeLabel;
    @FXML private Slider breakTimeSlider;
    @FXML private Label breakTimeLabel;

    // Statistics elements
    @FXML private Label weeklyHoursLabel;
    @FXML private Label totalHoursLabel;
    @FXML private Label totalSessionsLabel;
    @FXML private Label streakLabel;
    @FXML private ListView<String> topSubjectsListView;
    @FXML private ListView<String> recentSessionsListView;

    @FXML private Label timerLabel;
    @FXML private ProgressBar timerProgressBar;
    @FXML private Label sessionStatusLabel;
    @FXML private Button timerControlButton;
    @FXML private Label subjectTitleLabel;
    @FXML private Label documentTitleLabel;

    @FXML private Label sessionSummaryLabel;
    @FXML private Label totalTimeLabel;
    @FXML private Label focusSessionsLabel;

    // Fix the missing variables by properly declaring them with @FXML
    @FXML private Slider confidenceSlider;
    @FXML private Label confidenceLabel;
    @FXML private TextArea notesTextArea;

    // Services
    private final SubjectService subjectService = new SubjectServiceImpl();
    private final DocumentService documentService = new DocumentServiceImpl();
    private final StudySessionService studySessionService = new StudySessionServiceImpl();
    private AIStudyRecommendationService aiRecommendationService;
    private List<StudyRecommendation> recommendations;

    // Study session variables
    private User currentUser;
    private Subject selectedSubject;
    private Document selectedDocument;
    private int focusTimeMinutes;
    private int breakTimeMinutes;
    private IntegerProperty secondsRemaining = new SimpleIntegerProperty();
    private Timeline timer;
    private boolean isBreak = false;
    private int completedFocusSessions = 0;
    private int totalTimeMinutes = 0;
    private LocalDateTime sessionStartTime;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize focus and break time sliders
        setupTimeSliders();

        // Setup subject and document selection
        setupSubjectCombobox();

        // Initialize timer display
        secondsRemaining.addListener((obs, oldVal, newVal) -> {
            updateTimerDisplay();
        });

        // Initialize confidence slider
        setupConfidenceSlider();

        // Initialize AI recommendation service
        aiRecommendationService = new AIStudyRecommendationService(
                new StudySessionServiceImpl(), new QuizServiceImpl());
    }

    private void setupTimeSliders() {
        // Focus time slider binding
        focusTimeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int minutes = newVal.intValue();
            focusTimeLabel.setText(minutes + " minutes");
        });

        // Break time slider binding
        breakTimeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int minutes = newVal.intValue();
            breakTimeLabel.setText(minutes + " minutes");
        });
    }

    private void setupSubjectCombobox() {
        // Subject converter
        subjectComboBox.setConverter(new StringConverter<Subject>() {
            @Override
            public String toString(Subject subject) {
                return subject != null ? subject.getName() : "";
            }

            @Override
            public Subject fromString(String string) {
                return null;
            }
        });

        // Listen for subject selection
        subjectComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                selectedSubject = newValue;
                loadDocumentsBySubject(newValue);
            }
        });

        // Document converter
        documentComboBox.setConverter(new StringConverter<Document>() {
            @Override
            public String toString(Document document) {
                return document != null ? document.getTitle() : "";
            }

            @Override
            public Document fromString(String string) {
                return null;
            }
        });

        // Listen for document selection
        documentComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                selectedDocument = newValue;
            }
        });
    }

    private void setupConfidenceSlider() {
        // Check if the slider is available before adding a listener
        if (confidenceSlider != null) {
            confidenceSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                int confidenceValue = newVal.intValue();
                String confidenceText;

                switch (confidenceValue) {
                    case 1:
                        confidenceText = "Très faible";
                        break;
                    case 2:
                        confidenceText = "Faible";
                        break;
                    case 3:
                        confidenceText = "Moyenne";
                        break;
                    case 4:
                        confidenceText = "Bonne";
                        break;
                    case 5:
                        confidenceText = "Excellente";
                        break;
                    default:
                        confidenceText = "Moyenne";
                        break;
                }

                if (confidenceLabel != null) {
                    confidenceLabel.setText(confidenceText);
                }
            });
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        usernameLabel.setText(user.getUsername());

        // Load subjects
        loadSubjects();

        // Load AI recommendations
        loadRecommendations();

        // Setup AI recommendation UI elements
        setupAIRecommendations();
    }

    private void loadSubjects() {
        try {
            List<Subject> subjects = subjectService.getSubjectsByUser(currentUser);
            if (subjects != null && !subjects.isEmpty()) {
                subjectComboBox.setItems(FXCollections.observableArrayList(subjects));
                subjectComboBox.getSelectionModel().selectFirst();
            } else {
                // No subjects found
                showAlert(Alert.AlertType.WARNING, "Aucune matière",
                        "Vous n'avez pas de matières. Veuillez d'abord créer une matière dans la section Emploi du temps.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les matières: " + e.getMessage());
        }
    }

    private void loadDocumentsBySubject(Subject subject) {
        try {
            // Clear previous items
            documentComboBox.getItems().clear();

            // Add a "No Document" option
            Document noDocument = new Document();
            noDocument.setTitle("Aucun document spécifique");
            documentComboBox.getItems().add(noDocument);

            // Load documents for the selected subject
            List<Document> documents = documentService.getDocumentsBySubject(subject);
            if (documents != null && !documents.isEmpty()) {
                documentComboBox.getItems().addAll(documents);
            }

            // Select "No Document" by default
            documentComboBox.getSelectionModel().selectFirst();
            selectedDocument = noDocument;

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les documents: " + e.getMessage());
        }
    }

    private void loadRecommendations() {
        try {
            recommendations = aiRecommendationService.generateRecommendations(currentUser);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error generating recommendations: " + e.getMessage());
        }
    }

    private void setupAIRecommendations() {
        if (recommendations == null || recommendations.isEmpty()) {
            return;
        }

        // Create AI recommendation section in the UI
        VBox aiRecommendationBox = new VBox(10);
        aiRecommendationBox.getStyleClass().addAll("card", "ai-recommendation-card");
        aiRecommendationBox.setPadding(new Insets(15));

        // Add header with AI badge
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("Recommandation IA");
        titleLabel.getStyleClass().add("card-title");

        Label aiBadge = new Label("IA");
        aiBadge.getStyleClass().add("ai-badge");

        headerBox.getChildren().addAll(titleLabel, aiBadge);

        // Get top recommendation
        StudyRecommendation topRecommendation = recommendations.get(0);

        // Create recommendation content
        VBox contentBox = new VBox(8);
        contentBox.setPadding(new Insets(10, 0, 0, 0));

        // Description with reason
        Label descriptionLabel = new Label(topRecommendation.getReason());
        descriptionLabel.setWrapText(true);

        // Subject info
        Label subjectLabel = new Label("Matière: " + topRecommendation.getSubject().getName());
        subjectLabel.setStyle("-fx-font-weight: bold;");

        // Document info if available
        Label documentLabel = null;
        if (topRecommendation.getDocument() != null) {
            documentLabel = new Label("Document: " + topRecommendation.getDocument().getTitle());
        }

        // Recommended time
        Label timeLabel = new Label("Durée recommandée: " +
                topRecommendation.getRecommendedMinutes() + " minutes");

        // Add all elements to content box
        contentBox.getChildren().add(descriptionLabel);
        contentBox.getChildren().add(subjectLabel);
        if (documentLabel != null) {
            contentBox.getChildren().add(documentLabel);
        }
        contentBox.getChildren().add(timeLabel);

        // Apply recommendation button
        Button applyButton = new Button("Appliquer cette recommandation");
        applyButton.getStyleClass().add("button-primary");
        applyButton.setMaxWidth(Double.MAX_VALUE);
        applyButton.setOnAction(event -> {
            // Set the recommended subject
            subjectComboBox.setValue(topRecommendation.getSubject());

            // Set document if available
            if (topRecommendation.getDocument() != null) {
                // Wait for documents to load and then select
                Platform.runLater(() -> {
                    if (documentComboBox.getItems().contains(topRecommendation.getDocument())) {
                        documentComboBox.setValue(topRecommendation.getDocument());
                    }
                });
            }

            // Set recommended focus time
            focusTimeSlider.setValue(topRecommendation.getRecommendedMinutes());
        });

        // Add all elements to the main box
        aiRecommendationBox.getChildren().addAll(headerBox, contentBox, applyButton);

        // Animation effect for AI recommendation
        aiRecommendationBox.getStyleClass().add("ai-animated");

        // Add to the main layout at the top
        studySetupPane.getChildren().add(0, aiRecommendationBox);
    }

    @FXML
    private void handleStartStudy() {
        // Get selected values
        Subject subject = subjectComboBox.getValue();
        Document document = documentComboBox.getValue();
        focusTimeMinutes = (int) focusTimeSlider.getValue();
        breakTimeMinutes = (int) breakTimeSlider.getValue();

        // Validate selections
        if (subject == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Veuillez sélectionner une matière.");
            return;
        }

        // Save selections
        selectedSubject = subject;
        if (document.getId() != null) { // Skip the "No Document" option
            selectedDocument = document;
        } else {
            selectedDocument = null;
        }

        // Set session titles
        subjectTitleLabel.setText(selectedSubject.getName());
        documentTitleLabel.setText(selectedDocument != null ? selectedDocument.getTitle() : "Étude générale");

        // Initialize timer
        isBreak = false;
        secondsRemaining.set(focusTimeMinutes * 60);
        timerProgressBar.setProgress(1.0);
        sessionStatusLabel.setText("Session de concentration");
        timerControlButton.setText("Pause");
        completedFocusSessions = 0;
        totalTimeMinutes = 0;

        // Show study session pane
        showStudySessionPane();

        // Start the timer
        startTimer();

        // Record session start time
        sessionStartTime = LocalDateTime.now();
    }

    private void showStudySessionPane() {
        studySetupPane.setVisible(false);
        studySetupPane.setManaged(false);
        studySessionPane.setVisible(true);
        studySessionPane.setManaged(true);
        studyCompletedPane.setVisible(false);
        studyCompletedPane.setManaged(false);
    }

    private void showStudyCompletedPane() {
        studySetupPane.setVisible(false);
        studySetupPane.setManaged(false);
        studySessionPane.setVisible(false);
        studySessionPane.setManaged(false);
        studyCompletedPane.setVisible(true);
        studyCompletedPane.setManaged(true);

        // Update summary
        sessionSummaryLabel.setText("Étude de " + selectedSubject.getName());
        if (selectedDocument != null) {
            sessionSummaryLabel.setText(sessionSummaryLabel.getText() + " - " + selectedDocument.getTitle());
        }

        totalTimeLabel.setText(totalTimeMinutes + " minutes");
        focusSessionsLabel.setText(completedFocusSessions + " sessions");
    }

    private void startTimer() {
        if (timer != null && timer.getStatus() == Animation.Status.RUNNING) {
            timer.stop();
        }

        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (secondsRemaining.get() > 0) {
                secondsRemaining.set(secondsRemaining.get() - 1);

                // Update progress bar
                if (isBreak) {
                    timerProgressBar.setProgress((double) secondsRemaining.get() / (breakTimeMinutes * 60));
                } else {
                    timerProgressBar.setProgress((double) secondsRemaining.get() / (focusTimeMinutes * 60));
                }
            } else {
                timer.stop();
                handleTimerComplete();
            }
        }));

        timer.setCycleCount(Animation.INDEFINITE);
        timer.play();
    }

    private void updateTimerDisplay() {
        int minutes = secondsRemaining.get() / 60;
        int seconds = secondsRemaining.get() % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private void handleTimerComplete() {
        if (isBreak) {
            // Break completed, start new focus session
            isBreak = false;
            secondsRemaining.set(focusTimeMinutes * 60);
            sessionStatusLabel.setText("Session de concentration");

            // Play alert sound
            // playSound("focus_start.wav");

            // Show alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Pause terminée");
            alert.setHeaderText(null);
            alert.setContentText("La pause est terminée. Une nouvelle session de concentration commence maintenant.");
            alert.showAndWait();

            // Start timer
            startTimer();
        } else {
            // Focus session completed
            completedFocusSessions++;
            totalTimeMinutes += focusTimeMinutes;

            // Play alert sound
            // playSound("focus_complete.wav");

            // Show break or finish alert
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Session terminée");
            alert.setHeaderText(null);
            alert.setContentText("Votre session de concentration est terminée. Souhaitez-vous prendre une pause ou terminer l'étude?");

            ButtonType breakButton = new ButtonType("Pause");
            ButtonType finishButton = new ButtonType("Terminer");

            alert.getButtonTypes().setAll(breakButton, finishButton);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == breakButton) {
                // Start break
                isBreak = true;
                secondsRemaining.set(breakTimeMinutes * 60);
                sessionStatusLabel.setText("Pause");
                startTimer();
            } else {
                // Finish session
                finishStudySession();
            }
        }
    }

    @FXML
    private void handleTimerControl() {
        if (timer.getStatus() == Animation.Status.RUNNING) {
            // Pause timer
            timer.pause();
            timerControlButton.setText("Reprendre");
        } else {
            // Resume timer
            timer.play();
            timerControlButton.setText("Pause");
        }
    }

    @FXML
    private void handleEndSession() {
        // Stop timer
        if (timer != null) {
            timer.stop();
        }

        // Confirm ending session
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Terminer la session");
        alert.setHeaderText(null);
        alert.setContentText("Êtes-vous sûr de vouloir terminer la session d'étude?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Add the current incomplete session time to total
            int elapsedSeconds = isBreak ?
                    (breakTimeMinutes * 60) - secondsRemaining.get() :
                    (focusTimeMinutes * 60) - secondsRemaining.get();

            totalTimeMinutes += elapsedSeconds / 60;

            // Finish session
            finishStudySession();
        } else {
            // Resume timer
            startTimer();
        }
    }

    private void finishStudySession() {
        // Show completed pane
        showStudyCompletedPane();
    }

    @FXML
    private void handlePauseResume(ActionEvent event) {
        // Forward to the timer control method
        handleTimerControl();
    }

    /**
     * Called when the skip button is clicked in the timer view.
     * Skips the current phase (focus or break) and moves to the next.
     */
    @FXML
    private void handleSkip(ActionEvent event) {
        if (timer != null) {
            timer.stop();

            if (isBreak) {
                // If in break, skip to next focus session
                isBreak = false;
                secondsRemaining.set(focusTimeMinutes * 60);
                sessionStatusLabel.setText("Concentration");

                // Update UI elements
                timerProgressBar.setProgress(1.0);
                timerControlButton.setText("Pause");

            } else {
                // If in focus session, record completion and skip to break
                completedFocusSessions++;

                // Add the current session time to total
                int elapsedSeconds = (focusTimeMinutes * 60) - secondsRemaining.get();
                totalTimeMinutes += elapsedSeconds / 60;

                // Move to break
                isBreak = true;
                secondsRemaining.set(breakTimeMinutes * 60);
                sessionStatusLabel.setText("Pause");
            }

            // Restart the timer
            startTimer();
        }
    }

    /**
     * Called when the stop button is clicked in the timer view.
     * Ends the current study session and moves to the completion view.
     */
    @FXML
    private void handleStop(ActionEvent event) {
        // Delegate to the end session handler
        handleEndSession();
    }

    @FXML
    private void handleSaveSession() {
        try {
            // Create new study session
            StudySession session = new StudySession();
            session.setUser(currentUser);
            session.setSubject(selectedSubject);
            session.setDocument(selectedDocument);
            session.setStartTime(sessionStartTime);
            session.setEndTime(LocalDateTime.now());
            session.setDurationMinutes(totalTimeMinutes);
            session.setFocusSessionsCount(completedFocusSessions);

            // Use confidence slider value if available, otherwise default to 3
            int confidenceValue = 3;
            if (confidenceSlider != null) {
                confidenceValue = (int) confidenceSlider.getValue();
            }
            session.setConfidenceRating(confidenceValue);

            // Use notes text area if available
            String notes = "";
            if (notesTextArea != null) {
                notes = notesTextArea.getText();
            }
            session.setNotes(notes);

            // Save to database
            studySessionService.saveStudySession(session);

            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Session enregistrée",
                    "Votre session d'étude a été enregistrée avec succès.");

            // Return to setup pane
            handleNewSession();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'enregistrer la session: " + e.getMessage());
        }
    }

    @FXML
    private void handleNewSession() {
        // Reset panes visibility
        studySetupPane.setVisible(true);
        studySetupPane.setManaged(true);
        studySessionPane.setVisible(false);
        studySessionPane.setManaged(false);
        studyCompletedPane.setVisible(false);
        studyCompletedPane.setManaged(false);

        // Reset inputs
        if (notesTextArea != null) {
            notesTextArea.clear();
        }

        if (confidenceSlider != null) {
            confidenceSlider.setValue(3);
        }

        // Add this code to refresh statistics
        loadStudyStatistics();
    }

    // Also implement this method if not already present:
    private void loadStudyStatistics() {
        try {
            // Get all study sessions for the user
            List<StudySession> sessions = studySessionService.getStudySessionsByUser(currentUser);

            // Calculate total study time
            int totalMinutes = sessions.stream().mapToInt(StudySession::getDurationMinutes).sum();
            int totalHours = totalMinutes / 60;
            int remainingMinutes = totalMinutes % 60;

            // Calculate weekly study time (last 7 days)
            LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
            int weeklyMinutes = sessions.stream()
                    .filter(s -> s.getEndTime().isAfter(weekAgo))
                    .mapToInt(StudySession::getDurationMinutes)
                    .sum();
            int weeklyHours = weeklyMinutes / 60;
            int weeklyRemainingMinutes = weeklyMinutes % 60;

            // Update UI elements
            if (weeklyHoursLabel != null) {
                weeklyHoursLabel.setText(weeklyHours + "h " + weeklyRemainingMinutes + "m");
            }

            if (totalHoursLabel != null) {
                totalHoursLabel.setText(totalHours + "h " + remainingMinutes + "m");
            }

            if (totalSessionsLabel != null) {
                totalSessionsLabel.setText(String.valueOf(sessions.size()));
            }

            // Calculate streak
            int streak = calculateStreak(sessions);
            if (streakLabel != null) {
                streakLabel.setText(String.valueOf(streak));
            }

            // Update subjects list
            updateTopSubjectsList(sessions);

            // Update recent sessions list
            updateRecentSessionsList(sessions);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading study statistics: " + e.getMessage());
        }
    }

    private int calculateStreak(List<StudySession> sessions) {
        // Basic streak calculation
        if (sessions.isEmpty()) {
            return 0;
        }

        // Sort sessions by end time (most recent first)
        sessions.sort((s1, s2) -> s2.getEndTime().compareTo(s1.getEndTime()));

        // Check if there's a session today
        LocalDate today = LocalDate.now();
        boolean studiedToday = sessions.stream()
                .anyMatch(s -> s.getEndTime().toLocalDate().equals(today));

        if (!studiedToday) {
            return 0;
        }

        // Count consecutive days
        int streak = 1;
        LocalDate currentDate = today.minusDays(1);

        while (true) {
            final LocalDate checkDate = currentDate;
            boolean hasSessionOnDate = sessions.stream()
                    .anyMatch(s -> s.getEndTime().toLocalDate().equals(checkDate));

            if (hasSessionOnDate) {
                streak++;
                currentDate = currentDate.minusDays(1);
            } else {
                break;
            }
        }

        return streak;
    }

    private void updateTopSubjectsList(List<StudySession> sessions) {
        if (topSubjectsListView == null) {
            return;
        }

        // Group sessions by subject and sum up the duration
        Map<Subject, Integer> subjectDurations = new HashMap<>();
        for (StudySession session : sessions) {
            Subject subject = session.getSubject();
            int duration = session.getDurationMinutes();

            subjectDurations.put(subject,
                    subjectDurations.getOrDefault(subject, 0) + duration);
        }

        // Sort subjects by duration (descending)
        List<Map.Entry<Subject, Integer>> sortedSubjects = new ArrayList<>(subjectDurations.entrySet());
        sortedSubjects.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        // Create display items
        ObservableList<String> items = FXCollections.observableArrayList();
        for (Map.Entry<Subject, Integer> entry : sortedSubjects) {
            Subject subject = entry.getKey();
            int minutes = entry.getValue();

            String item = subject.getName() + " - " + (minutes / 60) + "h " + (minutes % 60) + "m";
            items.add(item);
        }

        topSubjectsListView.setItems(items);
    }

    private void updateRecentSessionsList(List<StudySession> sessions) {
        if (recentSessionsListView == null) {
            return;
        }

        // Sort sessions by end time (most recent first)
        sessions.sort((s1, s2) -> s2.getEndTime().compareTo(s1.getEndTime()));

        // Take the most recent 5 sessions
        List<StudySession> recentSessions = sessions.stream()
                .limit(5)
                .collect(Collectors.toList());

        // Create display items
        ObservableList<String> items = FXCollections.observableArrayList();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (StudySession session : recentSessions) {
            String date = session.getEndTime().format(formatter);
            String subject = session.getSubject().getName();
            int minutes = session.getDurationMinutes();

            String item = date + " - " + subject + " (" + (minutes / 60) + "h " + (minutes % 60) + "m)";
            items.add(item);
        }

        recentSessionsListView.setItems(items);
    }

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
        // Already on study screen
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
            // Stop timer if running
            if (timer != null) {
                timer.stop();
            }

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
            // Stop timer if running
            if (timer != null) {
                timer.stop();
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof DashboardController) {
                ((DashboardController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof TimetableController) {
                ((TimetableController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof DocumentsController) {
                ((DocumentsController) controller).setCurrentUser(currentUser);
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
}