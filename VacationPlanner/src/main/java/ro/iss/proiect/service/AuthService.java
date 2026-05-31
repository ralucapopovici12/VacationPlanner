package ro.iss.proiect.service;

import org.mindrot.jbcrypt.BCrypt;
import ro.iss.proiect.model.User;
import ro.iss.proiect.repository.UserRepository;

import java.util.Optional;

public class AuthService {
    private final UserRepository userRepository = new UserRepository();

    /**
     * Logica pentru inregistrarea unui utilizator nou.
     * Respecta fluxul din diagrama ta de secventa.
     */
    public boolean registerUser(String name, String email, String password) {
        // 1. Verificare existenta email (existsByEmail in diagrama ta)
        if (userRepository.existsEmail(email)) {
            System.out.println("Eroare: Email deja existent.");
            return false;
        }

        // 2. Hashing parola (Securitate ceruta in Iteratia 1)
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());

        // 3. Creare entitate si salvare (Forward Engineering prin Hibernate)
        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPassword(hashed);

        userRepository.save(newUser);
        return true;
    }

    /**
     * Logica pentru autentificare (Login).
     */
    public boolean authenticate(String email, String password) {
        Optional<User> userOpt = userRepository.getUserByEmail(email);

        if (userOpt.isPresent()) {
            // Verificam hash-ul parolei
            return BCrypt.checkpw(password, userOpt.get().getPassword());
        }
        return false;
    }
}