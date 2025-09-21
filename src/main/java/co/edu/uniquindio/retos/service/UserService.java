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

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ---------------------------
    // Registro
    // ---------------------------
    public User register(String nombre, String email, String rawPassword) {
        User u = new User();
        u.setNombre(nombre);
        u.setEmail(email);
        // Siempre encriptar la contraseña
        u.setPassword(passwordEncoder.encode(rawPassword));
        return userRepository.save(u);
    }

    // ---------------------------
    // Buscar por email
    // ---------------------------
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
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
}
