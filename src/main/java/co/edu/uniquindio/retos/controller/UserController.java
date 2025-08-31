package co.edu.uniquindio.retos.controller;

import co.edu.uniquindio.retos.model.User;
import co.edu.uniquindio.retos.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    public UserController(UserService userService) { this.userService = userService; }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String,String> body) {
        String nombre = body.get("nombre");
        String email = body.get("email");
        String password = body.get("password");
        if (nombre==null||email==null||password==null) {
            return ResponseEntity.badRequest().body(Map.of("error", "missing_fields"));
        }
        if (userService.findByEmail(email).isPresent()) {
            return ResponseEntity.status(409).body(Map.of("error", "email_exists"));
        }
        User u = userService.register(nombre, email, password);
        // don't return password
        u.setPassword(null);
        return ResponseEntity.status(201).body(u);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        return userService.findById(id)
                .map(u -> { u.setPassword(null); return ResponseEntity.ok(u); })
                .orElse(ResponseEntity.notFound().build());
    }

    // Implement update/delete as needed (omitted for brevity)
}
