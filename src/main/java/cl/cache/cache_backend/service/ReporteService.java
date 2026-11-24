package cl.cache.cache_backend.service;

import cl.cache.cache_backend.dto.ResumenVentasResponse;
import cl.cache.cache_backend.model.Pedido;
import cl.cache.cache_backend.model.EstadoPedido;
import cl.cache.cache_backend.repository.PedidoRepository;
import cl.cache.cache_backend.repository.DetallePedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

//Reporte de ventas en dashboard
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteService {

    private static final int TOP_PRODUCTOS = 10;

    private final PedidoRepository pedidoRepo;
    private final DetallePedidoRepository detalleRepo;

    public ResumenVentasResponse resumen() {
        List<Pedido> vigentes = pedidoRepo.findByEstadoNot(EstadoPedido.ANULADO);
        long total = vigentes.stream().mapToLong(Pedido::getTotal).sum();
        long cantidad = vigentes.size();
        List<ResumenVentasResponse.ProductoVendido> masVendidos = detalleRepo.masVendidos(EstadoPedido.ANULADO)
                .stream()
                .limit(TOP_PRODUCTOS)
                .map(fila -> new ResumenVentasResponse.ProductoVendido((String) fila[0], ((Number) fila[1]).longValue()))
                .toList();
        return new ResumenVentasResponse(cantidad, total, cantidad == 0 ? 0 : Math.round((double) total / cantidad), masVendidos);
    }
}
