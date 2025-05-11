package com.mustapha.revisia.controllers;

import com.mustapha.revisia.models.Document;
import com.mustapha.revisia.models.Subject;
import com.mustapha.revisia.models.User;
import com.mustapha.revisia.services.DocumentService;
import com.mustapha.revisia.services.DocumentServiceImpl;
import com.mustapha.revisia.services.SubjectService;
import com.mustapha.revisia.services.SubjectServiceImpl;
import com.mustapha.revisia.util.FileUtil;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DocumentDialogController implements Initializable {

    @FXML
    private TextField titleField;

    @FXML
    private ComboBox<Subject> subjectComboBox;

    @FXML
    private TextField filePathField;

    @FXML
    private TextArea descriptionField;

    private Stage dialogStage;
    private User currentUser;
    private Document document; // If editing an existing document
    private final DocumentService documentService = new DocumentServiceImpl();
    private final SubjectService subjectService = new SubjectServiceImpl();
    private Runnable callback;
    private boolean isEdit = false;
    private File selectedFile;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Nothing to initialize yet
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;

        // Load subjects for this user
        try {
            List<Subject> subjects = subjectService.getSubjectsByUser(currentUser);
            if (subjects != null && !subjects.isEmpty()) {
                subjectComboBox.setItems(FXCollections.observableArrayList(subjects));
            } else {
                showAlert(Alert.AlertType.WARNING, "Attention",
                        "Vous n'avez pas encore créé de matières. Veuillez d'abord créer une matière dans la section Emploi du temps.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les matières: " + e.getMessage());
        }
    }

    public void setDocument(Document document) {
        this.document = document;
        isEdit = true;

        // Fill fields with document data
        titleField.setText(document.getTitle());
        subjectComboBox.setValue(document.getSubject());
        filePathField.setText(document.getFilePath());

        if (document.getDescription() != null) {
            descriptionField.setText(document.getDescription());
        }
    }

    public void setCallback(Runnable callback) {
        this.callback = callback;
    }

    @FXML
    private void handleBrowse(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un document PDF");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
        );

        selectedFile = fileChooser.showOpenDialog(dialogStage);
        if (selectedFile != null) {
            filePathField.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        dialogStage.close();
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (validateInput()) {
            String title = titleField.getText().trim();
            Subject subject = subjectComboBox.getValue();
            String description = descriptionField.getText().trim();

            // Check if description field is empty
            if (description.isEmpty()) {
                description = null;
            }

            try {
                if (isEdit) {
                    // Update existing document
                    document.setTitle(title);
                    document.setSubject(subject);
                    document.setDescription(description);

                    // If a new file was selected, update the file
                    if (selectedFile != null) {
                        // Delete the old file
                        FileUtil.deleteFile(document.getFilePath());

                        // Save the new file
                        String filePath = FileUtil.saveFile(selectedFile, currentUser.getId().toString());
                        document.setFileName(selectedFile.getName());
                        document.setFilePath(filePath);
                    }

                    documentService.updateDocument(document);
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Document mis à jour avec succès!");
                } else {
                    // Create new document
                    if (selectedFile != null) {
                        String filePath = FileUtil.saveFile(selectedFile, currentUser.getId().toString());
                        documentService.createDocument(title, selectedFile.getName(), filePath, description, subject, currentUser);
                        showAlert(Alert.AlertType.INFORMATION, "Succès", "Document ajouté avec succès!");
                    } else {
                        // This should not happen due to validation
                        throw new IOException("Aucun fichier sélectionné");
                    }
                }

                // Close dialog and refresh documents list
                dialogStage.close();
                if (callback != null) {
                    callback.run();
                }
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de sauvegarder le fichier: " + e.getMessage());
            }
        }
    }

    private boolean validateInput() {
        StringBuilder errorMessage = new StringBuilder();

        if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
            errorMessage.append("Le titre du document ne peut pas être vide !\n");
        }

        if (subjectComboBox.getValue() == null) {
            errorMessage.append("Vous devez sélectionner une matière !\n");
        }

        if (!isEdit && (selectedFile == null && (filePathField.getText() == null || filePathField.getText().isEmpty()))) {
            errorMessage.append("Vous devez sélectionner un fichier PDF !\n");
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            // Show validation error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de validation");
            alert.setHeaderText(null);
            alert.setContentText(errorMessage.toString());
            alert.showAndWait();
            return false;
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