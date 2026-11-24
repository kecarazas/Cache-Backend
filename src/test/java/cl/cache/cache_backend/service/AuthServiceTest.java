package cl.cache.cache_backend.service;

import cl.cache.cache_backend.dto.AuthResponse;
import cl.cache.cache_backend.dto.LoginRequest;
import cl.cache.cache_backend.exception.CredencialesInvalidasException;
import cl.cache.cache_backend.model.Rol;
import cl.cache.cache_backend.model.Usuario;
import cl.cache.cache_backend.repository.UsuarioRepository;
import cl.cache.cache_backend.security.JwtService;
import io.jsonwebtoken.Claims;
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
import static org.mockito.Mockito.when;

//Prueba login y renovacion de tokens(con JwtService real)
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String SECRETO = "7p8jnchN1+Fki1QO/V8ghgRoSFitJaYeSve6qXq+iNnjfshjpNXe42hhk2nrr44Yai85gNgs0P30cRjlit5bEA==";

    @Mock
    UsuarioRepository repo;
    @Mock
    UsuarioService usuarioService;

    PasswordEncoder encoder = new BCryptPasswordEncoder();
    JwtService jwtService = new JwtService(SECRETO, 900_000, 604_800_000);
    AuthService service;
    Usuario admin;

    @BeforeEach
    void setUp() {
        service = new AuthService(repo, usuarioService, encoder, jwtService);
        admin = Usuario.builder().id(1L).run("11111111-1").nombre("Admin").apellidos("Cache")
                .correo("admin@duoc.cl").password(encoder.encode("admin123")).rol(Rol.ADMIN).build();
    }

    @Test
    void login_correcto_entregaTokensConElRol() {
        when(repo.findByCorreoIgnoreCase("admin@duoc.cl")).thenReturn(Optional.of(admin));

        AuthResponse r = service.login(new LoginRequest("admin@duoc.cl", "admin123"));

        Claims claims = jwtService.leer(r.accessToken());
        assertThat(claims.getSubject()).isEqualTo("admin@duoc.cl");
        assertThat(claims.get("rol")).isEqualTo("ADMIN");
        assertThat(claims.get(JwtService.CLAIM_TIPO)).isEqualTo(JwtService.TIPO_ACCESS);
        assertThat(r.usuario().correo()).isEqualTo("admin@duoc.cl");
        assertThat(r.refreshToken()).isNotBlank();
    }

    @Test
    void login_passwordIncorrecta_lanzaCredencialesInvalidas() {
        when(repo.findByCorreoIgnoreCase("admin@duoc.cl")).thenReturn(Optional.of(admin));
        assertThatThrownBy(() -> service.login(new LoginRequest("admin@duoc.cl", "otra")))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage(AuthService.CREDENCIALES_INVALIDAS);
    }

    @Test
    void login_correoInexistente_mismoMensaje() {
        when(repo.findByCorreoIgnoreCase("nadie@duoc.cl")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.login(new LoginRequest("nadie@duoc.cl", "x")))
                .hasMessage(AuthService.CREDENCIALES_INVALIDAS);
    }

    @Test
    void refresh_conRefreshToken_entregaTokensNuevos() {
        when(repo.findByCorreoIgnoreCase("admin@duoc.cl")).thenReturn(Optional.of(admin));
        String refresh = jwtService.generarRefreshToken(admin);

        AuthResponse r = service.refresh(refresh);

        assertThat(jwtService.leer(r.accessToken()).get("rol")).isEqualTo("ADMIN");
    }

    @Test
    void refresh_conAccessToken_esRechazado() {
        String access = jwtService.generarAccessToken(admin);
        assertThatThrownBy(() -> service.refresh(access)).isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void refresh_conTokenAlterado_esRechazado() {
        String refresh = jwtService.generarRefreshToken(admin);
        String alterado = refresh.substring(0, refresh.length() - 2) + "xx";
        assertThatThrownBy(() -> service.refresh(alterado)).isInstanceOf(CredencialesInvalidasException.class);
    }
}
