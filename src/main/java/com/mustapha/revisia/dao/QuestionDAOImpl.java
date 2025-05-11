package com.mustapha.revisia.dao;

import com.mustapha.revisia.models.Document;
import com.mustapha.revisia.models.Question;
import com.mustapha.revisia.models.Subject;
import com.mustapha.revisia.models.User;
import com.mustapha.revisia.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class QuestionDAOImpl implements QuestionDAO {

    @Override
    public void saveQuestion(Question question) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(question);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    @Override
    public Question getQuestionById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Question.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Question> getQuestionsBySubject(Subject subject) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Question> query = session.createQuery(
                    "FROM Question WHERE subject = :subject",
                    Question.class);
            query.setParameter("subject", subject);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Question> getQuestionsByDocument(Document document) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Question> query = session.createQuery(
                    "FROM Question WHERE document = :document",
                    Question.class);
            query.setParameter("document", document);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Question> getQuestionsBySubjectAndDocument(Subject subject, Document document) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Question> query = session.createQuery(
                    "FROM Question WHERE subject = :subject AND document = :document",
                    Question.class);
            query.setParameter("subject", subject);
            query.setParameter("document", document);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Question> getRandomQuestionsBySubject(Subject subject, int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Use native SQL for random selection
            Query<Question> query = session.createQuery(
                    "FROM Question WHERE subject = :subject ORDER BY RAND()",
                    Question.class);
            query.setParameter("subject", subject);
            query.setMaxResults(limit);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Question> getRandomQuestionsByDocument(Document document, int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Use native SQL for random selection
            Query<Question> query = session.createQuery(
                    "FROM Question WHERE document = :document ORDER BY RAND()",
                    Question.class);
            query.setParameter("document", document);
            query.setMaxResults(limit);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void updateQuestion(Question question) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(question);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    @Override
    public void deleteQuestion(Question question) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(question);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
}