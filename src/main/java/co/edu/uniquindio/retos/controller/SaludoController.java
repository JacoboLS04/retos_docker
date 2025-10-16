package co.edu.uniquindio.retos.controller;

import co.edu.uniquindio.retos.model.SaludoRegistro;
import co.edu.uniquindio.retos.service.SaludoRegistroService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

    /**
     * Endpoint opcional: devolver un saludo inmediato y registrarlo.
     * GET /api/saludo?nombre=Santiago

    @GetMapping("/saludo")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> saludo(@RequestParam(required = false) String nombre,
                                    @AuthenticationPrincipal Jwt jwt) {
        if (nombre == null || nombre.isBlank()) {
            return buildError(HttpStatus.BAD_REQUEST, "missing_name");
        }
        if (jwt == null) {
            return buildError(HttpStatus.UNAUTHORIZED, "unauthorized");
        }

        String nombreToken = jwt.getClaimAsString(NOMBRE_CLAIM);
        if (nombreToken == null) {
            return buildError(HttpStatus.FORBIDDEN, "missing_claim_nombre");
        }
        if (!nombreToken.equalsIgnoreCase(nombre)) {
            return buildError(HttpStatus.FORBIDDEN, "name_mismatch");
        }

        SaludoRegistro reg = registroService.registrar(nombreToken);

        return ResponseEntity.ok(Map.of(
                "message", "hola " + nombreToken,
                "saludoId", reg.getId(),
                "fechaHora", reg.getFechaHora().toString()
        ));
    }

    /**
     * Listar saludos con paginación
     * GET /api/saludos?page=0&size=10

    @GetMapping("/saludos")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> listar(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return buildError(HttpStatus.UNAUTHORIZED, "unauthorized");
        }

        Page<SaludoRegistro> resultados = registroService.listar(PageRequest.of(page, size));

        Map<String, Object> response = new HashMap<>();
        response.put("page", resultados.getNumber());
        response.put("size", resultados.getSize());
        response.put("totalElements", resultados.getTotalElements());
        response.put("totalPages", resultados.getTotalPages());
        response.put("data", resultados.getContent());

        return ResponseEntity.ok(response);
    }

    /**
     * Helper para construir errores consistentes

    private ResponseEntity<Map<String, Object>> buildError(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "code", status.value(),
                "message", message
        ));
    } */
}
