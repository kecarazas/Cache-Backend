package cl.cache.cache_backend.service;

import cl.cache.cache_backend.exception.RecursoNoEncontradoException;
import cl.cache.cache_backend.exception.ReglaNegocioException;
import cl.cache.cache_backend.exception.DatosInvalidosException;
import cl.cache.cache_backend.dto.PerfilRequest;
import cl.cache.cache_backend.dto.RegistroRequest;
import cl.cache.cache_backend.dto.UsuarioRequest;
import cl.cache.cache_backend.dto.UsuarioResponse;
import cl.cache.cache_backend.model.Rol;
import cl.cache.cache_backend.model.Usuario;
import cl.cache.cache_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

//Maneja la logica de los usuarios (run, correo, dominio, contraseña)
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository repo;
    private final PasswordEncoder passwordEncoder;

    public List<UsuarioResponse> listar() {
        return repo.findAllByOrderByIdAsc().stream().map(UsuarioResponse::from).toList();
    }

    public UsuarioResponse obtener(Long id) {
        return UsuarioResponse.from(buscar(id));
    }

    private Usuario buscarPorCorreo(String correo) {
        return repo.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    public Usuario buscar(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El usuario " + id + " no existe"));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest req) {
        Usuario u = new Usuario();
        aplicarDatos(u, req.run(), req.nombre(), req.apellidos(), req.correo(), req.telefono(), req.region(), req.comuna(),
                req.direccion(), req.departamento(), req.fechaNacimiento());
        u.setRol(req.tipoUsuario());
        u.setPassword(encriptarObligatoria(req.password()));
        return UsuarioResponse.from(repo.save(u));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioRequest req) {
        Usuario u = buscar(id);
        aplicarDatos(u, req.run(), req.nombre(), req.apellidos(), req.correo(), req.telefono(), req.region(), req.comuna(),
                req.direccion(), req.departamento(), req.fechaNacimiento());
        u.setRol(req.tipoUsuario());
        actualizarPasswordSiViene(u, req.password());
        return UsuarioResponse.from(u); // dentro de @Transactional los cambios se guardan solos
    }

    @Transactional
    public void eliminar(Long id, String correoUsuarioActual) {
        Usuario u = buscar(id);
        if (u.getCorreo().equalsIgnoreCase(correoUsuarioActual)) {
            throw new ReglaNegocioException("No puedes eliminar tu propia cuenta");
        }
        repo.delete(u);
    }

    //El rol siempre es cliente en registro publico
    @Transactional
    public Usuario crearCliente(RegistroRequest req) {
        Usuario u = new Usuario();
        aplicarDatos(u, req.run(), req.nombre(), req.apellidos(), req.correo(), req.telefono(), req.region(), req.comuna(),
                req.direccion(), req.departamento(), req.fechaNacimiento());
        u.setRol(Rol.CLIENTE);
        u.setPassword(encriptarObligatoria(req.password()));
        return repo.save(u);
    }

    //Perfil del usuario que inicia sesion(El token lo verifica por su correo)
    public UsuarioResponse perfil(String correo) {
        return UsuarioResponse.from(buscarPorCorreo(correo));
    }

    @Transactional
    public UsuarioResponse actualizarPerfil(String correo, PerfilRequest req) {
        Usuario u = buscarPorCorreo(correo);
        // el correo identifica la sesión (va dentro del token): no se cambia desde el perfil
        if (!u.getCorreo().equalsIgnoreCase(req.correo().trim())) {
            throw new DatosInvalidosException("El correo no se puede cambiar desde el perfil");
        }
        aplicarDatos(u, req.run(), req.nombre(), req.apellidos(), req.correo(), req.telefono(), req.region(), req.comuna(),
                req.direccion(), req.departamento(), req.fechaNacimiento());
        actualizarPasswordSiViene(u, req.password());
        return UsuarioResponse.from(u);
    }

    //reglas

    private void aplicarDatos(Usuario u, String run, String nombre, String apellidos, String correo,
                              String telefono, String region, String comuna, String direccion,
                              String departamento, String fechaNacimiento) {
        String runNormalizado = ValidadorUsuario.normalizarRun(run);
        if (!ValidadorUsuario.runValido(runNormalizado)) {
            throw new DatosInvalidosException("El RUN no es válido. Usa el formato 123456785 (sin puntos ni guion, con dígito verificador)");
        }
        repo.findByRun(runNormalizado)
                .filter(otro -> !otro.getId().equals(u.getId()))
                .ifPresent(otro -> {
                    throw new ReglaNegocioException("Ya existe un usuario con el RUN " + runNormalizado);
                });

        String correoNormalizado = correo.trim().toLowerCase();
        if (!ValidadorUsuario.dominioPermitido(correoNormalizado)) {
            throw new DatosInvalidosException("Solo se permiten correos @duoc.cl, @profesor.duoc.cl o @gmail.com");
        }
        repo.findByCorreoIgnoreCase(correoNormalizado)
                .filter(otro -> !otro.getId().equals(u.getId()))
                .ifPresent(otro -> {
                    throw new ReglaNegocioException("Ya existe un usuario con el correo " + correoNormalizado);
                });

        u.setRun(runNormalizado);
        u.setCorreo(correoNormalizado);
        u.setNombre(nombre.trim());
        u.setApellidos(apellidos.trim());
        u.setTelefono(vacioANull(telefono));
        u.setRegion(region);
        u.setComuna(comuna);
        u.setDireccion(direccion.trim());
        u.setDepartamento(vacioANull(departamento));
        u.setFechaNacimiento(parsearFecha(fechaNacimiento));
    }

    private String encriptarObligatoria(String password) {
        if (password == null || password.length() < 4 || password.length() > 10) {
            throw new DatosInvalidosException("La contraseña debe tener entre 4 y 10 caracteres");
        }
        return passwordEncoder.encode(password);
    }

    private void actualizarPasswordSiViene(Usuario u, String password) {
        if (password != null && !password.isBlank()) {
            u.setPassword(encriptarObligatoria(password));
        }
    }

    private static LocalDate parsearFecha(String fecha) {
        if (fecha == null || fecha.isBlank()) {
            return null;
        }
        try {
            LocalDate f = LocalDate.parse(fecha);
            if (f.isAfter(LocalDate.now())) {
                throw new DatosInvalidosException("La fecha de nacimiento no puede ser futura");
            }
            return f;
        } catch (DateTimeParseException e) {
            throw new DatosInvalidosException("La fecha de nacimiento debe tener el formato AAAA-MM-DD");
        }
    }

    private static String vacioANull(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
