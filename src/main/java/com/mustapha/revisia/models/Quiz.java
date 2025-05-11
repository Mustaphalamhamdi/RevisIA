package com.mustapha.revisia.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quizzes")
public class Quiz {

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

    @Column(nullable = false)
    private String title;

    @ManyToMany
    @JoinTable(
            name = "quiz_questions",
            joinColumns = @JoinColumn(name = "quiz_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    private List<Question> questions = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "quiz_answers",
            joinColumns = @JoinColumn(name = "quiz_id"))
    @Column(name = "selected_answer_index")
    private List<Integer> selectedAnswers = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "score")
    private Integer score;

    // Default constructor
    public Quiz() {
        this.createdAt = LocalDateTime.now();
    }

    // Constructor
    public Quiz(User user, Subject subject, Document document, String title, List<Question> questions) {
        this.user = user;
        this.subject = subject;
        this.document = document;
        this.title = title;
        this.questions = questions;
        this.createdAt = LocalDateTime.now();

        // Initialize selected answers list with nulls
        for (int i = 0; i < questions.size(); i++) {
            this.selectedAnswers.add(null);
        }
    }

    // Getters and Setters
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public List<Integer> getSelectedAnswers() {
        return selectedAnswers;
    }

    public void setSelectedAnswers(List<Integer> selectedAnswers) {
        this.selectedAnswers = selectedAnswers;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    // Helper methods
    public void setSelectedAnswer(int questionIndex, Integer answerIndex) {
        if (questionIndex >= 0 && questionIndex < selectedAnswers.size()) {
            selectedAnswers.set(questionIndex, answerIndex);
        }
    }

    public Integer getSelectedAnswer(int questionIndex) {
        if (questionIndex >= 0 && questionIndex < selectedAnswers.size()) {
            return selectedAnswers.get(questionIndex);
        }
        return null;
    }

    public boolean isQuestionAnswered(int questionIndex) {
        return getSelectedAnswer(questionIndex) != null;
    }

    public boolean isCompleted() {
        return completedAt != null;
    }

    public int getTotalQuestions() {
        return questions.size();
    }

    public int getAnsweredQuestions() {
        int count = 0;
        for (Integer answer : selectedAnswers) {
            if (answer != null) {
                count++;
            }
        }
        return count;
    }

    public void calculateScore() {
        int correctAnswers = 0;
        for (int i = 0; i < questions.size(); i++) {
            Integer selectedAnswer = getSelectedAnswer(i);
            if (selectedAnswer != null && questions.get(i).isCorrectAnswer(selectedAnswer)) {
                correctAnswers++;
            }
        }
        this.score = correctAnswers;
    }

    public double getScorePercentage() {
        if (questions.isEmpty()) return 0;
        return (double) score / questions.size();
    }

    public List<Question> getIncorrectlyAnsweredQuestions() {
        List<Question> incorrectQuestions = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            Integer selectedAnswer = getSelectedAnswer(i);
            Question question = questions.get(i);
            if (selectedAnswer != null && !question.isCorrectAnswer(selectedAnswer)) {
                incorrectQuestions.add(question);
            }
        }
        return incorrectQuestions;
    }

    public void complete() {
        this.completedAt = LocalDateTime.now();
        calculateScore();
    }
}