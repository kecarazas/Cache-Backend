package cl.cache.cache_backend.repository;

import cl.cache.cache_backend.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findByNombreIgnoreCase(String nombre);

    Optional<Categoria> findBySlugIgnoreCase(String slug);

    List<Categoria> findAllByOrderByIdAsc();
}
