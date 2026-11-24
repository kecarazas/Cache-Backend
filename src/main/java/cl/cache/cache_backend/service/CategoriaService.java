package cl.cache.cache_backend.service;

import cl.cache.cache_backend.exception.RecursoNoEncontradoException;
import cl.cache.cache_backend.exception.ReglaNegocioException;
import cl.cache.cache_backend.dto.CategoriaRequest;
import cl.cache.cache_backend.dto.CategoriaResponse;
import cl.cache.cache_backend.model.Categoria;
import cl.cache.cache_backend.repository.CategoriaRepository;
import cl.cache.cache_backend.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

//Reglas: nombre y slug de categoria unicos
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoriaService {

    private final CategoriaRepository categoriaRepo;
    private final ProductoRepository productoRepo;

    public List<CategoriaResponse> listar() {
        return categoriaRepo.findAllByOrderByIdAsc().stream()
                .map(c -> CategoriaResponse.from(c, productoRepo.countByCategoriaId(c.getId())))
                .toList();
    }

    public CategoriaResponse obtener(Long id) {
        Categoria c = buscar(id);
        return CategoriaResponse.from(c, productoRepo.countByCategoriaId(id));
    }

    @Transactional
    public CategoriaResponse crear(CategoriaRequest req) {
        validarNombreUnico(req.nombre(), null);
        validarSlugUnico(req.slug(), null);
        Categoria c = new Categoria();
        copiar(req, c);
        return CategoriaResponse.from(categoriaRepo.save(c), 0);
    }

    @Transactional
    public CategoriaResponse actualizar(Long id, CategoriaRequest req) {
        Categoria c = buscar(id);
        validarNombreUnico(req.nombre(), id);
        validarSlugUnico(req.slug(), id);
        copiar(req, c);
        return CategoriaResponse.from(c, productoRepo.countByCategoriaId(id));
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria c = buscar(id);
        long productos = productoRepo.countByCategoriaId(id);
        if (productos > 0) {
            throw new ReglaNegocioException("No se puede eliminar la categoría \"" + c.getNombre()
                    + "\" porque tiene " + productos + " producto(s) asociados");
        }
        categoriaRepo.delete(c);
    }

    Categoria buscar(Long id) {
        return categoriaRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("La categoría " + id + " no existe"));
    }

    private void validarNombreUnico(String nombre, Long idActual) {
        categoriaRepo.findByNombreIgnoreCase(nombre.trim())
                .filter(otra -> !otra.getId().equals(idActual))
                .ifPresent(otra -> {
                    throw new ReglaNegocioException("Ya existe la categoría " + otra.getNombre());
                });
    }

    private void validarSlugUnico(String slug, Long idActual) {
        categoriaRepo.findBySlugIgnoreCase(slug.trim())
                .filter(otra -> !otra.getId().equals(idActual))
                .ifPresent(otra -> {
                    throw new ReglaNegocioException("Ya existe una categoría con el slug " + otra.getSlug());
                });
    }

    private void copiar(CategoriaRequest req, Categoria c) {
        c.setNombre(req.nombre().trim());
        c.setSlug(req.slug().trim().toLowerCase());
        c.setDescripcion(req.descripcion() == null || req.descripcion().isBlank() ? null : req.descripcion().trim());
    }
}
