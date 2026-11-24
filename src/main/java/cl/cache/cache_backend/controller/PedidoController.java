package cl.cache.cache_backend.controller;

import cl.cache.cache_backend.config.OpenApiConfig;
import cl.cache.cache_backend.dto.PedidoResponse;
import cl.cache.cache_backend.dto.CompraRequest;
import cl.cache.cache_backend.dto.EstadoRequest;
import cl.cache.cache_backend.service.PedidoService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pedidos")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Compras de la tienda y gestión de órdenes")
public class PedidoController {

    private final PedidoService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar una compra (público)",
            description = "Recibe solo slug y cantidad de cada producto: el precio, el cupón, el envío y el total los calcula el backend."
                    + "Si la petición trae token, el pedido queda asociada al usuario.")
    @ApiResponse(responseCode = "409", description = "Stock insuficiente")
    public PedidoResponse comprar(@Valid @RequestBody CompraRequest req,
                                  @Parameter(hidden = true) @AuthenticationPrincipal UserDetails usuario) {
        return service.crearCompra(req, usuario != null ? usuario.getUsername() : null);
    }

    @GetMapping("/mias")
    @SecurityRequirement(name = OpenApiConfig.BEARER)
    @Operation(summary = "Mis compras (con sesión)")
    public List<PedidoResponse> misCompras(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails usuario) {
        return service.misCompras(usuario.getUsername());
    }

    @GetMapping
    @SecurityRequirement(name = OpenApiConfig.BEARER)
    @Operation(summary = "Listar pedidos (ADMIN, VENDEDOR)")
    public List<PedidoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = OpenApiConfig.BEARER)
    @Operation(summary = "Ver un pedido (ADMIN, VENDEDOR o el cliente dueño)")
    @ApiResponse(responseCode = "403", description = "Un cliente intenta ver un pedido ajeno")
    public PedidoResponse obtener(@PathVariable Long id,
                                  @Parameter(hidden = true) @AuthenticationPrincipal UserDetails usuario) {
        boolean esPersonal = usuario.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_VENDEDOR"));
        return service.obtener(id, usuario.getUsername(), esPersonal);
    }

    @GetMapping("/usuario/{usuarioId}")
    @SecurityRequirement(name = OpenApiConfig.BEARER)
    @Operation(summary = "Historial de compras de un usuario (ADMIN)")
    public List<PedidoResponse> porUsuario(@PathVariable Long usuarioId) {
        return service.porUsuario(usuarioId);
    }

    @PatchMapping("/{id}/estado")
    @SecurityRequirement(name = OpenApiConfig.BEARER)
    @Operation(summary = "Cambiar estado: recibido, enviado, entregado o anulado (ADMIN)",
            description = "Al anular, el stock vuelve al inventario. Un pedido anulado no se puede modificar.")
    @ApiResponse(responseCode = "409", description = "El pedido ya está anulado")
    public PedidoResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoRequest req) {
        return service.cambiarEstado(id, req.estado());
    }
}
