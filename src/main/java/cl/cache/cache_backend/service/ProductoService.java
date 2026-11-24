package cl.cache.cache_backend.service;

import cl.cache.cache_backend.exception.RecursoNoEncontradoException;
import cl.cache.cache_backend.exception.ReglaNegocioException;
import cl.cache.cache_backend.dto.InventarioCategoriaResponse;
import cl.cache.cache_backend.dto.ProductoRequest;
import cl.cache.cache_backend.dto.ProductoResponse;
import cl.cache.cache_backend.model.Categoria;
import cl.cache.cache_backend.model.Producto;
import cl.cache.cache_backend.repository.CategoriaRepository;
import cl.cache.cache_backend.repository.DetallePedidoRepository;
import cl.cache.cache_backend.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

//Reglas de codigo unico:
//  ofertas inferiores al precio, no se eliminan productos en pedidos(afecta historial)
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductoService {

    private final ProductoRepository productoRepo;
    private final CategoriaRepository categoriaRepo;
    private final DetallePedidoRepository detalleRepo;

    //Catalogo completo o filtrado por slug de categoria
    public List<ProductoResponse> listar(String categoriaSlug) {
        List<Producto> productos = categoriaSlug == null || categoriaSlug.isBlank()
                ? productoRepo.findAllByOrderByIdAsc()
                : productoRepo.findByCategoriaSlugIgnoreCaseOrderByIdAsc(categoriaSlug.trim());
        return productos.stream().map(ProductoResponse::from).toList();
    }

    public ProductoResponse obtener(Long id) {
        return ProductoResponse.from(buscar(id));
    }

    //Detalle por el slug usado por el frontend en la URL
    public ProductoResponse obtenerPorSlug(String slug) {
        return ProductoResponse.from(productoRepo.findBySlug(slug)
                .orElseThrow(() -> new RecursoNoEncontradoException("El producto " + slug + " no existe")));
    }

    public List<ProductoResponse> destacados() {
        return productoRepo.findByDestacadoTrueOrderByIdAsc().stream().map(ProductoResponse::from).toList();
    }

    public List<ProductoResponse> ofertas() {
        return productoRepo.findEnOferta().stream().map(ProductoResponse::from).toList();
    }

    public List<ProductoResponse> criticos() {
        return productoRepo.findCriticos().stream().map(ProductoResponse::from).toList();
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest req) {
        validarCodigoUnico(req.codigo(), null);
        Producto p = new Producto();
        // los productos creados desde el panel usan su código como slug (en minúsculas)
        String slug = req.codigo().trim().toLowerCase();
        if (productoRepo.findBySlug(slug).isPresent()) {
            throw new ReglaNegocioException("Ya existe un producto con el identificador " + slug);
        }
        p.setSlug(slug);
        copiar(req, p);
        return ProductoResponse.from(productoRepo.save(p));
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest req) {
        Producto p = buscar(id);
        validarCodigoUnico(req.codigo(), id);
        copiar(req, p);
        return ProductoResponse.from(p);
    }

    @Transactional
    public void eliminar(Long id) {
        Producto p = buscar(id);
        if (detalleRepo.existsByProductoId(id)) {
            throw new ReglaNegocioException("No se puede eliminar \"" + p.getNombre() + "\" porque aparece en pedidos de venta");
        }
        productoRepo.delete(p);
    }

    //Reporte de valor y unidad del inventario
    public List<InventarioCategoriaResponse> inventarioPorCategoria() {
        return categoriaRepo.findAllByOrderByIdAsc().stream().map(c -> {
            List<Producto> productos = productoRepo.findByCategoriaIdOrderByIdAsc(c.getId());
            long unidades = productos.stream().mapToLong(Producto::getStock).sum();
            long valor = productos.stream().mapToLong(p -> (long) p.getStock() * p.getPrecio()).sum();
            return new InventarioCategoriaResponse(c.getNombre(), productos.size(), unidades, valor);
        }).toList();
    }

    Producto buscar(Long id) {
        return productoRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El producto " + id + " no existe"));
    }

    private void validarCodigoUnico(String codigo, Long idActual) {
        productoRepo.findByCodigoIgnoreCase(codigo.trim())
                .filter(otro -> !otro.getId().equals(idActual))
                .ifPresent(otro -> {
                    throw new ReglaNegocioException("Ya existe un producto con el código " + otro.getCodigo());
                });
    }

    private void copiar(ProductoRequest req, Producto p) {
        if (req.precioOferta() != null && req.precioOferta() > 0 && req.precioOferta() >= req.precio()) {
            throw new ReglaNegocioException("El precio de oferta debe ser menor al precio normal");
        }
        Categoria categoria = categoriaRepo.findBySlugIgnoreCase(req.categoria().trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("La categoría " + req.categoria() + " no existe"));
        p.setCodigo(req.codigo().trim().toUpperCase());
        p.setNombre(req.nombre().trim());
        p.setDescripcion(req.descripcion());
        p.setMarca(req.marca() == null || req.marca().isBlank() ? null : req.marca().trim());
        p.setEtiqueta(req.etiqueta() == null || req.etiqueta().isBlank() ? null : req.etiqueta().trim());
        p.setDestacado(Boolean.TRUE.equals(req.destacado()));
        p.setPrecio(req.precio());
        p.setPrecioOferta(req.precioOferta() == null || req.precioOferta() == 0 ? null : req.precioOferta());
        p.setStock(req.stock());
        p.setStockCritico(req.stockCritico() == null ? 0 : req.stockCritico());
        p.setImagen(req.imagen() == null || req.imagen().isBlank() ? null : req.imagen().trim());
        p.getEspecificaciones().clear();
        if (req.especificaciones() != null) {
            req.especificaciones().stream().filter(e -> e != null && !e.isBlank()).map(String::trim)
                    .forEach(p.getEspecificaciones()::add);
        }
        p.setCategoria(categoria);
    }
}
