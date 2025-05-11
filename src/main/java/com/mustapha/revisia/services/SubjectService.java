package com.mustapha.revisia.services;

import com.mustapha.revisia.models.Subject;
import com.mustapha.revisia.models.User;
import java.util.List;

public interface SubjectService {
    void saveSubject(Subject subject);
    void createSubject(String name, String professorName, User user);
    Subject getSubjectById(Long id);
    List<Subject> getSubjectsByUser(User user);
    void updateSubject(Subject subject);
    void deleteSubject(Subject subject);
}