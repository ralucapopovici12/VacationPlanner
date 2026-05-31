package ro.iss.proiect.service;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import ro.iss.proiect.model.ActivitySuggestion;
import ro.iss.proiect.model.VacationPackage;
import ro.iss.proiect.repository.HibernateUtil;

public class DataSeeder {
    
    public void seedData() {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Stergem absolut tot ca sa recream baza cu design-ul nou
            session.createMutationQuery("delete from VacationActivity").executeUpdate();
            session.createMutationQuery("delete from Vacation").executeUpdate();
            session.createMutationQuery("delete from VacationPackage").executeUpdate();
            session.createMutationQuery("delete from ActivitySuggestion").executeUpdate();

            // Seed Packages
            System.out.println("Seeding database with vacation packages and activities...");
                
                session.persist(VacationPackage.builder().type("Mountain").bundleName("CABIN ONLY").destination("The Alps, Austria").price(2200.0)
                        .description("Discover the pristine winter landscape with a wooden forest cabin. Features a private jacuzzi.")
                        .imageUrl("https://images.unsplash.com/photo-1449844908441-8829872d2607?w=800&auto=format&fit=crop").build());
                        
                session.persist(VacationPackage.builder().type("Sea").bundleName("HOTEL + FLIGHT").destination("Maldives Atolls").price(4500.0)
                        .description("Disconnect in an overwater villa luxury escape. Private pool and pristine beaches await.")
                        .imageUrl("https://images.unsplash.com/photo-1573843981267-be1999ff37cd?w=800&q=80").build()); // Fixed Maldives URL
                        
                session.persist(VacationPackage.builder().type("Mountain").bundleName("FLIGHT + HOTEL").destination("Dolomites, Italy").price(850.0)
                        .description("Skiing adventure with 5-star hotel accommodation in the heart of the Dolomites.")
                        .imageUrl("https://images.unsplash.com/photo-1518779578993-ec3579fee39f?q=80&w=800&auto=format&fit=crop").build());
                        
                session.persist(VacationPackage.builder().type("Sea").bundleName("FLIGHT + HOTEL").destination("Nice, France").price(1200.0)
                        .description("Sunny beach vacation on the French Riviera with all-inclusive services.")
                        .imageUrl("https://images.unsplash.com/photo-1533676802871-eca1ae998cd5?q=80&w=800&auto=format&fit=crop").build());
                        
                session.persist(VacationPackage.builder().type("Business").bundleName("TRAIN + HOTEL").destination("Vienna, Austria").price(600.0)
                        .description("Comfortable business trip package near the central convention center.")
                        .imageUrl("https://images.unsplash.com/photo-1516550893923-42d28e5677af?q=80&w=800&auto=format&fit=crop").build());
                        
                session.persist(VacationPackage.builder().type("Romantic").bundleName("FLIGHT + HOTEL").destination("Paris, France").price(1500.0)
                        .description("The ultimate romantic getaway. Includes dinner at the Eiffel Tower.")
                        .imageUrl("https://images.unsplash.com/photo-1502602898657-3e91760cbb34?q=80&w=800&auto=format&fit=crop").build());
                
                // Seed Activity Suggestions cu imagini
                session.persist(ActivitySuggestion.builder().type("Mountain").title("Skiing Pass").description("Full day skiing access").price(120.0)
                        .imageUrl("https://images.unsplash.com/photo-1551524559-8af4e6624178?q=80&w=800&auto=format&fit=crop").build());
                session.persist(ActivitySuggestion.builder().type("Mountain").title("Hiking Guide").description("Guided mountain tour").price(50.0)
                        .imageUrl("https://images.unsplash.com/photo-1551632811-561732d1e306?q=80&w=800&auto=format&fit=crop").build());
                session.persist(ActivitySuggestion.builder().type("Sea").title("Snorkeling Adventure").description("Explore the vibrant coral reefs of your tropical atoll.").price(150.0)
                        .imageUrl("https://images.unsplash.com/photo-1544551763-46a013bb70d5?q=80&w=800&auto=format&fit=crop").build());
                session.persist(ActivitySuggestion.builder().type("Sea").title("Boat Tour").description("Sunset cruise on a private yacht.").price(80.0)
                        .imageUrl("https://images.unsplash.com/photo-1569263979104-865ab7cd8d13?w=800&auto=format&fit=crop").build());
                session.persist(ActivitySuggestion.builder().type("Business").title("Conference Pass").description("Entry to main tech conference").price(300.0)
                        .imageUrl("https://images.unsplash.com/photo-1540317580384-e5d43616b9aa?q=80&w=800&auto=format&fit=crop").build());
                session.persist(ActivitySuggestion.builder().type("Romantic").title("Local Wine Tasting").description("Sample authentic local wines in a volcanic soil cellar.").price(180.0)
                        .imageUrl("https://images.unsplash.com/photo-1510812431401-41d2bd2722f3?q=80&w=800&auto=format&fit=crop").build());

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }
}
