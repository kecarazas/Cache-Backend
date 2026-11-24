package cl.cache.cache_backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CompraRequest(
        @Schema(example = "Pedro Hacker") @NotBlank(message = "El nombre es obligatorio") @Size(max = 150) String nombre,
        @Schema(example = "pedro@gmail.com") @NotBlank(message = "El correo es obligatorio") @Email(message = "El correo no tiene un formato válido") @Size(max = 100) String correo,
        @Schema(example = "+56 9 1234 5678") @NotBlank(message = "El teléfono es obligatorio") @Pattern(regexp = "^[0-9+ ()-]{8,20}$", message = "El teléfono no tiene un formato válido") String telefono,
        @Schema(example = "Región Metropolitana") @NotBlank(message = "La región es obligatoria") @Size(max = 100) String region,
        @Schema(example = "Maipú") @NotBlank(message = "La comuna es obligatoria") @Size(max = 100) String comuna,
        @Schema(example = "Los Crisantemos 123, depto 603") @NotBlank(message = "La dirección es obligatoria") @Size(max = 300) String direccion,
        @Schema(example = "CACHE10", description = "Opcional: CACHE10 (10% de descuento) o ENVIOGRATIS") @Size(max = 30) String cupon,
        @NotEmpty(message = "El carrito está vacío") @Size(max = 50, message = "Máximo 50 productos distintos por compra") List<@Valid ItemCompraRequest> items
) {
}
