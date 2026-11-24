package cl.cache.cache_backend.security;

import cl.cache.cache_backend.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    public static final String CLAIM_TIPO = "tipo";
    public static final String TIPO_ACCESS = "access";
    public static final String TIPO_REFRESH = "refresh";

    private final SecretKey clave;
    private final long accessMs;
    private final long refreshMs;

    public JwtService(@Value("${jwt.secret}") String secretoBase64,
                      @Value("${jwt.access-expiration-ms}") long accessMs,
                      @Value("${jwt.refresh-expiration-ms}") long refreshMs) {
        this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretoBase64));
        this.accessMs = accessMs;
        this.refreshMs = refreshMs;
    }

    public String generarAccessToken(Usuario u) {
        return construir(u.getCorreo(),
                Map.of(CLAIM_TIPO, TIPO_ACCESS, "rol", u.getRol().name(), "nombre", u.getNombre()),
                accessMs);
    }

    public String generarRefreshToken(Usuario u) {
        return construir(u.getCorreo(), Map.of(CLAIM_TIPO, TIPO_REFRESH), refreshMs);
    }

    public Claims leer(String token) {
        return Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
    }

    public long getAccessMs() {
        return accessMs;
    }

    private String construir(String correo, Map<String, ?> claims, long duracionMs) {
        Date ahora = new Date();
        return Jwts.builder()
                .claims(claims)
                .subject(correo)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + duracionMs))
                .signWith(clave)
                .compact();
    }
}
