package com.mustapha.revisia.services;

import com.mustapha.revisia.dao.StudySessionDAO;
import com.mustapha.revisia.dao.StudySessionDAOImpl;
import com.mustapha.revisia.models.Document;
import com.mustapha.revisia.models.StudySession;
import com.mustapha.revisia.models.Subject;
import com.mustapha.revisia.models.User;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

public class StudySessionServiceImpl implements StudySessionService {

    private final StudySessionDAO studySessionDAO = new StudySessionDAOImpl();

    @Override
    public void saveStudySession(StudySession studySession) {
        studySessionDAO.saveStudySession(studySession);
    }

    @Override
    public void createStudySession(User user, Subject subject, Document document,
                                   LocalDateTime startTime, LocalDateTime endTime,
                                   Integer confidenceRating, String notes) {
        StudySession studySession = new StudySession(user, subject, document,
                startTime, endTime,
                confidenceRating, notes);
        saveStudySession(studySession);
    }

    @Override
    public StudySession getStudySessionById(Long id) {
        return studySessionDAO.getStudySessionById(id);
    }

    @Override
    public List<StudySession> getStudySessionsByUser(User user) {
        return studySessionDAO.getStudySessionsByUser(user);
    }

    @Override
    public List<StudySession> getStudySessionsByUserAndSubject(User user, Subject subject) {
        return studySessionDAO.getStudySessionsByUserAndSubject(user, subject);
    }

    @Override
    public List<StudySession> getRecentStudySessionsByUser(User user, int limit) {
        return studySessionDAO.getRecentStudySessionsByUser(user, limit);
    }

    @Override
    public List<StudySession> getStudySessionsThisWeek(User user) {
        // Get start and end of current week (Monday to Sunday)
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        LocalDateTime startDateTime = startOfWeek.atStartOfDay();
        LocalDateTime endDateTime = endOfWeek.plusDays(1).atStartOfDay().minusSeconds(1);

        return studySessionDAO.getStudySessionsByUserBetweenDates(user, startDateTime, endDateTime);
    }

    @Override
    public int getTotalStudyMinutes(User user) {
        return studySessionDAO.getTotalStudyMinutesByUser(user);
    }

    @Override
    public int getWeeklyStudyMinutes(User user) {
        // Get start and end of current week (Monday to Sunday)
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        LocalDateTime startDateTime = startOfWeek.atStartOfDay();
        LocalDateTime endDateTime = endOfWeek.plusDays(1).atStartOfDay().minusSeconds(1);

        return studySessionDAO.getStudyMinutesByUserBetweenDates(user, startDateTime, endDateTime);
    }

    @Override
    public Map<Subject, Integer> getSubjectStudyDistribution(User user) {
        List<StudySession> allSessions = getStudySessionsByUser(user);
        if (allSessions == null) return new HashMap<>();

        // Group sessions by subject and sum up minutes
        Map<Subject, Integer> distribution = new HashMap<>();

        for (StudySession session : allSessions) {
            Subject subject = session.getSubject();
            distribution.put(subject,
                    distribution.getOrDefault(subject, 0) + session.getDurationMinutes());
        }

        return distribution;
    }

    @Override
    public List<Subject> getTopStudiedSubjects(User user, int limit) {
        Map<Subject, Integer> distribution = getSubjectStudyDistribution(user);

        // Sort subjects by study time (descending)
        return distribution.entrySet().stream()
                .sorted(Map.Entry.<Subject, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}