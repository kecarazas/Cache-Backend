package cl.cache.cache_backend.service;

import cl.cache.cache_backend.exception.RecursoNoEncontradoException;
import cl.cache.cache_backend.exception.ReglaNegocioException;
import cl.cache.cache_backend.dto.ProductoRequest;
import cl.cache.cache_backend.dto.ProductoResponse;
import cl.cache.cache_backend.model.Categoria;
import cl.cache.cache_backend.model.Producto;
import cl.cache.cache_backend.repository.CategoriaRepository;
import cl.cache.cache_backend.repository.DetallePedidoRepository;
import cl.cache.cache_backend.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    ProductoRepository productoRepo;
    @Mock
    CategoriaRepository categoriaRepo;
    @Mock
    DetallePedidoRepository detalleRepo;
    @InjectMocks
    ProductoService service;

    private final Categoria mouse = Categoria.builder().id(1L).nombre("Mouse").slug("mouse").build();

    private ProductoRequest request(String codigo, int precio, Integer oferta) {
        return new ProductoRequest(codigo, "mouse", "Mouse Void Pro", "Mouse ligero", "Razer",
                precio, oferta, 10, null, null, null, null, null);
    }

    @Test
    void crear_productoValido_normalizaCodigoYStockCritico() {
        when(productoRepo.findByCodigoIgnoreCase("mou-017")).thenReturn(Optional.empty());
        when(categoriaRepo.findBySlugIgnoreCase("mouse")).thenReturn(Optional.of(mouse));
        when(productoRepo.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductoResponse r = service.crear(request("mou-017", 19990, 14990));

        assertThat(r.codigo()).isEqualTo("MOU-017");
        assertThat(r.stockCritico()).isZero();
        assertThat(r.precioFinal()).isEqualTo(14990);
        assertThat(r.enOferta()).isTrue();
        assertThat(r.categoriaNombre()).isEqualTo("Mouse");
        assertThat(r.categoria()).isEqualTo("mouse");
        assertThat(r.slug()).isEqualTo("mou-017");
    }

    @Test
    void crear_codigoRepetido_lanzaReglaNegocio() {
        when(productoRepo.findByCodigoIgnoreCase("MOU-001"))
                .thenReturn(Optional.of(Producto.builder().id(1L).codigo("MOU-001").build()));

        assertThatThrownBy(() -> service.crear(request("MOU-001", 19990, null)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("MOU-001");
        verify(productoRepo, never()).save(any());
    }

    @Test
    void crear_ofertaMayorOIgualAlPrecio_lanzaReglaNegocio() {
        when(productoRepo.findByCodigoIgnoreCase("MOU-017")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crear(request("MOU-017", 10000, 10000)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("oferta");
    }

    @Test
    void crear_categoriaInexistente_lanzaNoEncontrado() {
        when(productoRepo.findByCodigoIgnoreCase("MOU-017")).thenReturn(Optional.empty());
        when(categoriaRepo.findBySlugIgnoreCase("mouse")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crear(request("MOU-017", 10000, null)))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void actualizar_mismoCodigoDelProducto_estaPermitido() {
        Producto existente = Producto.builder().id(1L).codigo("MOU-001").categoria(mouse).build();
        when(productoRepo.findById(1L)).thenReturn(Optional.of(existente));
        when(productoRepo.findByCodigoIgnoreCase("MOU-001")).thenReturn(Optional.of(existente));
        when(categoriaRepo.findBySlugIgnoreCase("mouse")).thenReturn(Optional.of(mouse));

        ProductoResponse r = service.actualizar(1L, request("MOU-001", 20000, null));

        assertThat(r.precio()).isEqualTo(20000);
        assertThat(r.enOferta()).isFalse();
    }

    @Test
    void precioFinal_usaOfertaSoloSiEsMenor() {
        assertThat(Producto.builder().precio(100).precioOferta(80).build().precioFinal()).isEqualTo(80);
        assertThat(Producto.builder().precio(100).precioOferta(null).build().precioFinal()).isEqualTo(100);
        assertThat(Producto.builder().precio(100).precioOferta(120).build().precioFinal()).isEqualTo(100);
    }

    @Test
    void eliminar_productoConVentas_lanzaReglaNegocio() {
        Producto p = Producto.builder().id(3L).nombre("Minecraft").build();
        when(productoRepo.findById(3L)).thenReturn(Optional.of(p));
        when(detalleRepo.existsByProductoId(3L)).thenReturn(true);

        assertThatThrownBy(() -> service.eliminar(3L)).isInstanceOf(ReglaNegocioException.class);
        verify(productoRepo, never()).delete(any());
    }

    @Test
    void eliminar_productoSinVentas_loElimina() {
        Producto p = Producto.builder().id(16L).nombre("Cuphead").build();
        when(productoRepo.findById(16L)).thenReturn(Optional.of(p));
        when(detalleRepo.existsByProductoId(16L)).thenReturn(false);

        service.eliminar(16L);

        verify(productoRepo).delete(p);
    }

    @Test
    void critico_cuandoStockEsMenorOIgualAlCritico() {
        assertThat(Producto.builder().stock(3).stockCritico(5).build().critico()).isTrue();
        assertThat(Producto.builder().stock(10).stockCritico(5).build().critico()).isFalse();
    }

    @Test
    void crear_slugRepetido_lanzaReglaNegocio() {
        when(productoRepo.findByCodigoIgnoreCase("mou-017")).thenReturn(Optional.empty());
        when(productoRepo.findBySlug("mou-017"))
                .thenReturn(Optional.of(Producto.builder().id(9L).slug("mou-017").build()));

        assertThatThrownBy(() -> service.crear(request("mou-017", 10000, null)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("identificador");
        verify(productoRepo, never()).save(any());
    }

    @Test
    void obtenerPorSlug_inexistente_lanzaNoEncontrado() {
        when(productoRepo.findBySlug("mouse-99")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPorSlug("mouse-99"))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void descuentoTexto_soloCuandoHayOferta() {
        assertThat(Producto.builder().precio(100).precioOferta(75).build().descuentoTexto()).isEqualTo("25% OFF");
        assertThat(Producto.builder().precio(100).build().descuentoTexto()).isNull();
    }
}

