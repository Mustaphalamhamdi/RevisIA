package com.mustapha.revisia.services;

import com.mustapha.revisia.models.Document;
import com.mustapha.revisia.models.Question;
import com.mustapha.revisia.models.Subject;
import com.mustapha.revisia.models.User;

import java.util.List;

public interface QuestionService {
    void saveQuestion(Question question);
    Question getQuestionById(Long id);
    List<Question> getQuestionsBySubject(Subject subject);
    List<Question> getQuestionsByDocument(Document document);
    List<Question> getQuestionsBySubjectAndDocument(Subject subject, Document document);
    List<Question> getRandomQuestionsBySubject(Subject subject, int limit);
    List<Question> getRandomQuestionsByDocument(Document document, int limit);
    List<Question> generateQuestionsFromDocument(Document document, int count);
    void updateQuestion(Question question);
    void deleteQuestion(Question question);
}