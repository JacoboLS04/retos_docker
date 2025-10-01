package co.edu.uniquindio.retos.service;

import co.edu.uniquindio.retos.model.User;
import co.edu.uniquindio.retos.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EventPublisher eventPublisher;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, EventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    // ---------------------------
    // Registro
    // ---------------------------
    public User register(String nombre, String email, String rawPassword, String telefono) {
        User u = new User();
        u.setNombre(nombre);
        u.setEmail(email);
        u.setTelefono(telefono);
        u.setPassword(passwordEncoder.encode(rawPassword));

        User saved = userRepository.save(u);

        // 🔹 Publicar evento DESPUÉS de guardar
        eventPublisher.publishUserRegisteredEvent(saved.getId(), saved.getEmail(), saved.getNombre(), saved.getTelefono());

        return saved;
    }

    // ---------------------------
    // Buscar por email
    // ---------------------------
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // ---------------------------
    // Login
    // ---------------------------
    public void notifyLogin(User user) {
        eventPublisher.publishUserLoginEvent(user.getId(), user.getEmail(), user.getTelefono());
    }

    // ---------------------------
    // Buscar por ID
    // ---------------------------
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    // ---------------------------
    // Listar con paginación
    // ---------------------------
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    // ---------------------------
    // Guardar cambios
    // ---------------------------
    public User save(User user) {
        return userRepository.save(user);
    }

    // ---------------------------
    // Eliminar usuario
    // ---------------------------
    public void delete(User user) {
        userRepository.delete(user);
    }

    // ---------------------------
    // Buscar por token de reset
    // ---------------------------
    public Optional<User> findByResetToken(String token) {
        return userRepository.findByResetToken(token);
    }

    // ---------------------------
    // Crear token de recuperación
    // ---------------------------
    public String createResetTokenForUser(User user, long hoursValid) {
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(OffsetDateTime.now().plusHours(hoursValid));
        userRepository.save(user);

        // 🔹 Publicar evento
        eventPublisher.publishPasswordResetRequestedEvent(user.getId(), user.getEmail(), token, user.getTelefono());

        return token;
    }

    // ---------------------------
    // Limpiar token de recuperación
    // ---------------------------
    public void clearResetToken(User user) {
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }

    // ---------------------------
    // Cambio de contraseña
    // ---------------------------
    public void changePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        // 🔹 Publicar evento
        eventPublisher.publishPasswordChangedEvent(user.getId(), user.getEmail(), user.getTelefono());
    }
}
