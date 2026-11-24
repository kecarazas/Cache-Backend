package cl.cache.cache_backend.service;

import cl.cache.cache_backend.exception.ReglaNegocioException;
import cl.cache.cache_backend.dto.CategoriaRequest;
import cl.cache.cache_backend.model.Categoria;
import cl.cache.cache_backend.repository.CategoriaRepository;
import cl.cache.cache_backend.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    CategoriaRepository categoriaRepo;
    @Mock
    ProductoRepository productoRepo;
    @InjectMocks
    CategoriaService service;

    @Test
    void eliminar_conProductos_lanzaReglaNegocio() {
        when(categoriaRepo.findById(1L)).thenReturn(Optional.of(Categoria.builder().id(1L).nombre("Indie").build()));
        when(productoRepo.countByCategoriaId(1L)).thenReturn(4L);

        assertThatThrownBy(() -> service.eliminar(1L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("4 producto");
        verify(categoriaRepo, never()).delete(any());
    }

    @Test
    void eliminar_sinProductos_laElimina() {
        Categoria vacia = Categoria.builder().id(7L).nombre("Terror").build();
        when(categoriaRepo.findById(7L)).thenReturn(Optional.of(vacia));
        when(productoRepo.countByCategoriaId(7L)).thenReturn(0L);

        service.eliminar(7L);

        verify(categoriaRepo).delete(vacia);
    }

    @Test
    void crear_nombreRepetido_lanzaReglaNegocio() {
        when(categoriaRepo.findByNombreIgnoreCase("indie"))
                .thenReturn(Optional.of(Categoria.builder().id(4L).nombre("Indie").build()));

        assertThatThrownBy(() -> service.crear(new CategoriaRequest("indie", "indie", null)))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void crear_slugRepetido_lanzaReglaNegocio() {
        when(categoriaRepo.findByNombreIgnoreCase("Otra")).thenReturn(Optional.empty());
        when(categoriaRepo.findBySlugIgnoreCase("mouse"))
                .thenReturn(Optional.of(Categoria.builder().id(1L).nombre("Mouse").slug("mouse").build()));

        assertThatThrownBy(() -> service.crear(new CategoriaRequest("Otra", "mouse", null)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("slug");
        verify(categoriaRepo, never()).save(any());
    }
}
