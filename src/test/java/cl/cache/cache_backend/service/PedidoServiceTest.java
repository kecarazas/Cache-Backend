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
import cl.cache.cache_backend.model.Rol;
import cl.cache.cache_backend.model.Usuario;
import cl.cache.cache_backend.repository.PedidoRepository;
import cl.cache.cache_backend.repository.ProductoRepository;
import cl.cache.cache_backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    PedidoRepository pedidoRepo;
    @Mock
    ProductoRepository productoRepo;
    @Mock
    UsuarioRepository usuarioRepo;
    @InjectMocks
    PedidoService service;

    private final Usuario pedro = Usuario.builder().id(2L).correo("pedro@gmail.com").nombre("Pedro").rol(Rol.CLIENTE).build();

    private Producto mouse() {
        return Producto.builder().id(1L).slug("mouse-1").codigo("MOU-001").nombre("Mouse Gamer Void Pro")
                .precio(89990).stock(30).build();
    }

    private Producto skate() {
        return Producto.builder().id(2L).slug("skate-1").codigo("SKA-001").nombre("Skates PTFE")
                .precio(4990).stock(1).build();
    }

    private CompraRequest compra(String cupon, ItemCompraRequest... items) {
        return new CompraRequest("Pedro Hacker", "pedro@gmail.com", "+56 9 1234 5678",
                "Región Metropolitana", "Maipú", "Los Crisantemos 123", cupon, List.of(items));
    }

    @Test
    void crearCompra_usaPrecioDeLaBD_descuentaStockYCalculaTotales() {
        Producto mc = mouse();
        when(productoRepo.findBySlug("mouse-1")).thenReturn(Optional.of(mc));
        when(pedidoRepo.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        PedidoResponse r = service.crearCompra(compra(null, new ItemCompraRequest("mouse-1", 2)), null);

        assertThat(r.totales().subtotal()).isEqualTo(2 * 89990);
        assertThat(r.totales().envio()).isZero();
        assertThat(r.totales().total()).isEqualTo(2 * 89990);
        assertThat(mc.getStock()).isEqualTo(28);
        assertThat(r.estado()).isEqualTo("recibido");
        assertThat(r.usuarioId()).isNull();
        assertThat(r.productos()).extracting(PedidoResponse.Item::slug).containsExactly("mouse-1");
    }

    @Test
    void crearCompra_bajoElMinimo_cobraEnvio() {
        when(productoRepo.findBySlug("skate-1")).thenReturn(Optional.of(skate()));
        when(pedidoRepo.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        PedidoResponse r = service.crearCompra(compra(null, new ItemCompraRequest("skate-1", 1)), null);

        assertThat(r.totales().envio()).isEqualTo(2990);
        assertThat(r.totales().total()).isEqualTo(4990 + 2990);
    }

    @Test
    void crearCompra_cupon10_descuentaElDiezPorCiento() {
        when(productoRepo.findBySlug("mouse-1")).thenReturn(Optional.of(mouse()));
        when(pedidoRepo.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        PedidoResponse r = service.crearCompra(compra("cache10", new ItemCompraRequest("mouse-1", 1)), null);

        assertThat(r.cupon()).isEqualTo("CACHE10");
        assertThat(r.totales().descuento()).isEqualTo(8999);
        assertThat(r.totales().total()).isEqualTo(89990 - 8999);
    }

    @Test
    void crearCompra_cuponEnvioGratis_quitaElCostoDeEnvio() {
        when(productoRepo.findBySlug("skate-1")).thenReturn(Optional.of(skate()));
        when(pedidoRepo.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        PedidoResponse r = service.crearCompra(compra("ENVIOGRATIS", new ItemCompraRequest("skate-1", 1)), null);

        assertThat(r.totales().envio()).isZero();
        assertThat(r.totales().total()).isEqualTo(4990);
    }

    @Test
    void crearCompra_cuponInvalido_lanzaReglaNegocioYNoGuarda() {
        when(productoRepo.findBySlug("mouse-1")).thenReturn(Optional.of(mouse()));

        assertThatThrownBy(() -> service.crearCompra(compra("FALSO", new ItemCompraRequest("mouse-1", 1)), null))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("cupón");
        verify(pedidoRepo, never()).save(any());
    }

    @Test
    void crearCompra_conSesion_asociaElPedidoAlUsuario() {
        when(usuarioRepo.findByCorreoIgnoreCase("pedro@gmail.com")).thenReturn(Optional.of(pedro));
        when(productoRepo.findBySlug("mouse-1")).thenReturn(Optional.of(mouse()));
        when(pedidoRepo.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        PedidoResponse r = service.crearCompra(compra(null, new ItemCompraRequest("mouse-1", 1)), "pedro@gmail.com");

        assertThat(r.usuarioId()).isEqualTo(2L);
    }

    @Test
    void crearCompra_stockInsuficiente_lanzaReglaNegocioYNoGuarda() {
        when(productoRepo.findBySlug("mouse-1")).thenReturn(Optional.of(mouse()));
        when(productoRepo.findBySlug("skate-1")).thenReturn(Optional.of(skate()));

        assertThatThrownBy(() -> service.crearCompra(
                compra(null, new ItemCompraRequest("mouse-1", 1), new ItemCompraRequest("skate-1", 5)), null))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Stock insuficiente");
        verify(pedidoRepo, never()).save(any());
    }

    @Test
    void crearCompra_productoInexistente_lanzaNoEncontrado() {
        when(productoRepo.findBySlug("mouse-99")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crearCompra(compra(null, new ItemCompraRequest("mouse-99", 1)), null))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    private Pedido pedidoDe(Usuario usuario, Producto p, int cantidad, EstadoPedido estado) {
        Pedido b = new Pedido();
        b.setId(10L);
        b.setFecha(LocalDateTime.now());
        b.setEstado(estado);
        b.setUsuario(usuario);
        b.setClienteNombre("Pedro Hacker");
        b.setClienteCorreo("pedro@gmail.com");
        b.setClienteTelefono("+56 9 1234 5678");
        b.setRegion("Región Metropolitana");
        b.setComuna("Maipú");
        b.setDireccion("Los Crisantemos 123");
        b.agregarDetalle(new DetallePedido(p, cantidad, p.precioFinal()));
        b.setSubtotal(cantidad * p.precioFinal());
        b.setDescuento(0);
        b.setEnvio(0);
        b.setTotal(cantidad * p.precioFinal());
        return b;
    }

    @Test
    void cambiarEstado_aAnulado_devuelveElStock() {
        Producto mc = mouse();
        Pedido b = pedidoDe(pedro, mc, 2, EstadoPedido.RECIBIDO);
        when(pedidoRepo.findById(10L)).thenReturn(Optional.of(b));

        PedidoResponse r = service.cambiarEstado(10L, EstadoPedido.ANULADO);

        assertThat(r.estado()).isEqualTo("anulado");
        assertThat(mc.getStock()).isEqualTo(32);
    }

    @Test
    void cambiarEstado_pedidoYaAnulado_lanzaReglaNegocio() {
        Pedido b = pedidoDe(pedro, mouse(), 1, EstadoPedido.ANULADO);
        when(pedidoRepo.findById(10L)).thenReturn(Optional.of(b));

        assertThatThrownBy(() -> service.cambiarEstado(10L, EstadoPedido.ENVIADO))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void obtener_clienteVePedidoAjeno_lanzaAccesoDenegado() {
        Pedido b = pedidoDe(pedro, mouse(), 1, EstadoPedido.RECIBIDO);
        when(pedidoRepo.findById(10L)).thenReturn(Optional.of(b));

        assertThatThrownBy(() -> service.obtener(10L, "otro@gmail.com", false))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void obtener_vendedorPuedeVerCualquierPedido() {
        Pedido b = pedidoDe(pedro, mouse(), 1, EstadoPedido.RECIBIDO);
        when(pedidoRepo.findById(10L)).thenReturn(Optional.of(b));

        PedidoResponse r = service.obtener(10L, "vendedor@cache.cl", true);

        assertThat(r.id()).isEqualTo(10L);
        assertThat(r.codigo()).isEqualTo("PED-000010");
    }
}
