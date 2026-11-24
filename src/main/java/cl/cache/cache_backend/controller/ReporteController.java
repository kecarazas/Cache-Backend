package cl.cache.cache_backend.controller;

import cl.cache.cache_backend.config.OpenApiConfig;
import cl.cache.cache_backend.dto.ResumenVentasResponse;
import cl.cache.cache_backend.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
@SecurityRequirement(name = OpenApiConfig.BEARER)
@Tag(name = "Reportes", description = "Indicadores de ventas (solo ADMIN)")
public class ReporteController {

    private final ReporteService service;

    @GetMapping("/resumen")
    @Operation(summary = "Total vendido, cantidad de pedidos, ticket promedio y productos más vendidos (ADMIN)")
    public ResumenVentasResponse resumen() {
        return service.resumen();
    }
}