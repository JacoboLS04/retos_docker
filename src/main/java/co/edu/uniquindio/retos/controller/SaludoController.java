package co.edu.uniquindio.retos.controller;

import co.edu.uniquindio.retos.model.SaludoRegistro;
import co.edu.uniquindio.retos.service.SaludoRegistroService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SaludoController {

    private final SaludoRegistroService registroService;
    private static final String NOMBRE_CLAIM = "nombre";

    public SaludoController(SaludoRegistroService registroService) {
        this.registroService = registroService;
    }

    @GetMapping("/saludo")
    public ResponseEntity<String> saludo(@RequestParam(required = false) String nombre,
                                         @AuthenticationPrincipal Jwt jwt) {
        if (nombre == null || nombre.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Solicitud no válida: El nombre es obligatorio");
        }
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado");
        }
        String nombreToken = jwt.getClaimAsString(NOMBRE_CLAIM);
        if (nombreToken == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("El token no contiene el claim 'nombre'.");
        }
        if (!nombreToken.equalsIgnoreCase(nombre)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("El 'nombre' del query no coincide con el del token.");
        }
        SaludoRegistro reg = registroService.registrar(nombreToken);
        return ResponseEntity.ok("hola " + nombreToken);
    }

    @GetMapping("/saludos")
    public ResponseEntity<Page<SaludoRegistro>> listar(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size,
                                                       @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Page<SaludoRegistro> resultados = registroService.listar(PageRequest.of(page, size));
        return ResponseEntity.ok(resultados);
    }
}
