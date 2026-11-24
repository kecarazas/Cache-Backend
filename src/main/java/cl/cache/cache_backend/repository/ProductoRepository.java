package cl.cache.cache_backend.repository;

import cl.cache.cache_backend.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByCodigoIgnoreCase(String codigo);

    List<Producto> findAllByOrderByIdAsc();

    Optional<Producto> findBySlug(String slug);

    List<Producto> findByCategoriaIdOrderByIdAsc(Long categoriaId);

    List<Producto> findByCategoriaSlugIgnoreCaseOrderByIdAsc(String categoriaSlug);

    List<Producto> findByDestacadoTrueOrderByIdAsc();

    long countByCategoriaId(Long categoriaId);

    /** Ofertas: compara dos columnas, por eso se escribe la consulta en JPQL. */
    @Query("SELECT p FROM Producto p WHERE p.precioOferta IS NOT NULL AND p.precioOferta > 0 AND p.precioOferta < p.precio ORDER BY p.id")
    List<Producto> findEnOferta();

    /** Stock crítico: stock igual o menor al stock crítico. */
    @Query("SELECT p FROM Producto p WHERE p.stock <= p.stockCritico ORDER BY p.stock ASC")
    List<Producto> findCriticos();
}
