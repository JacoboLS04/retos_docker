package co.edu.uniquindio.retos.controller;

import co.edu.uniquindio.retos.model.User;
import co.edu.uniquindio.retos.service.EmailService;
import co.edu.uniquindio.retos.service.TokenService;
import co.edu.uniquindio.retos.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final TokenService tokenService;
    private final BCryptPasswordEncoder encoder;
    private final EmailService emailService;

    public AuthController(UserService userService, TokenService tokenService,
                          BCryptPasswordEncoder encoder, EmailService emailService) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.encoder = encoder;
        this.emailService = emailService;
    }

    // ---------------------------
    // Login
    // ---------------------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        return userService.findByEmail(email)
                .filter(u -> encoder.matches(password, u.getPassword()))
                .map(u -> ResponseEntity.ok(Map.of(
                        "token", tokenService.generateToken(u),
                        "expiresIn", System.getenv("JWT_EXPIRATION_SECONDS") != null
                                ? System.getenv("JWT_EXPIRATION_SECONDS")
                                : "3600"
                )))
                .orElse(buildError(HttpStatus.UNAUTHORIZED, "invalid_credentials"));
    }

    // ---------------------------
    // Request password reset (antes: forgot-password)
    // ---------------------------
    @PostMapping("/request-password-reset")
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, String> body) {
        String email = body.get("email");

        return userService.findByEmail(email)
                .map(user -> {
                    String token = userService.createResetTokenForUser(user, 2); // 2 horas
                    emailService.sendResetEmail(user.getEmail(), token);

                    return ResponseEntity.ok(Map.of(
                            "message", "reset_email_sent",
                            "resetToken", token
                    ));
                })
                .orElseGet(() -> buildError(HttpStatus.NOT_FOUND, "user_not_found"));
    }


    // ---------------------------
    // Reset password
    // ---------------------------
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");

        return userService.findByResetToken(token)
                .map(user -> {
                    if (user.getResetTokenExpiry() == null ||
                            user.getResetTokenExpiry().isBefore(OffsetDateTime.now())) {
                        return buildError(HttpStatus.BAD_REQUEST, "token_expired");
                    }
                    user.setPassword(encoder.encode(newPassword));
                    userService.clearResetToken(user);
                    userService.save(user);

                    return ResponseEntity.ok(Map.of("message", "password_reset"));
                })
                .orElse(buildError(HttpStatus.BAD_REQUEST, "invalid_token"));
    }

    // ---------------------------
    // Helper para respuestas de error
    // ---------------------------
    private ResponseEntity<Map<String,String>> buildError(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "code", String.valueOf(status.value()),
                "message", message
        ));
    }
}
