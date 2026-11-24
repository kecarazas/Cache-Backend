package cl.cache.cache_backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ItemCompraRequest(
        @Schema(example = "mouse-1") @NotBlank(message = "El slug del producto es obligatorio") String slug,
        @Schema(example = "2") @NotNull(message = "La cantidad es obligatoria") @Min(value = 1, message = "La cantidad mínima es 1") @Max(value = 99, message = "La cantidad máxima es 99") Integer cantidad
) {
}
