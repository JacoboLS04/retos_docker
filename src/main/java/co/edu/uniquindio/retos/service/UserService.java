package co.edu.uniquindio.retos.service;

import co.edu.uniquindio.retos.model.User;
import co.edu.uniquindio.retos.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository repo;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository repo, BCryptPasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(String nombre, String email, String rawPassword) {
        User u = new User();
        u.setNombre(nombre);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(rawPassword));
        return repo.save(u);
    }

    public Optional<User> findByEmail(String email) {
        return repo.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return repo.findById(id);
    }

    public User save(User user) { return repo.save(user); }

    public Optional<User> findByResetToken(String token) {
        return repo.findByResetToken(token);
    }

    // generate reset token and expiry
    public String createResetTokenForUser(User user, long hoursValid) {
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(OffsetDateTime.now().plusHours(hoursValid));
        repo.save(user);
        return token;
    }

    public void clearResetToken(User user) {
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        repo.save(user);
    }
}
