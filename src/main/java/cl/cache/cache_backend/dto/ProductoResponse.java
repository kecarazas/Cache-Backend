package cl.cache.cache_backend.dto;

import cl.cache.cache_backend.model.Producto;

import java.util.List;

public record ProductoResponse(
        Long id,
        String codigo,
        String slug,
        String nombre,
        String descripcion,
        String marca,
        String etiqueta,
        Integer precio,
        Integer precioOferta,
        Integer precioFinal,
        boolean enOferta,
        String descuento,
        Integer stock,
        Integer stockCritico,
        boolean critico,
        String imagen,
        double opiniones,
        boolean destacado,
        List<String> especificaciones,
        String categoria,
        String categoriaNombre
) {
    public static ProductoResponse from(Producto p) {
        return new ProductoResponse(
                p.getId(), p.getCodigo(), p.getSlug(), p.getNombre(), p.getDescripcion(), p.getMarca(), p.getEtiqueta(),
                p.getPrecio(), p.getPrecioOferta(), p.precioFinal(), p.enOferta(), p.descuentoTexto(),
                p.getStock(), p.getStockCritico(), p.critico(), p.getImagen(), p.getOpiniones(), p.isDestacado(),
                List.copyOf(p.getEspecificaciones()),
                p.getCategoria().getSlug(), p.getCategoria().getNombre());
    }
}
