package com.mustapha.revisia.controllers;

import com.mustapha.revisia.models.Subject;
import com.mustapha.revisia.models.TimeSlot;
import com.mustapha.revisia.models.User;
import com.mustapha.revisia.services.SubjectService;
import com.mustapha.revisia.services.SubjectServiceImpl;
import com.mustapha.revisia.services.TimeSlotService;
import com.mustapha.revisia.services.TimeSlotServiceImpl;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TimeSlotDialogController implements Initializable {

    @FXML
    private ComboBox<Subject> subjectComboBox;

    @FXML
    private ComboBox<String> dayComboBox;

    @FXML
    private TextField startTimeField;

    @FXML
    private TextField endTimeField;

    @FXML
    private TextField locationField;

    private Stage dialogStage;
    private User currentUser;
    private TimeSlot timeSlot; // If editing existing time slot
    private final SubjectService subjectService = new SubjectServiceImpl();
    private final TimeSlotService timeSlotService = new TimeSlotServiceImpl();
    private Runnable callback;
    private boolean isEdit = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize days of week combobox
        List<String> dayNames = Stream.of(DayOfWeek.values())
                .filter(day -> day != DayOfWeek.SUNDAY) // Exclude Sunday
                .map(day -> day.getDisplayName(TextStyle.FULL, Locale.FRENCH))
                .collect(Collectors.toList());
        dayComboBox.setItems(FXCollections.observableArrayList(dayNames));

        // Setup a custom cell factory for the subject combobox to show subject names nicely
        subjectComboBox.setCellFactory(param -> new ListCell<Subject>() {
            @Override
            protected void updateItem(Subject item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });

        // Also set a custom string converter for the selected value
        subjectComboBox.setButtonCell(new ListCell<Subject>() {
            @Override
            protected void updateItem(Subject item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;

        // Load subjects for this user
        List<Subject> subjects = subjectService.getSubjectsByUser(currentUser);
        subjectComboBox.setItems(FXCollections.observableArrayList(subjects));
    }

    public void setSelectedSubject(Subject subject) {
        subjectComboBox.setValue(subject);
    }

    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
        isEdit = true;

        // Fill fields with time slot data
        subjectComboBox.setValue(timeSlot.getSubject());

        String dayName = timeSlot.getDay().getDisplayName(TextStyle.FULL, Locale.FRENCH);
        dayComboBox.setValue(dayName);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        startTimeField.setText(timeSlot.getStartTime().format(formatter));
        endTimeField.setText(timeSlot.getEndTime().format(formatter));

        if (timeSlot.getLocation() != null) {
            locationField.setText(timeSlot.getLocation());
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
            Subject subject = subjectComboBox.getValue();

            // Convert day name to DayOfWeek enum
            String dayName = dayComboBox.getValue();
            DayOfWeek day = Stream.of(DayOfWeek.values())
                    .filter(d -> d.getDisplayName(TextStyle.FULL, Locale.FRENCH).equals(dayName))
                    .findFirst()
                    .orElse(DayOfWeek.MONDAY);

            // Parse time fields
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime startTime = LocalTime.parse(startTimeField.getText(), formatter);
            LocalTime endTime = LocalTime.parse(endTimeField.getText(), formatter);

            String location = locationField.getText().trim();
            if (location.isEmpty()) {
                location = null;
            }

            if (isEdit) {
                // Update existing time slot
                timeSlot.setSubject(subject);
                timeSlot.setDay(day);
                timeSlot.setStartTime(startTime);
                timeSlot.setEndTime(endTime);
                timeSlot.setLocation(location);
                timeSlotService.updateTimeSlot(timeSlot);
            } else {
                // Create new time slot
                timeSlotService.createTimeSlot(subject, day, startTime, endTime, location, currentUser);
            }

            // Close dialog and refresh timetable
            dialogStage.close();
            if (callback != null) {
                callback.run();
            }
        }
    }

    private boolean validateInput() {
        StringBuilder errorMessage = new StringBuilder();

        if (subjectComboBox.getValue() == null) {
            errorMessage.append("Vous devez sélectionner une matière !\n");
        }

        if (dayComboBox.getValue() == null) {
            errorMessage.append("Vous devez sélectionner un jour !\n");
        }

        // Validate start time
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime.parse(startTimeField.getText(), formatter);
        } catch (DateTimeParseException e) {
            errorMessage.append("L'heure de début doit être au format HH:MM (ex: 08:30) !\n");
        }

        // Validate end time
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime endTime = LocalTime.parse(endTimeField.getText(), formatter);

            // If start time is valid, check that end time is after start time
            try {
                LocalTime startTime = LocalTime.parse(startTimeField.getText(), formatter);
                if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
                    errorMessage.append("L'heure de fin doit être après l'heure de début !\n");
                }
            } catch (DateTimeParseException ignored) {
                // Start time was invalid, already handled above
            }
        } catch (DateTimeParseException e) {
            errorMessage.append("L'heure de fin doit être au format HH:MM (ex: 10:00) !\n");
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
}