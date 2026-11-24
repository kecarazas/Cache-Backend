package cl.cache.cache_backend.repository;

import cl.cache.cache_backend.model.DetallePedido;
import cl.cache.cache_backend.model.EstadoPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    boolean existsByProductoId(Long productoId);

    /** Productos más vendidos (sin contar pedidos anulados). Cada fila es [nombre, unidades]. */
    @Query("""
            SELECT d.producto.nombre, SUM(d.cantidad)
            FROM DetallePedido d
            WHERE d.pedido.estado <> :excluido
            GROUP BY d.producto.nombre
            ORDER BY SUM(d.cantidad) DESC
            """)
    List<Object[]> masVendidos(@Param("excluido") EstadoPedido excluido);
}
