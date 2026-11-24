package cl.cache.cache_backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(example = "admin@cache.cl") @NotBlank(message = "El correo es obligatorio") @Email(message = "El correo no tiene un formato válido") String correo,
        @Schema(example = "admin123") @JsonAlias("clave") @NotBlank(message = "La contraseña es obligatoria") String password
) {
}
