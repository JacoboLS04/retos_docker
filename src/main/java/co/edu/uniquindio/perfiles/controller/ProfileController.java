package co.edu.uniquindio.perfiles.controller;

import co.edu.uniquindio.perfiles.model.Profile;
import co.edu.uniquindio.perfiles.service.ProfileService;
import co.edu.uniquindio.perfiles.web.dto.ProfilePatchRequest;
import co.edu.uniquindio.perfiles.web.dto.ProfileRequest;
import co.edu.uniquindio.perfiles.web.dto.ProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/profile")
@Tag(name = "Perfil")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final ProfileService service;

    public ProfileController(ProfileService service) {
        this.service = service;
    }

    private Long currentUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            Object sub = jwt.getClaim("sub");
            if (sub == null) throw new EntityNotFoundException("Token sin 'sub'");
            try {
                return Long.valueOf(sub.toString());
            } catch (NumberFormatException e) {
                throw new EntityNotFoundException("El 'sub' del token no es numérico");
            }
        }
        throw new EntityNotFoundException("Autenticación inválida");
    }

    private static ProfileResponse toResponse(Profile p) {
        ProfileResponse r = new ProfileResponse();
        r.setId(p.getId());
        r.setPageUrl(p.getPageUrl());
        r.setNickname(p.getNickname());
        r.setContactPublic(p.isContactPublic());
        r.setAddress(p.getAddress());
        r.setBio(p.getBio());
        r.setOrganization(p.getOrganization());
        r.setCountry(p.getCountry());
        r.setSocialLinks(p.getSocialLinks());
        return r;
    }

    @GetMapping
    @Operation(summary = "Obtiene el perfil del usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Perfil no existe",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<ProfileResponse> get(Authentication authentication) {
        Long userId = currentUserId(authentication);
        Profile p = service.getByUserIdOrThrow(userId);
        return ResponseEntity.ok(toResponse(p));
    }

    @PostMapping
    @Operation(summary = "Crea el perfil del usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "409", description = "Ya existe",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<ProfileResponse> create(Authentication authentication,
                                                  @Valid @RequestBody ProfileRequest request) {
        Long userId = currentUserId(authentication);
        Profile created = service.create(userId, request);
        return ResponseEntity.created(URI.create("/profile")).body(toResponse(created));
    }

    @PutMapping
    @Operation(summary = "Reemplaza (o crea) el perfil del usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<ProfileResponse> replace(Authentication authentication,
                                                   @Valid @RequestBody ProfileRequest request) {
        Long userId = currentUserId(authentication);
        Profile updated = service.replace(userId, request);
        return ResponseEntity.ok(toResponse(updated));
    }

    @PatchMapping
    @Operation(summary = "Actualización parcial del perfil del usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "No existe",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<ProfileResponse> patch(Authentication authentication,
                                                 @Valid @RequestBody ProfilePatchRequest request) {
        Long userId = currentUserId(authentication);
        Profile updated = service.patch(userId, request);
        return ResponseEntity.ok(toResponse(updated));
    }

    @DeleteMapping
    @Operation(summary = "Elimina el perfil del usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Eliminado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "No existe",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Void> delete(Authentication authentication) {
        Long userId = currentUserId(authentication);
        service.delete(userId);
        return ResponseEntity.noContent().build();
    }
}
