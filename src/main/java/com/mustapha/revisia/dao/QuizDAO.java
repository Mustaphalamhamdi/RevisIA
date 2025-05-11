package com.mustapha.revisia.dao;

import com.mustapha.revisia.models.Document;
import com.mustapha.revisia.models.Quiz;
import com.mustapha.revisia.models.Subject;
import com.mustapha.revisia.models.User;

import java.util.List;

public interface QuizDAO {
    void saveQuiz(Quiz quiz);
    Quiz getQuizById(Long id);
    List<Quiz> getQuizzesByUser(User user);
    List<Quiz> getQuizzesByUserAndSubject(User user, Subject subject);
    List<Quiz> getRecentQuizzesByUser(User user, int limit);
    void updateQuiz(Quiz quiz);
    void deleteQuiz(Quiz quiz);
}