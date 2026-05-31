package ro.iss.proiect.repository;

import org.hibernate.Session;
import org.hibernate.query.Query;
import ro.iss.proiect.model.VacationPackage;

import java.util.List;
import java.util.Optional;

public class PackageRepository {

    public List<VacationPackage> findByType(String type) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<VacationPackage> query = session.createQuery("from VacationPackage where type = :type", VacationPackage.class);
            query.setParameter("type", type);
            return query.list();
        }
    }

    public Optional<VacationPackage> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(VacationPackage.class, id));
        }
    }
}
