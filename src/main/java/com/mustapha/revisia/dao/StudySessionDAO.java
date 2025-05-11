package com.mustapha.revisia.dao;

import com.mustapha.revisia.models.StudySession;
import com.mustapha.revisia.models.Subject;
import com.mustapha.revisia.models.User;

import java.time.LocalDateTime;
import java.util.List;

public interface StudySessionDAO {
    void saveStudySession(StudySession studySession);
    StudySession getStudySessionById(Long id);
    List<StudySession> getStudySessionsByUser(User user);
    List<StudySession> getStudySessionsByUserAndSubject(User user, Subject subject);
    List<StudySession> getRecentStudySessionsByUser(User user, int limit);
    List<StudySession> getStudySessionsByUserBetweenDates(User user, LocalDateTime startDate, LocalDateTime endDate);
    void updateStudySession(StudySession studySession);
    void deleteStudySession(StudySession studySession);
    int getTotalStudyMinutesByUser(User user);
    int getStudyMinutesByUserBetweenDates(User user, LocalDateTime startDate, LocalDateTime endDate);
}