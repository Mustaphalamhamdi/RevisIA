package com.mustapha.revisia.dao;

import com.mustapha.revisia.models.Document;
import com.mustapha.revisia.models.Quiz;
import com.mustapha.revisia.models.Subject;
import com.mustapha.revisia.models.User;
import com.mustapha.revisia.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class QuizDAOImpl implements QuizDAO {

    @Override
    public void saveQuiz(Quiz quiz) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(quiz);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    @Override
    public Quiz getQuizById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Quiz.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Quiz> getQuizzesByUser(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Quiz> query = session.createQuery(
                    "FROM Quiz WHERE user = :user ORDER BY createdAt DESC",
                    Quiz.class);
            query.setParameter("user", user);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Quiz> getQuizzesByUserAndSubject(User user, Subject subject) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Quiz> query = session.createQuery(
                    "FROM Quiz WHERE user = :user AND subject = :subject ORDER BY createdAt DESC",
                    Quiz.class);
            query.setParameter("user", user);
            query.setParameter("subject", subject);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Quiz> getRecentQuizzesByUser(User user, int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Quiz> query = session.createQuery(
                    "FROM Quiz WHERE user = :user ORDER BY createdAt DESC",
                    Quiz.class);
            query.setParameter("user", user);
            query.setMaxResults(limit);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void updateQuiz(Quiz quiz) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(quiz);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    @Override
    public void deleteQuiz(Quiz quiz) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(quiz);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
}