package cl.cache.cache_backend.config;

import cl.cache.cache_backend.model.Categoria;
import cl.cache.cache_backend.model.DetallePedido;
import cl.cache.cache_backend.model.EstadoPedido;
import cl.cache.cache_backend.model.Pedido;
import cl.cache.cache_backend.model.Producto;
import cl.cache.cache_backend.model.Rol;
import cl.cache.cache_backend.model.Usuario;
import cl.cache.cache_backend.repository.CategoriaRepository;
import cl.cache.cache_backend.repository.PedidoRepository;
import cl.cache.cache_backend.repository.ProductoRepository;
import cl.cache.cache_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final UsuarioRepository usuarioRepo;
    private final CategoriaRepository categoriaRepo;
    private final ProductoRepository productoRepo;
    private final PedidoRepository pedidoRepo;
    private final PasswordEncoder encoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepo.count() > 0) {
            return;
        }

        // ---------- Usuarios (los mismos del frontend: admin y vendedor, más un cliente) ----------
        usuarioRepo.save(Usuario.builder()
                .run("111111111").nombre("Administrador").apellidos("Cache")
                .correo("admin@cache.cl").password(encoder.encode("admin123")).rol(Rol.ADMIN)
                .region("Región Metropolitana").comuna("Santiago")
                .direccion("Av. Siempre Viva 123").fechaNacimiento(LocalDate.of(1990, 1, 1))
                .build());
        usuarioRepo.save(Usuario.builder()
                .run("222222222").nombre("Vendedor").apellidos("Cache")
                .correo("vendedor@cache.cl").password(encoder.encode("vendedor123")).rol(Rol.VENDEDOR)
                .region("Región Metropolitana").comuna("Maipú")
                .direccion("Calle Falsa 456").fechaNacimiento(LocalDate.of(1995, 5, 5))
                .build());
        Usuario cliente = usuarioRepo.save(Usuario.builder()
                .run("123456785").nombre("Pedro").apellidos("Hacker").telefono("+56 9 1234 5678")
                .correo("pedro@gmail.com").password(encoder.encode("pedro123")).rol(Rol.CLIENTE)
                .region("Región Metropolitana").comuna("Maipú")
                .direccion("Los Crisantemos 123").departamento("Depto 603")
                .fechaNacimiento(LocalDate.of(2001, 5, 20))
                .build());

        // ---------- Categorías (mismos slugs y nombres que usa el menú del frontend) ----------
        Map<String, Categoria> categorias = new HashMap<>();
        categoria(categorias, "mouse", "Mouse", "Mouse gamer de alto rendimiento.");
        categoria(categorias, "mousepad", "Mousepads", "Superficies para tu mouse.");
        categoria(categorias, "teclado", "Teclados", "Teclados mecánicos y gamer.");
        categoria(categorias, "skate", "Skates", "Patines y grip para tu mouse.");
        categoria(categorias, "audifonos", "Audífonos", "Audífonos gamer con micrófono.");
        categoria(categorias, "mangas", "Mangas", "Tomos en formato físico.");

        // ---------- Productos ----------
        List<SeedProductos.Semilla> semillas = SeedProductos.TODOS;
        Map<String, Producto> productos = new HashMap<>();
        for (int i = 0; i < semillas.size(); i++) {
            SeedProductos.Semilla s = semillas.get(i);
            int stock = 5 + (i * 7) % 26; // entre 5 y 30 unidades; los de 5 quedan como stock crítico
            Producto p = productoRepo.save(Producto.builder()
                    .slug(s.slug()).codigo(s.codigo()).nombre(s.nombre()).descripcion(s.descripcion())
                    .marca(s.marca()).etiqueta(s.etiqueta()).precio(s.precio())
                    .stock(stock).stockCritico(5)
                    .imagen(s.imagen()).opiniones(s.opiniones())
                    .especificaciones(new ArrayList<>(s.especificaciones()))
                    .categoria(categorias.get(s.categoria()))
                    .build());
            productos.put(s.slug(), p);
        }

        // ---------- Pedidos históricos (ventas pasadas: NO descuentan stock) ----------
        Pedido p1 = pedido(cliente, "Pedro Hacker", "pedro@gmail.com", "+56 9 1234 5678",
                "Región Metropolitana", "Maipú", "Los Crisantemos 123, Depto 603",
                EstadoPedido.ENTREGADO, LocalDateTime.now().minusDays(8));
        p1.agregarDetalle(new DetallePedido(productos.get("mouse-1"), 1, productos.get("mouse-1").precioFinal()));
        p1.agregarDetalle(new DetallePedido(productos.get("mousepad-1"), 2, productos.get("mousepad-1").precioFinal()));
        guardar(p1);

        Pedido p2 = pedido(null, "Javiera Soto", "javi.soto@gmail.com", "+56 9 8765 4321",
                "Región de Valparaíso", "Viña del Mar", "Av. Libertad 1020",
                EstadoPedido.RECIBIDO, LocalDateTime.now().minusDays(2));
        p2.agregarDetalle(new DetallePedido(productos.get("mangas-7"), 3, productos.get("mangas-7").precioFinal()));
        guardar(p2);

        log.info("Datos de prueba creados: {} productos. Usuarios: admin@cache.cl/admin123, "
                + "vendedor@cache.cl/vendedor123, pedro@gmail.com/pedro123", productos.size());
    }

    private void categoria(Map<String, Categoria> mapa, String slug, String nombre, String descripcion) {
        mapa.put(slug, categoriaRepo.save(Categoria.builder().slug(slug).nombre(nombre).descripcion(descripcion).build()));
    }

    private void guardar(Pedido p) {
        int subtotal = p.getDetalles().stream().mapToInt(DetallePedido::getSubtotal).sum();
        int envio = subtotal >= 20000 ? 0 : 2990;
        p.setSubtotal(subtotal);
        p.setDescuento(0);
        p.setEnvio(envio);
        p.setTotal(subtotal + envio);
        pedidoRepo.save(p);
    }

    private static Pedido pedido(Usuario usuario, String nombre, String correo, String telefono, String region,
                                 String comuna, String direccion, EstadoPedido estado, LocalDateTime fecha) {
        Pedido p = new Pedido();
        p.setUsuario(usuario);
        p.setClienteNombre(nombre);
        p.setClienteCorreo(correo);
        p.setClienteTelefono(telefono);
        p.setRegion(region);
        p.setComuna(comuna);
        p.setDireccion(direccion);
        p.setEstado(estado);
        p.setFecha(fecha);
        return p;
    }
}
