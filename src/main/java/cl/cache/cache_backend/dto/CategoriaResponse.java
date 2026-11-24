package cl.cache.cache_backend.dto;

import cl.cache.cache_backend.model.Categoria;

public record CategoriaResponse(Long id, String slug, String nombre, String descripcion, long cantidadProductos) {

    public static CategoriaResponse from(Categoria c, long cantidadProductos) {
        return new CategoriaResponse(c.getId(), c.getSlug(), c.getNombre(), c.getDescripcion(), cantidadProductos);
    }
}
