package ro.iss.proiect.repository;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import ro.iss.proiect.model.User;

import java.util.Optional;

public class UserRepository {

    // Metoda pentru salvarea unui utilizator (Create din CRUD)
    public void save(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(user);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    // Metoda pentru a verifica daca un email exista deja (folosita la Register)
    public boolean existsEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "select count(u) from User u where u.email = :email", Long.class);
            query.setParameter("email", email);
            return query.uniqueResult() > 0;
        }
    }

    // Metoda pentru a gasi un utilizator dupa email (folosita la Login)
    public Optional<User> getUserByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                    "from User where email = :email", User.class);
            query.setParameter("email", email);
            return query.uniqueResultOptional();
        }
    }
}