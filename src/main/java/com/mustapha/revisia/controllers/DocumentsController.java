package com.mustapha.revisia.controllers;

import com.mustapha.revisia.models.Document;
import com.mustapha.revisia.models.Subject;
import com.mustapha.revisia.models.User;
import com.mustapha.revisia.services.DocumentService;
import com.mustapha.revisia.services.DocumentServiceImpl;
import com.mustapha.revisia.services.SubjectService;
import com.mustapha.revisia.services.SubjectServiceImpl;
import com.mustapha.revisia.util.FileUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class DocumentsController implements Initializable {

    @FXML
    private Label usernameLabel;

    @FXML
    private ComboBox<Subject> subjectFilterComboBox;

    @FXML
    private TableView<Document> documentsTable;

    @FXML
    private TableColumn<Document, String> titleColumn;

    @FXML
    private TableColumn<Document, String> subjectColumn;

    @FXML
    private TableColumn<Document, String> uploadDateColumn;

    @FXML
    private TableColumn<Document, String> descriptionColumn;

    @FXML
    private TableColumn<Document, Void> actionsColumn;

    private final DocumentService documentService = new DocumentServiceImpl();
    private final SubjectService subjectService = new SubjectServiceImpl();

    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configure table columns
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

        subjectColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSubject() != null ?
                        cellData.getValue().getSubject().getName() : ""));

        uploadDateColumn.setCellValueFactory(cellData -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return new SimpleStringProperty(
                    cellData.getValue().getUploadDate().format(formatter));
        });

        descriptionColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDescription() != null ?
                        cellData.getValue().getDescription() : ""));

        // Add action buttons to the table
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("Voir");
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");

            {
                viewButton.getStyleClass().add("link-button");
                editButton.getStyleClass().add("link-button");
                deleteButton.getStyleClass().add("link-button");

                viewButton.setOnAction(event -> {
                    Document document = getTableView().getItems().get(getIndex());
                    openDocument(document);
                });

                editButton.setOnAction(event -> {
                    Document document = getTableView().getItems().get(getIndex());
                    editDocument(document);
                });

                deleteButton.setOnAction(event -> {
                    Document document = getTableView().getItems().get(getIndex());
                    deleteDocument(document);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    VBox buttons = new VBox(5, viewButton, editButton, deleteButton);
                    setGraphic(buttons);
                }
            }
        });

        // Listen for subject filter changes
        subjectFilterComboBox.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> loadDocuments(newVal));
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        usernameLabel.setText(user.getUsername());

        // Load subjects for filter
        try {
            List<Subject> subjects = subjectService.getSubjectsByUser(currentUser);

            // Create a special "All subjects" option
            Subject allSubjects = new Subject("Toutes les matières", "", currentUser);
            allSubjects.setId(0L); // Special ID to indicate all subjects

            // Create observable list for combobox
            ObservableList<Subject> subjectList = FXCollections.observableArrayList();
            subjectList.add(allSubjects); // Add "All subjects" option

            // Add actual subjects if any exist
            if (subjects != null && !subjects.isEmpty()) {
                subjectList.addAll(subjects);
            }

            subjectFilterComboBox.setItems(subjectList);
            subjectFilterComboBox.getSelectionModel().selectFirst(); // Select "All subjects" by default

            // Load all documents initially
            loadDocuments(allSubjects);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les matières: " + e.getMessage());
        }
    }

    private void loadDocuments(Subject subject) {
        try {
            List<Document> documents;

            if (subject == null || subject.getId() == 0L) {
                // Load all documents for this user
                documents = documentService.getDocumentsByUser(currentUser);
            } else {
                // Load documents for specific subject
                documents = documentService.getDocumentsBySubject(subject);
            }

            if (documents != null) {
                ObservableList<Document> documentsList = FXCollections.observableArrayList(documents);
                documentsTable.setItems(documentsList);

                // Log for debugging
                System.out.println("Loaded " + documents.size() + " documents");
            } else {
                // If no documents or error occurred
                documentsTable.setItems(FXCollections.observableArrayList());
                System.out.println("No documents found or error occurred");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les documents: " + e.getMessage());
        }
    }

    private void openDocument(Document document) {
        try {
            File file = FileUtil.getFile(document.getFilePath());
            if (file.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le fichier. Vérifiez qu'il existe toujours.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de l'ouverture du fichier: " + e.getMessage());
        }
    }

    private void editDocument(Document document) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DocumentDialog.fxml"));
            Parent dialogRoot = loader.load();

            DocumentDialogController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            controller.setDocument(document); // Set the document to edit

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Modifier un document");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(usernameLabel.getScene().getWindow());

            Scene scene = new Scene(dialogRoot);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            dialogStage.setScene(scene);

            // Set callback for when dialog is closed
            controller.setDialogStage(dialogStage);
            controller.setCallback(() -> {
                // Reload documents with current filter
                loadDocuments(subjectFilterComboBox.getValue());
            });

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre de modification de document.");
        }
    }

    private void deleteDocument(Document document) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer le document '" + document.getTitle() + "' ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Delete the file from disk
            boolean fileDeleted = FileUtil.deleteFile(document.getFilePath());

            // Delete the document from database
            documentService.deleteDocument(document);

            // Reload documents
            loadDocuments(subjectFilterComboBox.getValue());

            if (!fileDeleted) {
                showAlert(Alert.AlertType.WARNING, "Avertissement",
                        "Le document a été supprimé de la base de données, mais le fichier n'a pas pu être supprimé.");
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Document supprimé avec succès.");
            }
        }
    }

    @FXML
    public void handleAddDocument() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DocumentDialog.fxml"));
            Parent dialogRoot = loader.load();

            DocumentDialogController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Ajouter un document");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(usernameLabel.getScene().getWindow());

            Scene scene = new Scene(dialogRoot);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            dialogStage.setScene(scene);

            // Set callback for when dialog is closed
            controller.setDialogStage(dialogStage);
            controller.setCallback(() -> {
                // Reload documents with current filter
                loadDocuments(subjectFilterComboBox.getValue());
            });

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre d'ajout de document.");
        }
    }

    @FXML
    private void handleDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DashboardView.fxml"));
            Parent dashboardRoot = loader.load();

            DashboardController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Scene dashboardScene = new Scene(dashboardRoot);
            dashboardScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            stage.setScene(dashboardScene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Impossible de charger le tableau de bord.");
        }
    }

    @FXML
    private void handleTimetable(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TimetableView.fxml"));
            Parent timetableRoot = loader.load();

            TimetableController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Scene timetableScene = new Scene(timetableRoot);
            timetableScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            stage.setScene(timetableScene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Impossible de charger l'emploi du temps.");
        }
    }

    @FXML
    private void handleDocuments(ActionEvent event) {
        // Already on documents view
    }

    @FXML
    private void handleStudy() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StudyView.fxml"));
            Parent studyRoot = loader.load();

            StudyController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Scene studyScene = new Scene(studyRoot);
            studyScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            stage.setScene(studyScene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Impossible de charger le mode étude.");
        }
    }

    @FXML
    private void handleExams(ActionEvent event) {
        showNotImplementedAlert("Préparation aux examens");
    }

    @FXML
    private void handleSettings(ActionEvent event) {
        showNotImplementedAlert("Paramètres");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml"));
            Scene loginScene = new Scene(loginRoot);
            loginScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            stage.setScene(loginScene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de se déconnecter.");
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