package cl.cache.cache_backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import cl.cache.cache_backend.model.Rol;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @Schema(example = "98765433") @NotBlank(message = "El RUN es obligatorio") String run,
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 50) String nombre,
        @NotBlank(message = "Los apellidos son obligatorios") @Size(max = 100) String apellidos,
        @NotBlank(message = "El correo es obligatorio") @Email(message = "El correo no tiene un formato válido") @Size(max = 100) String correo,
        @Schema(description = "Obligatoria al crear; vacía al editar = no se cambia") @JsonAlias("clave") @Size(max = 10, message = "La contraseña debe tener entre 4 y 10 caracteres") String password,
        @Schema(example = "vendedor", description = "administrador, vendedor o cliente") @NotNull(message = "El tipo de usuario es obligatorio") Rol tipoUsuario,
        @Pattern(regexp = "^$|^[0-9+ ()-]{8,20}$", message = "El teléfono no tiene un formato válido") String telefono,
        @NotBlank(message = "La región es obligatoria") String region,
        @NotBlank(message = "La comuna es obligatoria") String comuna,
        @NotBlank(message = "La dirección es obligatoria") @Size(max = 300) String direccion,
        @Size(max = 50) String departamento,
        @Schema(example = "1998-11-02", description = "Formato AAAA-MM-DD, opcional") String fechaNacimiento
) {
}
