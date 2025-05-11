package com.mustapha.revisia.services;

import com.mustapha.revisia.models.Document;
import com.mustapha.revisia.models.Question;
import com.mustapha.revisia.models.Quiz;
import com.mustapha.revisia.models.Subject;
import com.mustapha.revisia.models.User;

import java.util.List;

public interface QuizService {
    void saveQuiz(Quiz quiz);
    Quiz getQuizById(Long id);
    List<Quiz> getQuizzesByUser(User user);
    List<Quiz> getQuizzesByUserAndSubject(User user, Subject subject);
    List<Quiz> getRecentQuizzesByUser(User user, int limit);
    Quiz createQuizFromSubject(User user, Subject subject, int questionCount);
    Quiz createQuizFromDocument(User user, Document document, int questionCount);
    void updateQuiz(Quiz quiz);
    void completeQuiz(Quiz quiz);
    void deleteQuiz(Quiz quiz);
}