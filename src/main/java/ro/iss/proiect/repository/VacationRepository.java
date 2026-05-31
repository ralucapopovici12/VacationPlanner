package ro.iss.proiect.repository;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import ro.iss.proiect.model.Vacation;
import ro.iss.proiect.model.VacationActivity;

import java.util.List;
import java.util.Optional;

public class VacationRepository {

    public Vacation save(Vacation vacation) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(vacation);
            transaction.commit();
            return vacation;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return null;
        }
    }

    public void update(Vacation vacation) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(vacation);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public Optional<Vacation> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(Vacation.class, id));
        }
    }

    public List<Vacation> findByUserId(Long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Vacation> query = session.createQuery("from Vacation where user.id = :uid", Vacation.class);
            query.setParameter("uid", userId);
            return query.list();
        }
    }
    
    public void saveActivity(VacationActivity activity) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(activity);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }
    
    public List<VacationActivity> findActivitiesByVacationId(Long vacationId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<VacationActivity> query = session.createQuery("from VacationActivity where vacation.id = :vid", VacationActivity.class);
            query.setParameter("vid", vacationId);
            return query.list();
        }
    }

    public void delete(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            // Delete associated activities first
            session.createMutationQuery("delete from VacationActivity where vacation.id = :vid")
                   .setParameter("vid", id)
                   .executeUpdate();
            
            // Delete the vacation
            session.createMutationQuery("delete from Vacation where id = :id")
                   .setParameter("id", id)
                   .executeUpdate();
                   
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }
}
