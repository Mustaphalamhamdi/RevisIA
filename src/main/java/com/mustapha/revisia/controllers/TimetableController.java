package com.mustapha.revisia.controllers;

import com.mustapha.revisia.models.Subject;
import com.mustapha.revisia.models.TimeSlot;
import com.mustapha.revisia.models.User;
import com.mustapha.revisia.services.SubjectService;
import com.mustapha.revisia.services.SubjectServiceImpl;
import com.mustapha.revisia.services.TimeSlotService;
import com.mustapha.revisia.services.TimeSlotServiceImpl;
import com.mustapha.revisia.services.UserService;
import com.mustapha.revisia.services.UserServiceImpl;
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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.*;

public class TimetableController implements Initializable {

    @FXML
    private Label usernameLabel;

    @FXML
    private GridPane timetableGrid;

    @FXML
    private TableView<Subject> subjectsTable;

    @FXML
    private TableColumn<Subject, String> nameColumn;

    @FXML
    private TableColumn<Subject, String> professorColumn;

    @FXML
    private TableColumn<Subject, String> actionsColumn;

    private final SubjectService subjectService = new SubjectServiceImpl();
    private final TimeSlotService timeSlotService = new TimeSlotServiceImpl();
    private final UserService userService = new UserServiceImpl();

    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configure the table columns
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        professorColumn.setCellValueFactory(new PropertyValueFactory<>("professorName"));

        // Add action buttons to the table
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button addButton = new Button("Ajouter cours");
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");

