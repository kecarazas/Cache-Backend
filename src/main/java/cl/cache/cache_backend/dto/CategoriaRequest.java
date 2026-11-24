package cl.cache.cache_backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
        @Schema(example = "Mouse Gamer") @NotBlank(message = "El nombre es obligatorio") @Size(max = 50) String nombre,
        @Schema(example = "mouse", description = "Solo minúsculas, números y guiones") @NotBlank(message = "El slug es obligatorio") @Size(max = 50)
        @Pattern(regexp = "^[a-z0-9-]+$", message = "El slug solo puede tener minúsculas, números y guiones") String slug,
        @Schema(example = "Mouse para jugar") @Size(max = 200) String descripcion
) {
}
