package com.mustapha.revisia.services;

import com.mustapha.revisia.dao.SubjectDAO;
import com.mustapha.revisia.dao.SubjectDAOImpl;
import com.mustapha.revisia.models.Subject;
import com.mustapha.revisia.models.User;
import java.util.List;

public class SubjectServiceImpl implements SubjectService {

    private final SubjectDAO subjectDAO = new SubjectDAOImpl();

    @Override
    public void saveSubject(Subject subject) {
        subjectDAO.saveSubject(subject);
    }

    @Override
    public void createSubject(String name, String professorName, User user) {
        Subject subject = new Subject(name, professorName, user);
        saveSubject(subject);
    }

    @Override
    public Subject getSubjectById(Long id) {
        return subjectDAO.getSubjectById(id);
    }

    @Override
    public List<Subject> getSubjectsByUser(User user) {
        return subjectDAO.getSubjectsByUser(user);
    }

    @Override
    public void updateSubject(Subject subject) {
        subjectDAO.updateSubject(subject);
    }

    @Override
    public void deleteSubject(Subject subject) {
        subjectDAO.deleteSubject(subject);
    }
}