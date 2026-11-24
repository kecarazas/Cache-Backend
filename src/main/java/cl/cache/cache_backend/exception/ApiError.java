package cl.cache.cache_backend.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiError(int status, String mensaje, Map<String, String> errores, String fecha) {

    public static ApiError de(int status, String mensaje, Map<String, String> errores) {
        return new ApiError(status, mensaje, errores, LocalDateTime.now().toString());
    }
}
