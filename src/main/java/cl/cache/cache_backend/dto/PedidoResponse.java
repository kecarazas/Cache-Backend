package cl.cache.cache_backend.dto;

import cl.cache.cache_backend.model.Pedido;

import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
        Long id,
        String codigo,
        LocalDateTime fecha,
        String estado,
        Long usuarioId,
        Cliente cliente,
        List<Item> productos,
        String cupon,
        Totales totales
) {
    public record Cliente(String nombre, String correo, String telefono, String region, String comuna, String direccion) {
    }

    public record Item(String slug, String nombre, Integer precio, Integer cantidad, Integer subtotal) {
    }

    public record Totales(Integer subtotal, Integer descuento, Integer envio, Integer total) {
    }

    /** Debe llamarse dentro de una transacción: lee relaciones LAZY (usuario, detalles, producto). */
    public static PedidoResponse from(Pedido p) {
        return new PedidoResponse(
                p.getId(), String.format("PED-%06d", p.getId()), p.getFecha(), p.getEstado().name().toLowerCase(),
                p.getUsuario() != null ? p.getUsuario().getId() : null,
                new Cliente(p.getClienteNombre(), p.getClienteCorreo(), p.getClienteTelefono(),
                        p.getRegion(), p.getComuna(), p.getDireccion()),
                p.getDetalles().stream()
                        .map(d -> new Item(d.getProducto().getSlug(), d.getProducto().getNombre(),
                                d.getPrecioUnitario(), d.getCantidad(), d.getSubtotal()))
                        .toList(),
                p.getCupon(),
                new Totales(p.getSubtotal(), p.getDescuento(), p.getEnvio(), p.getTotal()));
    }
}
