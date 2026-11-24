package cl.cache.cache_backend.controller;


import cl.cache.cache_backend.config.OpenApiConfig;
import cl.cache.cache_backend.dto.InventarioCategoriaResponse;
import cl.cache.cache_backend.dto.ProductoRequest;
import cl.cache.cache_backend.dto.ProductoResponse;
import cl.cache.cache_backend.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Catálogo público; administración solo ADMIN")
public class ProductoController {

    private final ProductoService service;

    @GetMapping
    @Operation(summary = "Catálogo completo, o filtrado por categoría (público)")
    public List<ProductoResponse> listar(
            @Parameter(description = "Opcional: slug de la categoría, por ejemplo mouse") @RequestParam(required = false) String categoria) {
        return service.listar(categoria);
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Detalle de un producto por su slug, por ejemplo mouse-1 (público)")
    @ApiResponse(responseCode = "404", description = "El producto no existe")
    public ProductoResponse obtenerPorSlug(@PathVariable String slug) {
        return service.obtenerPorSlug(slug);
    }

    @GetMapping("/destacados")
    @Operation(summary = "Productos destacados (público)")
    public List<ProductoResponse> destacados() {
        return service.destacados();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un producto (público)")
    @ApiResponse(responseCode = "404", description = "El producto no existe")
    public ProductoResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @GetMapping("/ofertas")
    @Operation(summary = "Productos en oferta (público)")
    public List<ProductoResponse> ofertas() {
        return service.ofertas();
    }

    @GetMapping("/criticos")
    @SecurityRequirement(name = OpenApiConfig.BEARER)
    @Operation(summary = "Productos con stock igual o menor a su stock crítico (ADMIN)")
    public List<ProductoResponse> criticos() {
        return service.criticos();
    }

    @GetMapping("/reportes/inventario")
    @SecurityRequirement(name = OpenApiConfig.BEARER)
    @Operation(summary = "Inventario por categoría: productos, unidades y valor (ADMIN)")
    public List<InventarioCategoriaResponse> inventario() {
        return service.inventarioPorCategoria();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = OpenApiConfig.BEARER)
    @Operation(summary = "Crear producto (ADMIN)")
    @ApiResponse(responseCode = "409", description = "Código repetido u oferta mayor o igual al precio")
    public ProductoResponse crear(@Valid @RequestBody ProductoRequest req) {
        return service.crear(req);
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = OpenApiConfig.BEARER)
    @Operation(summary = "Editar producto (ADMIN)")
    public ProductoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = OpenApiConfig.BEARER)
    @Operation(summary = "Eliminar producto (ADMIN)")
    @ApiResponse(responseCode = "409", description = "El producto aparece en pedidos")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
