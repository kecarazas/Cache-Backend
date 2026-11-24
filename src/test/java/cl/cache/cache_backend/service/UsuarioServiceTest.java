package cl.cache.cache_backend.service;

import cl.cache.cache_backend.exception.ReglaNegocioException;
import cl.cache.cache_backend.exception.DatosInvalidosException;
import cl.cache.cache_backend.dto.PerfilRequest;
import cl.cache.cache_backend.dto.RegistroRequest;
import cl.cache.cache_backend.dto.UsuarioRequest;
import cl.cache.cache_backend.dto.UsuarioResponse;
import cl.cache.cache_backend.model.Rol;
import cl.cache.cache_backend.model.Usuario;
import cl.cache.cache_backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    UsuarioRepository repo;

    PasswordEncoder encoder = new BCryptPasswordEncoder();
    UsuarioService service;

    @BeforeEach
    void setUp() {
        service = new UsuarioService(repo, encoder);
    }

    private RegistroRequest registro(String run, String correo) {
        return new RegistroRequest(run, "Ana", "Pérez", correo, "ana123", null,
                "Región Metropolitana", "Ñuñoa", "Calle 1", null, null);
    }

    @Test
    void crearCliente_encriptaPasswordYAsignaRolCliente() {
        when(repo.findByRun("218765432")).thenReturn(Optional.empty());
        when(repo.findByCorreoIgnoreCase("ana@gmail.com")).thenReturn(Optional.empty());
        when(repo.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario creado = service.crearCliente(registro("21.876.543-2", "ANA@gmail.com"));

        assertThat(creado.getRol()).isEqualTo(Rol.CLIENTE);
        assertThat(creado.getRun()).isEqualTo("218765432");
        assertThat(creado.getCorreo()).isEqualTo("ana@gmail.com");
        assertThat(creado.getTelefono()).isNull();
        assertThat(creado.getPassword()).isNotEqualTo("ana123");
        assertThat(encoder.matches("ana123", creado.getPassword())).isTrue();
    }

    @Test
    void crearCliente_runInvalido_lanzaSolicitudInvalida() {
        assertThatThrownBy(() -> service.crearCliente(registro("123456789", "ana@gmail.com")))
                .isInstanceOf(DatosInvalidosException.class);
        verify(repo, never()).save(any());
    }

    @Test
    void crearCliente_correoRepetido_lanzaReglaNegocio() {
        Usuario existente = Usuario.builder().id(5L).correo("ana@gmail.com").build();
        when(repo.findByRun("218765432")).thenReturn(Optional.empty());
        when(repo.findByCorreoIgnoreCase("ana@gmail.com")).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.crearCliente(registro("218765432", "ana@gmail.com")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("correo");
    }

    @Test
    void crearCliente_dominioNoPermitido_lanzaSolicitudInvalida() {
        when(repo.findByRun("218765432")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.crearCliente(registro("218765432", "ana@hotmail.com")))
                .isInstanceOf(DatosInvalidosException.class);
    }

    @Test
    void actualizar_conPasswordVacia_mantieneLaAnterior() {
        Usuario u = Usuario.builder().id(3L).run("98765433").correo("camila@duoc.cl")
                .password("hash-anterior").rol(Rol.VENDEDOR).build();
        when(repo.findById(3L)).thenReturn(Optional.of(u));
        when(repo.findByRun("98765433")).thenReturn(Optional.of(u));
        when(repo.findByCorreoIgnoreCase("camila@duoc.cl")).thenReturn(Optional.of(u));

        UsuarioRequest req = new UsuarioRequest("98765433", "Camila", "Rojas", "camila@duoc.cl", "",
                Rol.ADMIN, null, "Región de Valparaíso", "Viña del Mar", "Calle 5 Norte 123", null, null);
        UsuarioResponse r = service.actualizar(3L, req);

        assertThat(u.getPassword()).isEqualTo("hash-anterior");
        assertThat(r.tipoUsuario()).isEqualTo("administrador");
    }

    @Test
    void eliminar_propiaCuenta_lanzaReglaNegocio() {
        when(repo.findById(1L)).thenReturn(Optional.of(Usuario.builder().id(1L).correo("admin@duoc.cl").build()));
        assertThatThrownBy(() -> service.eliminar(1L, "admin@duoc.cl")).isInstanceOf(ReglaNegocioException.class);
        verify(repo, never()).delete(any());
    }

    @Test
    void actualizarPerfil_intentaCambiarCorreo_lanzaDatosInvalidos() {
        Usuario pedro = Usuario.builder().id(2L).run("123456785").correo("pedro@gmail.com").rol(Rol.CLIENTE).build();
        when(repo.findByCorreoIgnoreCase("pedro@gmail.com")).thenReturn(Optional.of(pedro));

        PerfilRequest req = new PerfilRequest("123456785", "Pedro", "Hacker", "otro@gmail.com", "", null,
                "Región Metropolitana", "Maipú", "Calle 1", null, null);

        assertThatThrownBy(() -> service.actualizarPerfil("pedro@gmail.com", req))
                .isInstanceOf(DatosInvalidosException.class);
    }
}
