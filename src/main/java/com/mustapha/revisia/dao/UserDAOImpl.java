package com.mustapha.revisia.dao;

import com.mustapha.revisia.models.User;
import com.mustapha.revisia.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class UserDAOImpl implements UserDAO {

    @Override
    public void saveUser(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(user);
            transaction.commit();
            System.out.println("User saved successfully with ID: " + user.getId()); // Add logging
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            System.err.println("Error saving user: " + e.getMessage()); // Add logging
            e.printStackTrace();
        }
    }

    @Override
    public User getUserById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(User.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public User getUserByUsername(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            System.out.println("Searching for user: " + username);

            String hql = "FROM User WHERE username = :username";
            Query<User> query = session.createQuery(hql, User.class);
            query.setParameter("username", username);

            try {
                User user = query.getSingleResult();
                if (user != null) {
                    System.out.println("User found: " + user.getUsername());
                    return user;
                } else {
                    System.out.println("No user found for username: " + username);
                    return null;
                }
            } catch (Exception e) {
                System.out.println("Error finding user: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        } catch (Exception e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<User> getAllUsers() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM User", User.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void updateUser(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(user);  // Changed from update to merge
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    @Override
    public void deleteUser(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(user);  // Changed from delete to remove
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    @Override
    public boolean validateLogin(String username, String password) {
        System.out.println("Attempting to validate login for: " + username);
        User user = getUserByUsername(username);
        if (user != null) {
            System.out.println("User found. Stored password: " + user.getPasswordHash());
            System.out.println("Entered password: " + password);
            boolean matches = password.equals(user.getPasswordHash());
            System.out.println("Passwords match: " + matches);
            return matches;
        }
        System.out.println("User not found");
        return false;
    }
}