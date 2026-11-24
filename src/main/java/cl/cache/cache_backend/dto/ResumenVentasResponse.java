package cl.cache.cache_backend.dto;

import java.util.List;

public record ResumenVentasResponse(
        long cantidadOrdenes,
        long totalVentas,
        long ticketPromedio,
        List<ProductoVendido> masVendidos
) {
    public record ProductoVendido(String nombre, long cantidad) {
    }
}
