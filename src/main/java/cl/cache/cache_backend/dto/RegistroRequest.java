package cl.cache.cache_backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @Schema(example = "218765432") @NotBlank(message = "El RUN es obligatorio") String run,
        @Schema(example = "Ana") @NotBlank(message = "El nombre es obligatorio") @Size(max = 50) String nombre,
        @Schema(example = "Pérez Soto") @NotBlank(message = "Los apellidos son obligatorios") @Size(max = 100) String apellidos,
        @Schema(example = "ana@gmail.com") @NotBlank(message = "El correo es obligatorio") @Email(message = "El correo no tiene un formato válido") @Size(max = 100) String correo,
        @Schema(example = "ana123") @JsonAlias("clave") @NotBlank(message = "La contraseña es obligatoria") @Size(min = 4, max = 10, message = "La contraseña debe tener entre 4 y 10 caracteres") String password,
        @Schema(example = "+56 9 1234 5678") @Pattern(regexp = "^$|^[0-9+ ()-]{8,20}$", message = "El teléfono no tiene un formato válido") String telefono,
        @Schema(example = "Región Metropolitana") @NotBlank(message = "La región es obligatoria") String region,
        @Schema(example = "Maipú") @NotBlank(message = "La comuna es obligatoria") String comuna,
        @Schema(example = "Av. Pajaritos 1234") @NotBlank(message = "La dirección es obligatoria") @Size(max = 300) String direccion,
        @Size(max = 50) String departamento,
        @Schema(example = "2003-04-15", description = "Formato AAAA-MM-DD, opcional") String fechaNacimiento
) {
}
