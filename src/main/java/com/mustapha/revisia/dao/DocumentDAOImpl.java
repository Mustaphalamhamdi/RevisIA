package com.mustapha.revisia.dao;

import com.mustapha.revisia.models.Document;
import com.mustapha.revisia.models.Subject;
import com.mustapha.revisia.models.User;
import com.mustapha.revisia.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;

public class DocumentDAOImpl implements DocumentDAO {

    @Override
    public void saveDocument(Document document) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(document);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    @Override
    public Document getDocumentById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Document.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Document> getDocumentsByUser(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Document> query = session.createQuery(
                    "FROM Document WHERE user.id = :userId ORDER BY uploadDate DESC", Document.class);
            query.setParameter("userId", user.getId());
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Document> getDocumentsBySubject(Subject subject) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Document> query = session.createQuery(
                    "FROM Document WHERE subject.id = :subjectId ORDER BY uploadDate DESC", Document.class);
            query.setParameter("subjectId", subject.getId());
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void updateDocument(Document document) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(document);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    @Override
    public void deleteDocument(Document document) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(document);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
}