package com.mustapha.revisia.dao;

import com.mustapha.revisia.models.Subject;
import com.mustapha.revisia.models.TimeSlot;
import com.mustapha.revisia.models.User;
import com.mustapha.revisia.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.time.DayOfWeek;
import java.util.List;

public class TimeSlotDAOImpl implements TimeSlotDAO {

    @Override
    public void saveTimeSlot(TimeSlot timeSlot) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(timeSlot);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    @Override
    public TimeSlot getTimeSlotById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(TimeSlot.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<TimeSlot> getTimeSlotsByUser(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<TimeSlot> query = session.createQuery(
                    "FROM TimeSlot WHERE user.id = :userId ORDER BY day, startTime", TimeSlot.class);
            query.setParameter("userId", user.getId());
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<TimeSlot> getTimeSlotsByUserAndDay(User user, DayOfWeek day) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<TimeSlot> query = session.createQuery(
                    "FROM TimeSlot WHERE user.id = :userId AND day = :day ORDER BY startTime", TimeSlot.class);
            query.setParameter("userId", user.getId());
            query.setParameter("day", day);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<TimeSlot> getTimeSlotsBySubject(Subject subject) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<TimeSlot> query = session.createQuery(
                    "FROM TimeSlot WHERE subject.id = :subjectId ORDER BY day, startTime", TimeSlot.class);
            query.setParameter("subjectId", subject.getId());
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void updateTimeSlot(TimeSlot timeSlot) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(timeSlot);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTimeSlot(TimeSlot timeSlot) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(timeSlot);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
}