            {
                addButton.getStyleClass().add("link-button");
                editButton.getStyleClass().add("link-button");
                deleteButton.getStyleClass().add("link-button");

                addButton.setOnAction(event -> {
                    Subject subject = getTableView().getItems().get(getIndex());
                    showTimeSlotDialog(subject);
                });

                editButton.setOnAction(event -> {
                    Subject subject = getTableView().getItems().get(getIndex());
                    showEditCourseDialog(subject);
                });

                deleteButton.setOnAction(event -> {
                    Subject subject = getTableView().getItems().get(getIndex());
                    deleteSubject(subject);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    VBox buttons = new VBox(5, addButton, editButton, deleteButton);
                    setGraphic(buttons);
                }
            }
        });
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        usernameLabel.setText(user.getUsername());
        loadSubjects();
        displayTimetable();
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
        // Already on timetable view
    }

    // In TimetableController.java
    @FXML
    private void handleDocuments(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DocumentsView.fxml"));
            Parent documentsRoot = loader.load();

            DocumentsController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Scene documentsScene = new Scene(documentsRoot);
            documentsScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(documentsScene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Impossible de charger la gestion des documents.");
        }
    }

    @FXML
    private void handleStudy(ActionEvent event) {
        showNotImplementedAlert("Mode étude");
    }

    @FXML
    private void handleExams(ActionEvent event) {
        showNotImplementedAlert("Préparation aux examens");
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

    @FXML
    private void handleAddCourse(ActionEvent event) {
        showAddCourseDialog();
    }

    private void loadSubjects() {
        List<Subject> subjects = subjectService.getSubjectsByUser(currentUser);
        ObservableList<Subject> subjectsList = FXCollections.observableArrayList(subjects);
        subjectsTable.setItems(subjectsList);
    }

    private void displayTimetable() {
        // Clear existing time slots
        for (int col = 1; col <= 6; col++) {
            final int finalCol = col;  // Create a final copy
            for (int row = 1; row <= 11; row++) {
                final int finalRow = row;  // Create a final copy
                // Clear any existing content at this cell
                timetableGrid.getChildren().removeIf(node ->
                        GridPane.getColumnIndex(node) != null &&
                                GridPane.getRowIndex(node) != null &&
                                GridPane.getColumnIndex(node) == finalCol &&
                                GridPane.getRowIndex(node) == finalRow);
            }
        }

        // Get all time slots for the current user
        List<TimeSlot> timeSlots = timeSlotService.getTimeSlotsByUser(currentUser);

        // Display each time slot in the grid
        for (TimeSlot slot : timeSlots) {
            // Calculate grid position
            int col = slot.getDay().getValue(); // 1 for Monday, 7 for Sunday
            if (col == 7) continue; // Skip Sunday

            int startHour = slot.getStartTime().getHour();
            int endHour = slot.getEndTime().getHour();

            // Adjust row to match our grid (row 1 is 8:00)
            int startRow = startHour - 7;
            int endRow = endHour - 7;

            if (startRow < 1) startRow = 1;
            if (startRow > 11) continue; // Out of our time range
            if (endRow > 11) endRow = 11;

            // Create a cell for this time slot
            VBox slotBox = createTimeSlotBox(slot);

            // Add to the grid
            timetableGrid.add(slotBox, col, startRow, 1, endRow - startRow + 1);
        }
    }

    private VBox createTimeSlotBox(TimeSlot slot) {
        VBox box = new VBox(5);
        box.getStyleClass().addAll("course-card");

        Label subjectLabel = new Label(slot.getSubject().getName());
        subjectLabel.getStyleClass().add("course-name");

        String timeText = String.format("%s - %s",
                slot.getStartTime().toString(),
                slot.getEndTime().toString());
        Label timeLabel = new Label(timeText);
        timeLabel.getStyleClass().add("course-time");

        String locationText = slot.getLocation() != null && !slot.getLocation().isEmpty()
                ? slot.getLocation()
                : "Pas de lieu spécifié";
        Label locationLabel = new Label(locationText);
        locationLabel.getStyleClass().add("course-location");

        box.getChildren().addAll(subjectLabel, timeLabel, locationLabel);

        // Add context menu for edit/delete
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Modifier");
        MenuItem deleteItem = new MenuItem("Supprimer");

        editItem.setOnAction(e -> showEditTimeSlotDialog(slot));
        deleteItem.setOnAction(e -> deleteTimeSlot(slot));

        contextMenu.getItems().addAll(editItem, deleteItem);
        box.setOnContextMenuRequested(e -> contextMenu.show(box, e.getScreenX(), e.getScreenY()));

        return box;
    }

    private void showAddCourseDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CourseDialog.fxml"));
            Parent dialogRoot = loader.load();

            CourseDialogController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Ajouter une matière");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(usernameLabel.getScene().getWindow());

            Scene scene = new Scene(dialogRoot);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            dialogStage.setScene(scene);

            // Set callback for when dialog is closed
            controller.setDialogStage(dialogStage);
            controller.setCallback(() -> {
                loadSubjects();
                displayTimetable();
            });

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre d'ajout de matière.");
        }
    }

    private void showEditCourseDialog(Subject subject) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CourseDialog.fxml"));
            Parent dialogRoot = loader.load();

            CourseDialogController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            controller.setSubject(subject); // Set the subject to edit

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Modifier une matière");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(usernameLabel.getScene().getWindow());

            Scene scene = new Scene(dialogRoot);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            dialogStage.setScene(scene);

            // Set callback for when dialog is closed
            controller.setDialogStage(dialogStage);
            controller.setCallback(() -> {
                loadSubjects();
                displayTimetable();
            });

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre de modification de matière.");
        }
    }

    private void showTimeSlotDialog(Subject subject) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TimeSlotDialog.fxml"));
            Parent dialogRoot = loader.load();

            TimeSlotDialogController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            controller.setSelectedSubject(subject); // Pre-select the subject

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Ajouter un cours à l'emploi du temps");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(usernameLabel.getScene().getWindow());

            Scene scene = new Scene(dialogRoot);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            dialogStage.setScene(scene);

            // Set callback for when dialog is closed
            controller.setDialogStage(dialogStage);
            controller.setCallback(() -> displayTimetable());

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre d'ajout de cours.");
        }
    }

    private void showEditTimeSlotDialog(TimeSlot timeSlot) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TimeSlotDialog.fxml"));
            Parent dialogRoot = loader.load();

            TimeSlotDialogController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            controller.setTimeSlot(timeSlot); // Set the time slot to edit

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Modifier un cours");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(usernameLabel.getScene().getWindow());

            Scene scene = new Scene(dialogRoot);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            dialogStage.setScene(scene);

            // Set callback for when dialog is closed
            controller.setDialogStage(dialogStage);
            controller.setCallback(() -> displayTimetable());

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre de modification de cours.");
        }
    }

    private void deleteSubject(Subject subject) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer la matière " + subject.getName() + " et tous ses cours associés ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // First check if there are any time slots associated with this subject
                List<TimeSlot> timeSlots = timeSlotService.getTimeSlotsBySubject(subject);

                // Delete all associated time slots first
                for (TimeSlot timeSlot : timeSlots) {
                    timeSlotService.deleteTimeSlot(timeSlot);
                }

                // Then delete the subject
                subjectService.deleteSubject(subject);

                // Refresh the UI
                loadSubjects();
                displayTimetable();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer la matière: " + e.getMessage());
            }
        }
    }

    private void deleteTimeSlot(TimeSlot timeSlot) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer ce cours de l'emploi du temps ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            timeSlotService.deleteTimeSlot(timeSlot);
            displayTimetable();
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