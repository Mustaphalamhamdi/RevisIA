package com.mustapha.revisia.models;

public class StudyRecommendation {
    private Subject subject;
    private Document document;
    private int recommendedMinutes;
    private String reason;
    private int priority; // 1-5, with 5 being highest priority

    // Constructor
    public StudyRecommendation(Subject subject, Document document, int recommendedMinutes, String reason, int priority) {
        this.subject = subject;
        this.document = document;
        this.recommendedMinutes = recommendedMinutes;
        this.reason = reason;
        this.priority = priority;
    }

    // Getters and setters
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

    public int getRecommendedMinutes() {
        return recommendedMinutes;
    }

    public void setRecommendedMinutes(int recommendedMinutes) {
        this.recommendedMinutes = recommendedMinutes;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}