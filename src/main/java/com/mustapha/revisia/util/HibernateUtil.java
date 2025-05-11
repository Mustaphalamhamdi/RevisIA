package com.mustapha.revisia.util;

import com.mustapha.revisia.models.*;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

public class HibernateUtil {
    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                Configuration configuration = new Configuration();

                // Configure database connection from properties file
                configuration.configure();

                // Add annotated classes
                configuration.addAnnotatedClass(User.class);
                configuration.addAnnotatedClass(Subject.class);
                configuration.addAnnotatedClass(TimeSlot.class);
                configuration.addAnnotatedClass(Document.class);
                configuration.addAnnotatedClass(StudySession.class); // Add this line
                configuration.addAnnotatedClass(Question.class); // Add this line
                configuration.addAnnotatedClass(Quiz.class); // Add this line

                ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                        .applySettings(configuration.getProperties())
                        .build();

                sessionFactory = configuration.buildSessionFactory(serviceRegistry);
                System.out.println("Hibernate initialized with Document entity");
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error initializing Hibernate: " + e.getMessage());
            }
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}