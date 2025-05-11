package com.mustapha.revisia.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.Duration;

@Entity
@Table(name = "study_sessions")
public class StudySession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne
    @JoinColumn(name = "document_id")
    private Document document;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "focus_sessions_count")
    private Integer focusSessionsCount;

    @Column(name = "confidence_rating")
    private Integer confidenceRating;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Default constructor required by JPA
    public StudySession() {
        this.startTime = LocalDateTime.now();
    }

    // Constructor
    public StudySession(User user, Subject subject, Document document,
                        LocalDateTime startTime, LocalDateTime endTime, Integer confidenceRating, String notes) {
        this.user = user;
        this.subject = subject;
        this.document = document;
        this.startTime = startTime;
        this.endTime = endTime;
        this.confidenceRating = confidenceRating;
        this.notes = notes;
        this.durationMinutes = calculateDurationInMinutes();
        this.focusSessionsCount = 0; // Default value
    }

    // Helper method to calculate duration in minutes
    private Integer calculateDurationInMinutes() {
        if (startTime != null && endTime != null) {
            return (int) Duration.between(startTime, endTime).toMinutes();
        }
        return 0;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
        this.durationMinutes = calculateDurationInMinutes();
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
        this.durationMinutes = calculateDurationInMinutes();
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Integer getFocusSessionsCount() {
        return focusSessionsCount;
    }

    public void setFocusSessionsCount(Integer focusSessionsCount) {
        this.focusSessionsCount = focusSessionsCount;
    }

    public Integer getConfidenceRating() {
        return confidenceRating;
    }

    public void setConfidenceRating(Integer confidenceRating) {
        this.confidenceRating = confidenceRating;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Utility method to get formatted duration as string (e.g. "1h 30m")
    public String getFormattedDuration() {
        if (durationMinutes == null) return "0m";

        int hours = durationMinutes / 60;
        int minutes = durationMinutes % 60;

        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else {
            return String.format("%dm", minutes);
        }
    }
}