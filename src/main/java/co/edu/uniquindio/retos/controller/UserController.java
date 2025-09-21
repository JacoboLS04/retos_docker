package co.edu.uniquindio.retos.controller;

import co.edu.uniquindio.retos.model.User;
import co.edu.uniquindio.retos.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Crear un nuevo usuario
     * Método: POST /api/usuarios
     */
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> body) {
        String nombre = body.get("nombre");
        String email = body.get("email");
        String password = body.get("password");

        if (nombre == null || email == null || password == null) {
            return buildError(HttpStatus.BAD_REQUEST, "missing_fields");
        }

        if (userService.findByEmail(email).isPresent()) {
            return buildError(HttpStatus.CONFLICT, "email_exists");
        }

        User u = userService.register(nombre, email, password);
        u.setPassword(null); // nunca devolver la contraseña

        return ResponseEntity.status(HttpStatus.CREATED).body(u);
    }

    /**
     * Listar usuarios con paginación
     * Método: GET /api/usuarios?page=0&size=10
     */
    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal Jwt jwt) {

        if (jwt == null) {
            return buildError(HttpStatus.UNAUTHORIZED, "unauthorized");
        }

        Page<User> usersPage = userService.findAll(PageRequest.of(page, size));

        Map<String, Object> response = new HashMap<>();
        response.put("page", usersPage.getNumber());
        response.put("size", usersPage.getSize());
        response.put("totalElements", usersPage.getTotalElements());
        response.put("totalPages", usersPage.getTotalPages());
        response.put("data", usersPage.getContent());

        return ResponseEntity.ok(response);
    }

    /**
     * Obtener un usuario por ID
     * Método: GET /api/usuarios/{id}
     */
    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getUserById(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        if (jwt == null) {
            return buildError(HttpStatus.UNAUTHORIZED, "unauthorized");
        }

        return userService.findById(id)
                .<ResponseEntity<?>>map(u -> {
                    u.setPassword(null);
                    return ResponseEntity.ok(u); // <- ahora ResponseEntity<?>
                })
                .orElseGet(() -> buildError(HttpStatus.NOT_FOUND, "user_not_found"));
    }


    /**
     * Actualizar un usuario
     * Método: PUT /api/usuarios/{id}
     */
    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal Jwt jwt) {

        if (jwt == null) {
            return buildError(HttpStatus.UNAUTHORIZED, "unauthorized");
        }

        Long userIdFromToken = Long.parseLong(jwt.getSubject());

        return userService.findById(id)
                .map(u -> {
                    if (!u.getId().equals(userIdFromToken)) {
                        return buildError(HttpStatus.FORBIDDEN, "forbidden");
                    }

                    String nombre = body.get("nombre");
                    String email = body.get("email");
                    if (nombre != null) u.setNombre(nombre);
                    if (email != null) u.setEmail(email);

                    userService.save(u);
                    u.setPassword(null);

                    return ResponseEntity.ok(u);
                })
                .orElseGet(() -> buildError(HttpStatus.NOT_FOUND, "user_not_found"));
    }

    /**
     * Eliminar un usuario
     * Método: DELETE /api/usuarios/{id}
     */
    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        if (jwt == null) {
            return buildError(HttpStatus.UNAUTHORIZED, "unauthorized");
        }

        Long userIdFromToken = Long.parseLong(jwt.getSubject());

        return userService.findById(id)
                .map(u -> {
                    if (!u.getId().equals(userIdFromToken)) {
                        return buildError(HttpStatus.FORBIDDEN, "forbidden");
                    }
                    userService.delete(u);
                    return ResponseEntity.noContent().build();
                })
                .orElseGet(() -> buildError(HttpStatus.NOT_FOUND, "user_not_found"));
    }

    // ---------------------------
    // Helper para respuestas de error
    // ---------------------------
    private ResponseEntity<Map<String, Object>> buildError(HttpStatus status, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("code", status.value());
        error.put("message", message);
        return ResponseEntity.status(status).body(error);
    }
}
