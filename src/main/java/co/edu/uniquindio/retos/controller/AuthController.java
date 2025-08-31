package co.edu.uniquindio.retos.controller;

import co.edu.uniquindio.retos.model.User;
import co.edu.uniquindio.retos.service.EmailService;
import co.edu.uniquindio.retos.service.TokenService;
import co.edu.uniquindio.retos.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final TokenService tokenService;
    private final BCryptPasswordEncoder encoder;
    private final EmailService emailService;

    public AuthController(UserService userService, TokenService tokenService, BCryptPasswordEncoder encoder, EmailService emailService) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.encoder = encoder;
        this.emailService = emailService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String,String> body) {
        String email = body.get("email");
        String password = body.get("password");
        return userService.findByEmail(email)
                .filter(u -> encoder.matches(password, u.getPassword()))
                .map(u -> Map.of(
                        "token", tokenService.generateToken(u),
                        "expiresIn", System.getenv("JWT_EXPIRATION_SECONDS") != null ? System.getenv("JWT_EXPIRATION_SECONDS") : "3600"
                ))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(401).body(Map.of("error","invalid_credentials")));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String,String> body) {
        String email = body.get("email");
        return userService.findByEmail(email)
                .map(user -> {
                    String token = userService.createResetTokenForUser(user, 2); // 2 hours
                    emailService.sendResetEmail(user.getEmail(), token);
                    // En ambiente dev devolvemos token en response para probar (production: no)
                    return ResponseEntity.ok(Map.of("message", "Email sent (dev)", "resetToken", token));
                })
                .orElse(ResponseEntity.status(404).body(Map.of("error","user_not_found")));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String,String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");
        return userService.findByResetToken(token)
                .map(user -> {
                    if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(OffsetDateTime.now())) {
                        return ResponseEntity.status(400).body(Map.of("error", "token_expired"));
                    }
                    user.setPassword(encoder.encode(newPassword));
                    userService.clearResetToken(user);
                    userService.save(user);
                    return ResponseEntity.ok(Map.of("message", "password_reset"));
                })
                .orElse(ResponseEntity.status(404).body(Map.of("error","invalid_token")));
    }
}
