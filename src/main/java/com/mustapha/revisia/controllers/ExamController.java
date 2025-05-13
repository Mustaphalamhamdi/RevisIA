package com.mustapha.revisia.controllers;

import com.mustapha.revisia.models.Document;
import com.mustapha.revisia.models.Question;
import com.mustapha.revisia.models.Quiz;
import com.mustapha.revisia.models.Subject;
import com.mustapha.revisia.models.User;
import com.mustapha.revisia.services.DocumentService;
import com.mustapha.revisia.services.DocumentServiceImpl;
import com.mustapha.revisia.services.QuizService;
import com.mustapha.revisia.services.QuizServiceImpl;
import com.mustapha.revisia.services.SubjectService;
import com.mustapha.revisia.services.SubjectServiceImpl;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import java.util.Arrays;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ExamController implements Initializable {

    // FXML elements for exam setup
    @FXML private Label usernameLabel;
    @FXML private ComboBox<Subject> subjectComboBox;
    @FXML private ComboBox<Document> documentComboBox;
    @FXML private Slider questionCountSlider;
    @FXML private Label questionCountLabel;
    @FXML private Label aiProcessingLabel;
    @FXML private TableView<Quiz> quizzesTableView;
    @FXML private TableColumn<Quiz, String> quizSubjectColumn;
    @FXML private TableColumn<Quiz, String> quizDocumentColumn;
    @FXML private TableColumn<Quiz, String> quizDateColumn;
    @FXML private TableColumn<Quiz, String> quizScoreColumn;
    @FXML private TableColumn<Quiz, Void> quizActionsColumn;
    @FXML private ListView<String> tipsListView;

    // FXML elements for quiz pane
    @FXML private VBox examSetupPane;
    @FXML private VBox quizPane;
    @FXML private Label quizTitleLabel;
    @FXML private Label questionCounterLabel;
    @FXML private Label questionLabel;
    @FXML private VBox answersBox;
    @FXML private Button previousButton;
    @FXML private Button nextButton;
    @FXML private Button finishButton;
    @FXML private ProgressBar quizProgressBar;

    // FXML elements for results pane
    @FXML private VBox resultsPane;
    @FXML private Label scoreLabel;
    @FXML private ProgressBar scoreProgressBar;
    @FXML private Label feedbackLabel;
    @FXML private ListView<Question> incorrectQuestionsListView;

    // Services
    private final SubjectService subjectService = new SubjectServiceImpl();
    private final DocumentService documentService = new DocumentServiceImpl();
    private final QuizService quizService = new QuizServiceImpl();

    // Current user and quiz
    private User currentUser;
    private Quiz currentQuiz;
    private int currentQuestionIndex = 0;
    private List<ToggleGroup> answerGroups = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Setup slider binding for question count
        questionCountSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int questions = newVal.intValue();
            questionCountLabel.setText(questions + " questions");
        });

        // Setup subject combobox
        setupSubjectCombobox();

        // Setup quizzes table
        setupQuizzesTable();

        // Initialize with study tips
        loadExamTips();

        // Setup AI label tooltip
        Tooltip aiTooltip = new Tooltip("Nos algorithmes d'IA analyseront vos documents pour générer des questions pertinentes");
        Tooltip.install(aiProcessingLabel, aiTooltip);
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        usernameLabel.setText(user.getUsername());

        // Load subjects
        loadSubjects();

        // Load recent quizzes
        loadQuizzes();
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
    }

    private void setupQuizzesTable() {
        // Setup columns
        quizSubjectColumn.setCellValueFactory(cellData -> {
            Quiz quiz = cellData.getValue();
            return new SimpleStringProperty(quiz.getSubject() != null ? quiz.getSubject().getName() : "");
        });

        quizDocumentColumn.setCellValueFactory(cellData -> {
            Quiz quiz = cellData.getValue();
            return new SimpleStringProperty(quiz.getDocument() != null ? quiz.getDocument().getTitle() : "Général");
        });

        quizDateColumn.setCellValueFactory(cellData -> {
            Quiz quiz = cellData.getValue();
            LocalDateTime date = quiz.getCompletedAt() != null ? quiz.getCompletedAt() : quiz.getCreatedAt();
            return new SimpleStringProperty(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        });

        quizScoreColumn.setCellValueFactory(cellData -> {
            Quiz quiz = cellData.getValue();
            if (quiz.getCompletedAt() != null && quiz.getScore() != null) {
                return new SimpleStringProperty(quiz.getScore() + "/" + quiz.getQuestions().size());
            } else {
                return new SimpleStringProperty("En cours");
            }
        });

        // Add action buttons
        quizActionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button continueButton = new Button("Continuer");

            {
                continueButton.setOnAction(event -> {
                    Quiz quiz = getTableView().getItems().get(getIndex());
                    loadQuiz(quiz);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Quiz quiz = getTableView().getItems().get(getIndex());
                    if (quiz.isCompleted()) {
                        Button reviewButton = new Button("Revoir");
                        reviewButton.setOnAction(event -> loadQuizResults(quiz));
                        setGraphic(reviewButton);
                    } else {
                        setGraphic(continueButton);
                    }
                }
            }
        });
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

            // Add a "All Documents" option
            Document allDocuments = new Document();
            allDocuments.setTitle("Tous les documents");
            documentComboBox.getItems().add(allDocuments);

            // Load documents for the selected subject
            List<Document> documents = documentService.getDocumentsBySubject(subject);
            if (documents != null && !documents.isEmpty()) {
                documentComboBox.getItems().addAll(documents);
            }

            // Select "All Documents" by default
            documentComboBox.getSelectionModel().selectFirst();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les documents: " + e.getMessage());
        }
    }

    private void loadQuizzes() {
        try {
            List<Quiz> quizzes = quizService.getRecentQuizzesByUser(currentUser, 10);
            if (quizzes != null && !quizzes.isEmpty()) {
                quizzesTableView.setItems(FXCollections.observableArrayList(quizzes));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les quiz: " + e.getMessage());
        }
    }

    private void loadExamTips() {
        ObservableList<String> tips = FXCollections.observableArrayList(
                "Révisez régulièrement plutôt qu'en une seule fois avant l'examen",
                "Faites des pauses régulières pendant vos révisions",
                "Testez-vous avec des quiz pour renforcer la mémorisation",
                "Révisez les sujets difficiles en premier, quand votre concentration est maximale",
                "Expliquez les concepts à quelqu'un d'autre pour vérifier votre compréhension",
                "Notre IA analyse vos PDF pour générer des questions de qualité spécifiques à vos cours",
                "Les quiz adaptatifs s'ajustent à votre niveau pour un apprentissage optimal"
        );
        tipsListView.setItems(tips);
    }

    @FXML
    private void handleGenerateQuestions() {
        try {
            Subject subject = subjectComboBox.getValue();
            Document document = documentComboBox.getValue();
            int questionCount = (int) questionCountSlider.getValue();

            if (subject == null) {
                showAlert(Alert.AlertType.WARNING, "Sélection requise", "Veuillez sélectionner une matière.");
                return;
            }

            // Show loading indicator and AI processing message
            ProgressIndicator progressIndicator = new ProgressIndicator();
            progressIndicator.setMaxSize(100, 100);

            VBox loadingPane = new VBox(15);
            loadingPane.setAlignment(Pos.CENTER);
            loadingPane.setPrefSize(400, 300);

            Label processingLabel = new Label("L'IA analyse vos documents...");
            processingLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            Label aiProcessingLabel = new Label("Génération de questions intelligentes en cours");
            aiProcessingLabel.getStyleClass().add("ai-badge");
            aiProcessingLabel.setPadding(new Insets(8, 15, 8, 15));

            loadingPane.getChildren().addAll(processingLabel, progressIndicator, aiProcessingLabel);

            // Replace the main content with loading indicator
            StackPane mainContainer = (StackPane) examSetupPane.getParent();
            mainContainer.getChildren().add(loadingPane);
            examSetupPane.setVisible(false);

            // Run question generation in background thread
            Task<Quiz> generateTask = new Task<>() {
                @Override
                protected Quiz call() throws Exception {
                    // Simulate AI processing time for UI feedback
                    Thread.sleep(1500);

                    Quiz quiz;

                    // Generate questions based on selection
                    if (document != null && document.getId() == null) {
                        // Generate quiz from subject
                        quiz = quizService.createQuizFromSubject(currentUser, subject, questionCount);
                    } else {
                        // Generate quiz from specific document
                        quiz = quizService.createQuizFromDocument(currentUser, document, questionCount);
                    }

                    return quiz;
                }
            };

            // When completed, remove loading indicator and show quiz
            generateTask.setOnSucceeded(e -> {
                // Remove loading indicator
                mainContainer.getChildren().remove(loadingPane);
                examSetupPane.setVisible(true);

                // Get the generated quiz
                Quiz quiz = generateTask.getValue();

                if (quiz != null) {
                    loadQuiz(quiz);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                            "Impossible de générer des questions. Veuillez uploader des documents pour cette matière.");
                }
            });

            generateTask.setOnFailed(e -> {
                // Remove loading indicator
                mainContainer.getChildren().remove(loadingPane);
                examSetupPane.setVisible(true);

                // Show error
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Impossible de générer les questions: " + generateTask.getException().getMessage());
                generateTask.getException().printStackTrace();
            });

            // Start the task
            new Thread(generateTask).start();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de générer les questions: " + e.getMessage());
        }
    }

    @FXML
    private void handleCreateCustomQuestions() {
        showNotImplementedAlert("Création de questions personnalisées");
    }

    private void loadQuiz(Quiz quiz) {
        currentQuiz = quiz;
        currentQuestionIndex = 0;

        // Update quiz title
        quizTitleLabel.setText(quiz.getTitle());

        // Show quiz pane, hide setup pane
        examSetupPane.setVisible(false);
        examSetupPane.setManaged(false);
        quizPane.setVisible(true);
        quizPane.setManaged(true);
        resultsPane.setVisible(false);
        resultsPane.setManaged(false);

        // Display first question
        displayCurrentQuestion();
    }

    @FXML
    private void displayCurrentQuestion() {
        // Clear previous answers
        answersBox.getChildren().clear();

        // Get current question
        Question question = currentQuiz.getQuestions().get(currentQuestionIndex);

        // Create question header with AI badge
        HBox questionHeader = new HBox(10);
        questionHeader.setAlignment(Pos.CENTER_LEFT);

        // Question title
        Label questionTitleLabel = new Label(question.getQuestionText());
        questionTitleLabel.setWrapText(true);
        questionTitleLabel.getStyleClass().add("question-text");
        questionHeader.getChildren().add(questionTitleLabel);

        // Add AI badge for non-user-created questions
        if (!question.isUserCreated()) {
            Label aiBadge = new Label("IA");
            aiBadge.getStyleClass().add("ai-badge");
            Tooltip tooltip = new Tooltip("Question générée par intelligence artificielle");
            Tooltip.install(aiBadge, tooltip);
            questionHeader.getChildren().add(aiBadge);
        }

        // Add question header to answers box
        answersBox.getChildren().add(questionHeader);

        // Update question counter
        int total = currentQuiz.getQuestions().size();
        questionCounterLabel.setText("Question " + (currentQuestionIndex + 1) + "/" + total);

        // Update progress bar
        quizProgressBar.setProgress((currentQuestionIndex + 1.0) / total);

        // Check if we're at the last question
        boolean isLastQuestion = currentQuestionIndex == total - 1;
        nextButton.setVisible(!isLastQuestion);
        nextButton.setManaged(!isLastQuestion);
        finishButton.setVisible(isLastQuestion);
        finishButton.setManaged(isLastQuestion);

        // Setup previous button (disabled on first question)
        previousButton.setDisable(currentQuestionIndex == 0);

        // Add difficulty indicator based on question's AI-evaluated difficulty
        if (question.getDifficultyLevel() != null) {
            String difficultyText = "";
            String difficultyStyle = "";

            switch (question.getDifficultyLevel()) {
                case 1:
                    difficultyText = "Facile";
                    difficultyStyle = "-fx-text-fill: #27ae60;"; // Green
                    break;
                case 2:
                    difficultyText = "Moyen-Facile";
                    difficultyStyle = "-fx-text-fill: #2ecc71;"; // Light green
                    break;
                case 3:
                    difficultyText = "Moyen";
                    difficultyStyle = "-fx-text-fill: #f39c12;"; // Orange
                    break;
                case 4:
                    difficultyText = "Moyen-Difficile";
                    difficultyStyle = "-fx-text-fill: #e67e22;"; // Dark orange
                    break;
                case 5:
                    difficultyText = "Difficile";
                    difficultyStyle = "-fx-text-fill: #e74c3c;"; // Red
                    break;
                default:
                    difficultyText = "Moyen";
                    difficultyStyle = "-fx-text-fill: #f39c12;";
            }

            Label difficultyLabel = new Label("Niveau: " + difficultyText);
            difficultyLabel.setStyle(difficultyStyle + " -fx-font-size: 12px; -fx-font-style: italic;");
            difficultyLabel.setPadding(new Insets(0, 0, 10, 0));
            answersBox.getChildren().add(difficultyLabel);
        }

        // Display answer options
        ToggleGroup answerGroup = new ToggleGroup();

        if (question.getOptions() != null && !question.getOptions().isEmpty()) {
            // Create a radio button for each option
            for (int i = 0; i < question.getOptions().size(); i++) {
                final int optionIndex = i;
                String optionText = question.getOptions().get(i);

                RadioButton optionButton = new RadioButton(optionText);
                optionButton.setToggleGroup(answerGroup);
                optionButton.setWrapText(true);
                optionButton.setMaxWidth(Double.MAX_VALUE);
                optionButton.getStyleClass().add("answer-option");

                // Set up event handling
                optionButton.setOnAction(e -> {
                    currentQuiz.setSelectedAnswer(currentQuestionIndex, optionIndex);
                    // Save answer to database
                    quizService.updateQuiz(currentQuiz);
                });

                // Check if this option was previously selected
                if (currentQuiz.getSelectedAnswer(currentQuestionIndex) != null &&
                        currentQuiz.getSelectedAnswer(currentQuestionIndex) == i) {
                    optionButton.setSelected(true);
                }

                // Add directly to the answers container
                answersBox.getChildren().add(optionButton);
            }
        } else {
            // If no options, provide a text field for open answer
            TextField answerField = new TextField();
            answerField.setPromptText("Entrez votre réponse ici...");

            // Save answer when changed
            answerField.textProperty().addListener((obs, oldVal, newVal) -> {
                // For open questions, we'll just store the text in the first option
                if (question.getOptions() == null || question.getOptions().isEmpty()) {
                    question.setOptions(Arrays.asList(newVal));
                } else {
                    question.getOptions().set(0, newVal);
                }

                // Update the quiz
                currentQuiz.updateQuestionOptions(currentQuestionIndex, question.getOptions());
                quizService.updateQuiz(currentQuiz);
            });

            answersBox.getChildren().add(answerField);
        }

        // Add an AI-powered hint button
        if (!currentQuiz.isCompleted()) {
            Button hintButton = new Button("Indice IA");
            hintButton.getStyleClass().add("link-button");
            hintButton.setOnAction(e -> showAIHint(question));

            HBox hintBox = new HBox(10);
            hintBox.setAlignment(Pos.CENTER_RIGHT);
            hintBox.getChildren().add(hintButton);
            hintBox.setPadding(new Insets(15, 0, 0, 0));

            answersBox.getChildren().add(hintBox);
        }
    }

    private void showAIHint(Question question) {
        // In a real implementation, this would generate a contextual hint
        // based on the question content and document analysis

        String hint;
        if (question.getCorrectOptionIndex() != null) {
            // Provide a vague hint based on the correct answer
            String correctAnswer = question.getOptions().get(question.getCorrectOptionIndex());

            // Generate a hint that doesn't directly give away the answer
            if (correctAnswer.length() > 3) {
                hint = "La réponse correcte commence par la lettre '" +
                        correctAnswer.substring(0, 1).toUpperCase() +
                        "' et contient " + correctAnswer.length() + " caractères.";
            } else {
                hint = "La réponse correcte est un terme court et concis.";
            }
        } else {
            hint = "Relisez attentivement la question et considérez les concepts clés du document.";
        }

        // Show hint in a dialog
        Alert hintAlert = new Alert(Alert.AlertType.INFORMATION);
        hintAlert.setTitle("Indice IA");
        hintAlert.setHeaderText("L'IA vous suggère:");
        hintAlert.setContentText(hint);

        // Add an AI badge to the dialog
        Label aiBadge = new Label("IA");
        aiBadge.getStyleClass().add("ai-badge");
        hintAlert.setGraphic(aiBadge);

        hintAlert.showAndWait();
    }

    @FXML
    private void handlePreviousQuestion() {
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--;
            displayCurrentQuestion();
        }
    }

    @FXML
    private void handleNextQuestion() {
        if (currentQuestionIndex < currentQuiz.getQuestions().size() - 1) {
            currentQuestionIndex++;
            displayCurrentQuestion();
        }
    }

    @FXML
    private void handleFinishQuiz() {
        // Make sure all questions are answered
        int unansweredCount = 0;
        for (int i = 0; i < currentQuiz.getQuestions().size(); i++) {
            if (!currentQuiz.isQuestionAnswered(i)) {
                unansweredCount++;
            }
        }

        if (unansweredCount > 0) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Questions non répondues");
            alert.setHeaderText(null);
            alert.setContentText("Il y a " + unansweredCount + " question(s) sans réponse. Êtes-vous sûr de vouloir terminer le quiz?");

            ButtonType buttonTypeYes = new ButtonType("Oui, terminer");
            ButtonType buttonTypeNo = new ButtonType("Non, continuer", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

            alert.showAndWait().ifPresent(type -> {
                if (type == buttonTypeYes) {
                    completeQuiz();
                }
            });
        } else {
            completeQuiz();
        }
    }

    private void completeQuiz() {
        try {
            // Mark quiz as completed and calculate score
            quizService.completeQuiz(currentQuiz);

            // Load results screen
            loadQuizResults(currentQuiz);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de terminer le quiz: " + e.getMessage());
        }
    }

    private void loadQuizResults(Quiz quiz) {
        this.currentQuiz = quiz;

        // Update score display
        int totalQuestions = quiz.getQuestions().size();
        int correctAnswers = quiz.getScore();
        double percentage = (double) correctAnswers / totalQuestions;

        scoreLabel.setText(correctAnswers + "/" + totalQuestions);
        scoreProgressBar.setProgress(percentage);

        // Set feedback based on score
        if (percentage >= 0.9) {
            feedbackLabel.setText("Excellent! Vous maîtrisez très bien ce sujet.");
        } else if (percentage >= 0.75) {
            feedbackLabel.setText("Très bien! Vous avez une bonne compréhension du sujet.");
        } else if (percentage >= 0.6) {
            feedbackLabel.setText("Bien! Continuez à travailler sur ce sujet.");
        } else if (percentage >= 0.4) {
            feedbackLabel.setText("Vous avez des bases, mais ce sujet nécessite plus de travail.");
        } else {
            feedbackLabel.setText("Ce sujet nécessite plus de révision. Courage!");
        }

        // Display incorrect questions
        List<Question> incorrect = quiz.getIncorrectlyAnsweredQuestions();
        incorrectQuestionsListView.setItems(FXCollections.observableArrayList(incorrect));

        // Setup cell factory for incorrect questions
        incorrectQuestionsListView.setCellFactory(param -> new ListCell<Question>() {
            @Override
            protected void updateItem(Question item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getQuestionText());
                }
            }
        });

        // Show results pane
        examSetupPane.setVisible(false);
        examSetupPane.setManaged(false);
        quizPane.setVisible(false);
        quizPane.setManaged(false);
        resultsPane.setVisible(true);
        resultsPane.setManaged(true);

        // Refresh quizzes table
        loadQuizzes();

        // Add AI recommendation for next steps
        VBox aiRecommendationBox = new VBox(10);
        aiRecommendationBox.getStyleClass().addAll("card", "ai-recommendation-card");
        aiRecommendationBox.setPadding(new Insets(15));

        // Add header with AI badge
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("Recommandation de l'IA");
        titleLabel.getStyleClass().add("card-title");

        Label aiBadge = new Label("IA");
        aiBadge.getStyleClass().add("ai-badge");

        headerBox.getChildren().addAll(titleLabel, aiBadge);

        // Create recommendation content based on quiz performance
        Label recommendationText = new Label(generateAIRecommendation(percentage, incorrect.size()));
        recommendationText.setWrapText(true);

        // Add to the results pane
        aiRecommendationBox.getChildren().addAll(headerBox, recommendationText);

        // Add the AI recommendation box to the results pane
        if (resultsPane.getChildren().size() > 3) {
            // Replace existing recommendation if it exists
            resultsPane.getChildren().set(3, aiRecommendationBox);
        } else {
            // Add new recommendation
            resultsPane.getChildren().add(aiRecommendationBox);
        }
    }

    private String generateAIRecommendation(double scorePercentage, int incorrectCount) {
        // Generate AI recommendation based on quiz performance
        if (scorePercentage >= 0.9) {
            return "Félicitations pour votre excellent score! Vous maîtrisez très bien ce sujet. "
                    + "Je recommande de passer à des concepts plus avancés ou d'explorer des domaines connexes pour approfondir vos connaissances.";
        } else if (scorePercentage >= 0.75) {
            return "Vous avez une bonne compréhension du sujet. "
                    + "Je suggère de revoir les " + incorrectCount + " questions que vous avez manquées, puis de vous concentrer sur les concepts connexes pour renforcer votre compréhension globale.";
        } else if (scorePercentage >= 0.6) {
            return "Vous avez une compréhension de base du sujet, mais il y a encore des lacunes à combler. "
                    + "Je recommande de réviser les sections du document liées aux questions manquées, puis de refaire un quiz similaire dans 2-3 jours pour renforcer votre apprentissage.";
        } else {
            return "Ce sujet nécessite plus de travail. Je suggère de reprendre l'étude depuis le début, "
                    + "en vous concentrant particulièrement sur les concepts fondamentaux. Divisez votre étude en sessions plus courtes et plus fréquentes, et réessayez dans quelques jours.";
        }
    }

    @FXML
    private void handleReviewQuestions() {
        // Go back to quiz mode but with answers shown
        examSetupPane.setVisible(false);
        examSetupPane.setManaged(false);
        quizPane.setVisible(true);
        quizPane.setManaged(true);
        resultsPane.setVisible(false);
        resultsPane.setManaged(false);

        // Start from the first question
        currentQuestionIndex = 0;
        displayCorrectAnswers();
    }

    private void displayCorrectAnswers() {
        // Clear previous answers
        answersBox.getChildren().clear();

        // Update question counter
        int total = currentQuiz.getQuestions().size();
        questionCounterLabel.setText("Question " + (currentQuestionIndex + 1) + "/" + total + " (Révision)");

        // Update progress bar
        quizProgressBar.setProgress((currentQuestionIndex + 1.0) / total);

        // Check if we're at the last question
        boolean isLastQuestion = currentQuestionIndex == total - 1;
        nextButton.setVisible(!isLastQuestion);
        nextButton.setManaged(!isLastQuestion);
        finishButton.setVisible(isLastQuestion);
        finishButton.setManaged(isLastQuestion);

        // Setup previous button (disabled on first question)
        previousButton.setDisable(currentQuestionIndex == 0);

        // Get current question
        Question question = currentQuiz.getQuestions().get(currentQuestionIndex);
        questionLabel.setText(question.getQuestionText());

        // Show difficulty level
        if (question.getDifficultyLevel() != null) {
            String difficultyText = "";
            String difficultyStyle = "";

            switch (question.getDifficultyLevel()) {
                case 1: difficultyText = "Facile"; difficultyStyle = "-fx-text-fill: #27ae60;"; break;
                case 2: difficultyText = "Moyen-Facile"; difficultyStyle = "-fx-text-fill: #2ecc71;"; break;
                case 3: difficultyText = "Moyen"; difficultyStyle = "-fx-text-fill: #f39c12;"; break;
                case 4: difficultyText = "Moyen-Difficile"; difficultyStyle = "-fx-text-fill: #e67e22;"; break;
                case 5: difficultyText = "Difficile"; difficultyStyle = "-fx-text-fill: #e74c3c;"; break;
                default: difficultyText = "Moyen"; difficultyStyle = "-fx-text-fill: #f39c12;";
            }

            Label difficultyLabel = new Label("Niveau: " + difficultyText);
            difficultyLabel.setStyle(difficultyStyle + " -fx-font-size: 12px; -fx-font-style: italic;");
            difficultyLabel.setPadding(new Insets(0, 0, 10, 0));
            answersBox.getChildren().add(difficultyLabel);
        }

        // Display answer options with correct/incorrect marking
        for (int i = 0; i < question.getOptions().size(); i++) {
            final int answerIndex = i;
            String optionText = question.getOptions().get(i);

            // Create option as label instead of radio button
            Label optionLabel = new Label(optionText);
            optionLabel.setWrapText(true);
            optionLabel.setMaxWidth(Double.MAX_VALUE);
            optionLabel.setPadding(new Insets(10, 15, 10, 15));

            // Style based on correctness
            if (question.isCorrectAnswer(answerIndex)) {
                optionLabel.getStyleClass().add("answer-correct");
                optionLabel.setText("✓ " + optionText + " (Réponse correcte)");
            } else if (currentQuiz.getSelectedAnswer(currentQuestionIndex) != null &&
                    currentQuiz.getSelectedAnswer(currentQuestionIndex) == answerIndex) {
                optionLabel.getStyleClass().add("answer-incorrect");
                optionLabel.setText("✗ " + optionText + " (Votre réponse)");
            } else {
                optionLabel.getStyleClass().add("answer-option");
            }

            answersBox.getChildren().add(optionLabel);
        }

        // Add explanation if available (AI-generated)
        String explanation = generateExplanation(question);

        VBox explanationBox = new VBox(10);
        explanationBox.setPadding(new Insets(15, 0, 0, 0));

        Label explanationTitle = new Label("Explication:");
        explanationTitle.setStyle("-fx-font-weight: bold;");

        Label explanationText = new Label(explanation);
        explanationText.setWrapText(true);
        explanationText.setStyle("-fx-font-style: italic;");

        HBox aiLabelBox = new HBox(5);
        aiLabelBox.setAlignment(Pos.CENTER_RIGHT);
        Label aiExplanationLabel = new Label("Explication générée par IA");
        aiExplanationLabel.getStyleClass().add("ai-badge");
        aiExplanationLabel.setStyle("-fx-font-size: 10px;");
        aiLabelBox.getChildren().add(aiExplanationLabel);

        explanationBox.getChildren().addAll(explanationTitle, explanationText, aiLabelBox);
        answersBox.getChildren().add(explanationBox);

        // Disable buttons when in review mode
        finishButton.setText("Retour aux résultats");
        finishButton.setOnAction(e -> loadQuizResults(currentQuiz));
    }

    private String generateExplanation(Question question) {
        // In a real application, this would generate an explanation based on the document content
        // This is a simplified version

        if (question.getCorrectOptionIndex() == null) {
            return "Cette question est basée sur l'ensemble du contenu du document.";
        }

        String correctAnswer = question.getOptions().get(question.getCorrectOptionIndex());

        // Generate simple explanation based on question type
        if (question.getQuestionText().contains("Complétez")) {
            return "Le terme \"" + correctAnswer + "\" est le seul qui complète correctement la phrase selon le contexte du document.";
        } else if (question.getQuestionText().contains("définition")) {
            return "La définition correcte est celle qui correspond au concept tel qu'il est expliqué dans le document.";
        } else if (question.getQuestionText().contains("vrai ou faux")) {
            return "Selon le document, cette affirmation est " + (question.getCorrectOptionIndex() == 0 ? "vraie" : "fausse") + ".";
        } else {
            return "La réponse \"" + correctAnswer + "\" est celle qui correspond le mieux aux informations présentées dans le document.";
        }
    }

    @FXML
    private void handleBackToExams() {
        // Return to exam setup screen
        examSetupPane.setVisible(true);
        examSetupPane.setManaged(true);
        quizPane.setVisible(false);
        quizPane.setManaged(false);
        resultsPane.setVisible(false);
        resultsPane.setManaged(false);
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
        navigateTo("/fxml/StudyView.fxml");
    }

    @FXML
    private void handleExams() {
        // Already on exams screen
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