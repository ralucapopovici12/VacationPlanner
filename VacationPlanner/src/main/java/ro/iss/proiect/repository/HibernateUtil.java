package ro.iss.proiect.repository;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import ro.iss.proiect.model.User;

public class HibernateUtil {
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            // Creeaza SessionFactory din hibernate.cfg.xml
            return new Configuration()
                    .configure()
                    .addAnnotatedClass(User.class)
                    .addAnnotatedClass(ro.iss.proiect.model.VacationPackage.class)
                    .addAnnotatedClass(ro.iss.proiect.model.ActivitySuggestion.class)
                    .addAnnotatedClass(ro.iss.proiect.model.Vacation.class)
                    .addAnnotatedClass(ro.iss.proiect.model.VacationActivity.class)
                    .buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        getSessionFactory().close();
    }
}