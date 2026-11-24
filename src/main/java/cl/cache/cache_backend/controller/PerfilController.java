package cl.cache.cache_backend.controller;

import cl.cache.cache_backend.config.OpenApiConfig;
import cl.cache.cache_backend.dto.PerfilRequest;
import cl.cache.cache_backend.dto.UsuarioResponse;
import cl.cache.cache_backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//Datos propios del usuario
@RestController
@RequestMapping("/api/v1/perfil")
@RequiredArgsConstructor
@SecurityRequirement(name = OpenApiConfig.BEARER)
@Tag(name = "Perfil", description = "Datos del usuario con sesión")
public class PerfilController {

    private final UsuarioService service;

    @GetMapping
    @Operation(summary = "Ver mi perfil (con sesión)")
    public UsuarioResponse ver(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails usuario) {
        return service.perfil(usuario.getUsername());
    }

    @PutMapping
    @Operation(summary = "Editar mi perfil (con sesión)", description = "No permite cambiar el rol ni el correo. Password vacía = no se cambia")
    public UsuarioResponse actualizar(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails usuario,
                                      @Valid @RequestBody PerfilRequest req) {
        return service.actualizarPerfil(usuario.getUsername(), req);
    }
}