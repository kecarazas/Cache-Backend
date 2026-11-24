package cl.cache.cache_backend.repository;

import cl.cache.cache_backend.model.Pedido;
import cl.cache.cache_backend.model.EstadoPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findAllByOrderByFechaDesc();

    List<Pedido> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

    List<Pedido> findByEstadoNot(EstadoPedido estado);
}
