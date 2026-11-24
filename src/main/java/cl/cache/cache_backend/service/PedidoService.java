package cl.cache.cache_backend.service;

import cl.cache.cache_backend.dto.CompraRequest;
import cl.cache.cache_backend.dto.ItemCompraRequest;
import cl.cache.cache_backend.dto.PedidoResponse;
import cl.cache.cache_backend.exception.RecursoNoEncontradoException;
import cl.cache.cache_backend.exception.ReglaNegocioException;
import cl.cache.cache_backend.model.DetallePedido;
import cl.cache.cache_backend.model.EstadoPedido;
import cl.cache.cache_backend.model.Pedido;
import cl.cache.cache_backend.model.Producto;
import cl.cache.cache_backend.model.Usuario;
import cl.cache.cache_backend.repository.PedidoRepository;
import cl.cache.cache_backend.repository.ProductoRepository;
import cl.cache.cache_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

//Logica del negocio

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PedidoService {

    static final int COSTO_ENVIO = 2990;
    static final int ENVIO_GRATIS_DESDE = 20000;
    static final String CUPON_DESCUENTO = "CACHE10";
    static final int PORCENTAJE_DESCUENTO = 10;
    static final String CUPON_ENVIO_GRATIS = "ENVIOGRATIS";

    private final PedidoRepository pedidoRepo;
    private final ProductoRepository productoRepo;
    private final UsuarioRepository usuarioRepo;


    //Registra compra
    @Transactional
    public PedidoResponse crearCompra(CompraRequest req, String correoUsuario) {
        Pedido pedido = new Pedido();
        pedido.setFecha(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.RECIBIDO);
        pedido.setClienteNombre(req.nombre().trim());
        pedido.setClienteCorreo(req.correo().trim().toLowerCase());
        pedido.setClienteTelefono(req.telefono().trim());
        pedido.setRegion(req.region().trim());
        pedido.setComuna(req.comuna().trim());
        pedido.setDireccion(req.direccion().trim());

        // si quien compra inició sesion, el pedido queda asociado a su cuenta
        if (correoUsuario != null) {
            usuarioRepo.findByCorreoIgnoreCase(correoUsuario).ifPresent(pedido::setUsuario);
        }

        int subtotal = 0;
        for (ItemCompraRequest item : req.items()) {
            Producto p = productoRepo.findBySlug(item.slug().trim())
                    .orElseThrow(() -> new RecursoNoEncontradoException("El producto " + item.slug() + " no existe"));
            if (p.getStock() < item.cantidad()) {
                throw new ReglaNegocioException("Stock insuficiente para " + p.getNombre()
                        + " (disponible: " + p.getStock() + ", solicitado: " + item.cantidad() + ")");
            }
            p.setStock(p.getStock() - item.cantidad()); // descuenta inventario
            DetallePedido detalle = new DetallePedido(p, item.cantidad(), p.precioFinal()); // precio desde la BD
            pedido.agregarDetalle(detalle);
            subtotal += detalle.getSubtotal();
        }

        //cupon, descuento y envio
        String cupon = req.cupon() == null || req.cupon().isBlank() ? null : req.cupon().trim().toUpperCase();
        int descuento = 0;
        boolean envioGratis = subtotal >= ENVIO_GRATIS_DESDE;
        if (cupon != null) {
            switch (cupon) {
                case CUPON_DESCUENTO -> descuento = (int) Math.round(subtotal * PORCENTAJE_DESCUENTO / 100.0);
                case CUPON_ENVIO_GRATIS -> envioGratis = true;
                default -> throw new ReglaNegocioException("El cupón no es válido");
            }
        }
        int envio = envioGratis ? 0 : COSTO_ENVIO;

        pedido.setCupon(cupon);
        pedido.setSubtotal(subtotal);
        pedido.setDescuento(descuento);
        pedido.setEnvio(envio);
        pedido.setTotal(subtotal - descuento + envio);
        return PedidoResponse.from(pedidoRepo.save(pedido));
    }

    public List<PedidoResponse> listar() {
        return pedidoRepo.findAllByOrderByFechaDesc().stream().map(PedidoResponse::from).toList();
    }

    //
    public PedidoResponse obtener(Long id, String correoUsuario, boolean esPersonal) {
        Pedido p = buscar(id);
        boolean esDueno = p.getUsuario() != null && p.getUsuario().getCorreo().equalsIgnoreCase(correoUsuario);
        if (!esPersonal && !esDueno) {
            throw new AccessDeniedException("El pedido no pertenece al usuario");
        }
        return PedidoResponse.from(p);
    }

    public List<PedidoResponse> porUsuario(Long usuarioId) {
        return pedidoRepo.findByUsuarioIdOrderByFechaDesc(usuarioId).stream().map(PedidoResponse::from).toList();
    }

    public List<PedidoResponse> misCompras(String correoUsuario) {
        Usuario u = usuarioRepo.findByCorreoIgnoreCase(correoUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        return porUsuario(u.getId());
    }

    @Transactional
    public PedidoResponse cambiarEstado(Long id, EstadoPedido nuevo) {
        Pedido p = buscar(id);
        if (p.getEstado() == EstadoPedido.ANULADO) {
            throw new ReglaNegocioException("El pedido #" + id + " está anulado y no se puede modificar");
        }
        if (nuevo == EstadoPedido.ANULADO) {
            // devolver al inventario lo que se habia vendido
            p.getDetalles().forEach(d -> d.getProducto().setStock(d.getProducto().getStock() + d.getCantidad()));
        }
        p.setEstado(nuevo);
        return PedidoResponse.from(p);
    }

    private Pedido buscar(Long id) {
        return pedidoRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El pedido " + id + " no existe"));
    }
}
