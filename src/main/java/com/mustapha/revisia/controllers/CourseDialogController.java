package com.mustapha.revisia.controllers;

import com.mustapha.revisia.models.Subject;
import com.mustapha.revisia.models.User;
import com.mustapha.revisia.services.SubjectService;
import com.mustapha.revisia.services.SubjectServiceImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

public class CourseDialogController implements Initializable {

    @FXML
    private TextField nameField;

    @FXML
    private TextField professorField;

    private Stage dialogStage;
    private User currentUser;
    private Subject subject; // If editing an existing subject
    private final SubjectService subjectService = new SubjectServiceImpl();
    private Runnable callback;
    private boolean isEdit = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Nothing to initialize yet
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
        isEdit = true;

        // Fill fields with subject data
        nameField.setText(subject.getName());
        if (subject.getProfessorName() != null) {
            professorField.setText(subject.getProfessorName());
        }
    }

    public void setCallback(Runnable callback) {
        this.callback = callback;
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        dialogStage.close();
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (validateInput()) {
            try {
                String name = nameField.getText().trim();
                String professor = professorField.getText().trim();

                // Check if professor field is empty
                if (professor.isEmpty()) {
                    professor = null;
                }

                if (isEdit) {
                    // Update existing subject
                    subject.setName(name);
                    subject.setProfessorName(professor);
                    subjectService.updateSubject(subject);
                    System.out.println("Updated subject: " + subject.getName());
                } else {
                    // Create new subject
                    Subject newSubject = new Subject(name, professor, currentUser);
                    subjectService.saveSubject(newSubject);
                    System.out.println("Created new subject: " + newSubject.getName());
                }

                // Close dialog and refresh timetable
                dialogStage.close();
                if (callback != null) {
                    callback.run();
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Error saving subject: " + e.getMessage());
            }
        }
    }
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean validateInput() {
        String errorMessage = "";

        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            errorMessage += "Le nom de la matière ne peut pas être vide !\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            // Show validation error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de validation");
            alert.setHeaderText(null);
            alert.setContentText(errorMessage);
            alert.showAndWait();
            return false;
        }
    }
}