package cl.cache.cache_backend.controller;

import cl.cache.cache_backend.config.OpenApiConfig;
import cl.cache.cache_backend.dto.CategoriaRequest;
import cl.cache.cache_backend.dto.CategoriaResponse;
import cl.cache.cache_backend.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

@RestController
@RequestMapping("/api/v1/categorias")
@RequiredArgsConstructor
@Tag(name = "Categorías", description = "Lectura pública; creación, edición y eliminación solo ADMIN")
public class CategoriaController {

    private final CategoriaService service;

    @GetMapping
    @Operation(summary = "Listar categorías con su cantidad de productos (público)")
    public List<CategoriaResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver una categoría (público)")
    @ApiResponse(responseCode = "404", description = "La categoría no existe")
    public CategoriaResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = OpenApiConfig.BEARER)
    @Operation(summary = "Crear categoría (ADMIN)")
    @ApiResponse(responseCode = "409", description = "Nombre repetido")
    public CategoriaResponse crear(@Valid @RequestBody CategoriaRequest req) {
        return service.crear(req);
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = OpenApiConfig.BEARER)
    @Operation(summary = "Editar categoría (ADMIN)")
    public CategoriaResponse actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = OpenApiConfig.BEARER)
    @Operation(summary = "Eliminar categoría (ADMIN)")
    @ApiResponse(responseCode = "409", description = "La categoría tiene productos asociados")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
