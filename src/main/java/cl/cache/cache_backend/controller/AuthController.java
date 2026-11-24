package cl.cache.cache_backend.controller;

import cl.cache.cache_backend.config.OpenApiConfig;
import cl.cache.cache_backend.dto.AuthResponse;
import cl.cache.cache_backend.dto.LoginRequest;
import cl.cache.cache_backend.dto.RefreshRequest;
import cl.cache.cache_backend.dto.RegistroRequest;
import cl.cache.cache_backend.dto.UsuarioResponse;
import cl.cache.cache_backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Inicio de sesión, registro y renovación de tokens JWT")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión (público)", description = "Devuelve un access token (15 min) y un refresh token (7 días)")
    @ApiResponse(responseCode = "401", description = "Correo o contraseña incorrectos")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear cuenta (público)", description = "Siempre crea un usuario con rol CLIENTE e inicia su sesión")
    @ApiResponse(responseCode = "409", description = "El RUN o el correo ya están registrados")
    public AuthResponse registro(@Valid @RequestBody RegistroRequest req) {
        return authService.registro(req);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar tokens (público)", description = "Recibe un refresh token válido y entrega un par de tokens nuevo")
    @ApiResponse(responseCode = "401", description = "Refresh token vencido, inválido o es un access token")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest req) {
        return authService.refresh(req.refreshToken());
    }

    @GetMapping("/me")
    @SecurityRequirement(name = OpenApiConfig.BEARER)
    @Operation(summary = "Datos del usuario dueño del token (con sesión)",
            description = "El frontend lo usa al recargar la página para recuperar la sesión")
    public UsuarioResponse me(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails usuario) {
        return authService.usuarioActual(usuario.getUsername());
    }
}
