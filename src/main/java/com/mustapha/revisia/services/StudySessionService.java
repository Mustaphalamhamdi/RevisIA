package com.mustapha.revisia.services;

import com.mustapha.revisia.models.Document;
import com.mustapha.revisia.models.StudySession;
import com.mustapha.revisia.models.Subject;
import com.mustapha.revisia.models.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface StudySessionService {
    void saveStudySession(StudySession studySession);
    void createStudySession(User user, Subject subject, Document document,
                            LocalDateTime startTime, LocalDateTime endTime,
                            Integer confidenceRating, String notes);
    StudySession getStudySessionById(Long id);
    List<StudySession> getStudySessionsByUser(User user);
    List<StudySession> getStudySessionsByUserAndSubject(User user, Subject subject);
    List<StudySession> getRecentStudySessionsByUser(User user, int limit);
    List<StudySession> getStudySessionsThisWeek(User user);
    int getTotalStudyMinutes(User user);
    int getWeeklyStudyMinutes(User user);
    Map<Subject, Integer> getSubjectStudyDistribution(User user);
    List<Subject> getTopStudiedSubjects(User user, int limit);
}