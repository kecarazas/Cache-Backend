package cl.cache.cache_backend.controller;

import cl.cache.cache_backend.config.OpenApiConfig;
import cl.cache.cache_backend.dto.UsuarioRequest;
import cl.cache.cache_backend.dto.UsuarioResponse;
import cl.cache.cache_backend.service.UsuarioService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

//CRUD de usuarios (solo ADMIN)
@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@SecurityRequirement(name = OpenApiConfig.BEARER)
@Tag(name = "Usuarios", description = "Gestión de usuarios (solo ADMIN)")
public class UsuarioController {

    private final UsuarioService service;

    @GetMapping
    @Operation(summary = "Listar usuarios (ADMIN)")
    public List<UsuarioResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver un usuario (ADMIN)")
    @ApiResponse(responseCode = "404", description = "El usuario no existe")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear usuario con cualquier rol (ADMIN)")
    @ApiResponse(responseCode = "409", description = "RUN o correo repetido")
    public UsuarioResponse crear(@Valid @RequestBody UsuarioRequest req) {
        return service.crear(req);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar usuario (ADMIN)", description = "Si password viene vacía, se mantiene la actual")
    public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar usuario (ADMIN)", description = "Un administrador no puede eliminarse a sí mismo")
    public void eliminar(@PathVariable Long id, @Parameter(hidden = true) @AuthenticationPrincipal UserDetails actual) {
        service.eliminar(id, actual.getUsername());
    }
}
