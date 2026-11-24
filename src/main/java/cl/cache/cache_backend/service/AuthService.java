package cl.cache.cache_backend.service;

import cl.cache.cache_backend.dto.AuthResponse;
import cl.cache.cache_backend.dto.LoginRequest;
import cl.cache.cache_backend.dto.RegistroRequest;
import cl.cache.cache_backend.dto.UsuarioResponse;
import cl.cache.cache_backend.exception.CredencialesInvalidasException;
import cl.cache.cache_backend.model.Usuario;
import cl.cache.cache_backend.repository.UsuarioRepository;
import cl.cache.cache_backend.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//Inicio de sesion, registro y renovacion de tokens
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    static final String CREDENCIALES_INVALIDAS = "Correo o contraseña incorrectos";

    private final UsuarioRepository usuarioRepo;
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest req) {
        Usuario u = usuarioRepo.findByCorreoIgnoreCase(req.correo().trim())
                .filter(usuario -> passwordEncoder.matches(req.password(), usuario.getPassword()))
                .orElseThrow(() -> new CredencialesInvalidasException(CREDENCIALES_INVALIDAS));
        return emitirTokens(u);
    }

    //Registro publico: siempre se crea un cliente e inicia sesion
    @Transactional
    public AuthResponse registro(RegistroRequest req) {
        return emitirTokens(usuarioService.crearCliente(req));
    }

    //al recargar se entrega un nuevo token
    public AuthResponse refresh(String refreshToken) {
        Claims claims;
        try {
            claims = jwtService.leer(refreshToken);
        } catch (JwtException | IllegalArgumentException e) {
            throw new CredencialesInvalidasException("Tu sesión expiró, vuelve a iniciar sesión");
        }
        //access token No sirve para renovar la sesion
        if (!JwtService.TIPO_REFRESH.equals(claims.get(JwtService.CLAIM_TIPO))) {
            throw new CredencialesInvalidasException("El token enviado no es un refresh token");
        }
        Usuario u = usuarioRepo.findByCorreoIgnoreCase(claims.getSubject())
                .orElseThrow(() -> new CredencialesInvalidasException("El usuario de la sesión ya no existe"));
        return emitirTokens(u);
    }

    public UsuarioResponse usuarioActual(String correo) {
        return usuarioRepo.findByCorreoIgnoreCase(correo)
                .map(UsuarioResponse::from)
                .orElseThrow(() -> new CredencialesInvalidasException("El usuario de la sesión ya no existe"));
    }

    private AuthResponse emitirTokens(Usuario u) {
        return new AuthResponse(
                jwtService.generarAccessToken(u),
                jwtService.generarRefreshToken(u),
                jwtService.getAccessMs(),
                UsuarioResponse.from(u));
    }
}
