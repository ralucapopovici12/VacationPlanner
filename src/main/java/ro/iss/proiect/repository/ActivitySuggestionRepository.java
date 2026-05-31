package ro.iss.proiect.repository;

import org.hibernate.Session;
import org.hibernate.query.Query;
import ro.iss.proiect.model.ActivitySuggestion;

import java.util.List;

public class ActivitySuggestionRepository {

    public List<ActivitySuggestion> findByType(String type) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<ActivitySuggestion> query = session.createQuery("from ActivitySuggestion where type = :type", ActivitySuggestion.class);
            query.setParameter("type", type);
            return query.list();
        }
    }
}
