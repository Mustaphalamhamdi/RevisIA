package com.mustapha.revisia.dao;

import com.mustapha.revisia.models.Subject;
import com.mustapha.revisia.models.User;
import java.util.List;

public interface SubjectDAO {
    void saveSubject(Subject subject);
    Subject getSubjectById(Long id);
    List<Subject> getSubjectsByUser(User user);
    void updateSubject(Subject subject);
    void deleteSubject(Subject subject);
}