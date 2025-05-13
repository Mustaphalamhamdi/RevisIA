package com.mustapha.revisia.services;

import com.mustapha.revisia.models.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class AIStudyRecommendationService {

    private final StudySessionService studySessionService;
    private final QuizService quizService;

    public AIStudyRecommendationService(StudySessionService studySessionService, QuizService quizService) {
        this.studySessionService = studySessionService;
        this.quizService = quizService;
    }

    /**
     * Generate personalized study recommendations based on user data
     */
    public List<StudyRecommendation> generateRecommendations(User user) {
        List<StudyRecommendation> recommendations = new ArrayList<>();

        // Get user data
        List<StudySession> allSessions = studySessionService.getStudySessionsByUser(user);
        List<Quiz> completedQuizzes = quizService.getQuizzesByUser(user).stream()
                .filter(Quiz::isCompleted)
                .collect(Collectors.toList());

        // Get all subjects the user is studying
        Set<Subject> subjects = new HashSet<>();
        for (StudySession session : allSessions) {
            subjects.add(session.getSubject());
        }
        for (Quiz quiz : completedQuizzes) {
            subjects.add(quiz.getSubject());
        }

        // For each subject, generate recommendations
        for (Subject subject : subjects) {
            // Calculate metrics for this subject
            SubjectMetrics metrics = calculateSubjectMetrics(subject, allSessions, completedQuizzes);

            // Generate recommendation based on metrics
            StudyRecommendation recommendation = createRecommendation(subject, metrics);
            if (recommendation != null) {
                recommendations.add(recommendation);
            }
        }

        // Sort recommendations by priority (highest first)
        recommendations.sort(Comparator.comparing(StudyRecommendation::getPriority).reversed());

        return recommendations;
    }

    private SubjectMetrics calculateSubjectMetrics(Subject subject,
                                                   List<StudySession> allSessions,
                                                   List<Quiz> completedQuizzes) {
        SubjectMetrics metrics = new SubjectMetrics();
        metrics.subject = subject;

        // Filter sessions and quizzes for this subject
        List<StudySession> subjectSessions = allSessions.stream()
                .filter(s -> s.getSubject().getId().equals(subject.getId()))
                .collect(Collectors.toList());

        List<Quiz> subjectQuizzes = completedQuizzes.stream()
                .filter(q -> q.getSubject().getId().equals(subject.getId()))
                .collect(Collectors.toList());

        // Calculate total study time
        metrics.totalStudyMinutes = subjectSessions.stream()
                .mapToInt(StudySession::getDurationMinutes)
                .sum();

        // Calculate average confidence
        metrics.averageConfidence = subjectSessions.stream()
                .mapToInt(StudySession::getConfidenceRating)
                .average()
                .orElse(3.0); // Default to middle value if no data

        // Calculate quiz performance
        if (!subjectQuizzes.isEmpty()) {
            metrics.quizPerformance = subjectQuizzes.stream()
                    .mapToDouble(q -> (double) q.getScore() / q.getQuestions().size())
                    .average()
                    .orElse(0.5); // Default to 50% if no data
        }

        // Find last study session
        metrics.daysSinceLastStudy = subjectSessions.stream()
                .map(StudySession::getEndTime)
                .max(LocalDateTime::compareTo)
                .map(date -> ChronoUnit.DAYS.between(date, LocalDateTime.now()))
                .orElse(Long.MAX_VALUE); // If never studied, set to max

        // Find documents with low confidence
        Map<Document, Double> documentConfidence = new HashMap<>();
        for (StudySession session : subjectSessions) {
            if (session.getDocument() != null) {
                Document doc = session.getDocument();
                double confidence = session.getConfidenceRating();

                // Update with weighted average (more recent sessions have higher weight)
                documentConfidence.put(doc,
                        documentConfidence.containsKey(doc)
                                ? (documentConfidence.get(doc) * 0.7 + confidence * 0.3)
                                : confidence);
            }
        }

        // Find document with lowest confidence
        if (!documentConfidence.isEmpty()) {
            metrics.lowestConfidenceDocument = documentConfidence.entrySet().stream()
                    .min(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

            metrics.lowestConfidenceValue = metrics.lowestConfidenceDocument != null
                    ? documentConfidence.get(metrics.lowestConfidenceDocument)
                    : 0;
        }

        return metrics;
    }

    private StudyRecommendation createRecommendation(Subject subject, SubjectMetrics metrics) {
        // Calculate priority (1-5)
        int priority = 1;
        String reason = "";
        Document recommendedDocument = null;
        int recommendedMinutes = 25; // Default Pomodoro session

        // Factor 1: Days since last study (higher = more priority)
        if (metrics.daysSinceLastStudy > 14) {
            priority += 2;
            reason += "Vous n'avez pas étudié cette matière depuis longtemps. ";
        } else if (metrics.daysSinceLastStudy > 7) {
            priority += 1;
            reason += "Cela fait plus d'une semaine depuis votre dernière session. ";
        }

        // Factor 2: Low confidence (lower = more priority)
        if (metrics.averageConfidence < 2.5) {
            priority += 2;
            reason += "Votre niveau de confiance dans cette matière est faible. ";
        } else if (metrics.averageConfidence < 3.5) {
            priority += 1;
            reason += "Vous pourriez améliorer votre confiance dans cette matière. ";
        }
            // Factor 3: Poor quiz performance (lower = more priority)
            if (metrics.quizPerformance < 0.6) {
                priority += 2;
                reason += "Vos résultats de quiz dans cette matière peuvent être améliorés. ";
            } else if (metrics.quizPerformance < 0.8) {
                priority += 1;
                reason += "Revoir cette matière pourrait améliorer vos résultats de quiz. ";
            }

            // Factor 4: Low confidence in specific document
            if (metrics.lowestConfidenceDocument != null && metrics.lowestConfidenceValue < 3) {
                recommendedDocument = metrics.lowestConfidenceDocument;
                reason += "Le document '" + recommendedDocument.getTitle() + "' mérite une révision approfondie. ";
            }

            // Calculate recommended study time
            if (metrics.averageConfidence < 3 || metrics.quizPerformance < 0.7) {
                recommendedMinutes = 45; // Longer session for difficult material
            } else if (metrics.daysSinceLastStudy > 10) {
                recommendedMinutes = 35; // Longer refresher
            }

            // Cap priority at 5
            priority = Math.min(priority, 5);

            // Only recommend if priority is at least 2
            if (priority >= 2) {
                return new StudyRecommendation(
                        subject, recommendedDocument, recommendedMinutes, reason.trim(), priority);
            }

            return null;
        }

        // Helper class to store metrics for a subject
        private static class SubjectMetrics {
            Subject subject;
            int totalStudyMinutes;
            double averageConfidence;
            double quizPerformance;
            long daysSinceLastStudy;
            Document lowestConfidenceDocument;
            double lowestConfidenceValue;
        }
    }