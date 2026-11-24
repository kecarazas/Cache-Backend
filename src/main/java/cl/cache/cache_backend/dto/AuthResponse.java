package cl.cache.cache_backend.dto;

public record AuthResponse(String accessToken, String refreshToken, long expiraEnMs, UsuarioResponse usuario) {
}
