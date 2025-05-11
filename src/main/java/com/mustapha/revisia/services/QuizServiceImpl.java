package com.mustapha.revisia.services;

import com.mustapha.revisia.dao.QuizDAO;
import com.mustapha.revisia.dao.QuizDAOImpl;
import com.mustapha.revisia.models.Document;
import com.mustapha.revisia.models.Question;
import com.mustapha.revisia.models.Quiz;
import com.mustapha.revisia.models.Subject;
import com.mustapha.revisia.models.User;

import java.util.List;

public class QuizServiceImpl implements QuizService {

    private final QuizDAO quizDAO = new QuizDAOImpl();
    private final QuestionService questionService = new QuestionServiceImpl();

    @Override
    public void saveQuiz(Quiz quiz) {
        quizDAO.saveQuiz(quiz);
    }

    @Override
    public Quiz getQuizById(Long id) {
        return quizDAO.getQuizById(id);
    }

    @Override
    public List<Quiz> getQuizzesByUser(User user) {
        return quizDAO.getQuizzesByUser(user);
    }

    @Override
    public List<Quiz> getQuizzesByUserAndSubject(User user, Subject subject) {
        return quizDAO.getQuizzesByUserAndSubject(user, subject);
    }

    @Override
    public List<Quiz> getRecentQuizzesByUser(User user, int limit) {
        return quizDAO.getRecentQuizzesByUser(user, limit);
    }

    @Override
    public Quiz createQuizFromSubject(User user, Subject subject, int questionCount) {
        // Get existing questions for this subject
        List<Question> existingQuestions = questionService.getQuestionsBySubject(subject);

        // If we have enough existing questions, use them
        if (existingQuestions != null && existingQuestions.size() >= questionCount) {
            List<Question> randomQuestions = questionService.getRandomQuestionsBySubject(subject, questionCount);
            Quiz quiz = new Quiz(user, subject, null, "Quiz de " + subject.getName(), randomQuestions);
            quizDAO.saveQuiz(quiz);
            return quiz;
        }

        // Otherwise, try to generate questions from documents in this subject
        List<Document> documents = subject.getDocuments();
        if (documents != null && !documents.isEmpty()) {
            // Choose the first document for simplicity
            Document document = documents.get(0);

            // Generate questions
            List<Question> generatedQuestions = questionService.generateQuestionsFromDocument(document, questionCount);

            // Create and save quiz
            Quiz quiz = new Quiz(user, subject, document, "Quiz de " + subject.getName(), generatedQuestions);
            quizDAO.saveQuiz(quiz);
            return quiz;
        }

        return null; // Couldn't create quiz
    }

    @Override
    public Quiz createQuizFromDocument(User user, Document document, int questionCount) {
        // First check if we already have questions for this document
        List<Question> existingQuestions = questionService.getQuestionsByDocument(document);

        if (existingQuestions != null && existingQuestions.size() >= questionCount) {
            // Use existing questions
            List<Question> randomQuestions = questionService.getRandomQuestionsByDocument(document, questionCount);
            Quiz quiz = new Quiz(user, document.getSubject(), document,
                    "Quiz sur " + document.getTitle(), randomQuestions);
            quizDAO.saveQuiz(quiz);
            return quiz;
        }

        // Generate new questions
        List<Question> generatedQuestions = questionService.generateQuestionsFromDocument(document, questionCount);

        // Create and save quiz
        Quiz quiz = new Quiz(user, document.getSubject(), document,
                "Quiz sur " + document.getTitle(), generatedQuestions);
        quizDAO.saveQuiz(quiz);
        return quiz;
    }

    @Override
    public void updateQuiz(Quiz quiz) {
        quizDAO.updateQuiz(quiz);
    }

    @Override
    public void completeQuiz(Quiz quiz) {
        quiz.complete();
        quizDAO.updateQuiz(quiz);
    }

    @Override
    public void deleteQuiz(Quiz quiz) {
        quizDAO.deleteQuiz(quiz);
    }
}