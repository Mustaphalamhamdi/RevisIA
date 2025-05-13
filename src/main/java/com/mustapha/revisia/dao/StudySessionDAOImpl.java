package com.mustapha.revisia.dao;

import com.mustapha.revisia.models.StudySession;
import com.mustapha.revisia.models.Subject;
import com.mustapha.revisia.models.User;
import com.mustapha.revisia.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
import java.util.List;

public class StudySessionDAOImpl implements StudySessionDAO {

    @Override
    public void saveStudySession(StudySession studySession) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(studySession);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    @Override
    public StudySession getStudySessionById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(StudySession.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<StudySession> getStudySessionsByUser(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<StudySession> query = session.createQuery(
                    "FROM StudySession WHERE user = :user ORDER BY startTime DESC",
                    StudySession.class);
            query.setParameter("user", user);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<StudySession> getStudySessionsByUserAndSubject(User user, Subject subject) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<StudySession> query = session.createQuery(
                    "FROM StudySession WHERE user = :user AND subject = :subject ORDER BY startTime DESC",
                    StudySession.class);
            query.setParameter("user", user);
            query.setParameter("subject", subject);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<StudySession> getRecentStudySessionsByUser(User user, int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<StudySession> query = session.createQuery(
                    "FROM StudySession WHERE user = :user ORDER BY startTime DESC",
                    StudySession.class);
            query.setParameter("user", user);
            query.setMaxResults(limit);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<StudySession> getStudySessionsByUserBetweenDates(User user, LocalDateTime startDate, LocalDateTime endDate) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<StudySession> query = session.createQuery(
                    "FROM StudySession WHERE user = :user AND startTime BETWEEN :startDate AND :endDate ORDER BY startTime DESC",
                    StudySession.class);
            query.setParameter("user", user);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void updateStudySession(StudySession studySession) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(studySession);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    @Override
    public void deleteStudySession(StudySession studySession) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(studySession);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    @Override
    public int getTotalStudyMinutesByUser(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT SUM(durationMinutes) FROM StudySession WHERE user = :user",
                    Long.class);
            query.setParameter("user", user);
            Long result = query.getSingleResult();
            return result != null ? result.intValue() : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int getStudyMinutesByUserBetweenDates(User user, LocalDateTime startDate, LocalDateTime endDate) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT SUM(durationMinutes) FROM StudySession WHERE user = :user AND startTime BETWEEN :startDate AND :endDate",
                    Long.class);
            query.setParameter("user", user);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            Long result = query.getSingleResult();
            return result != null ? result.intValue() : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}