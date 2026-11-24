package cl.cache.cache_backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ProductoRequest(
        @Schema(example = "MOU-010") @NotBlank(message = "El código es obligatorio") @Size(min = 3, max = 10, message = "El código debe tener entre 3 y 10 caracteres") String codigo,
        @Schema(example = "mouse", description = "Slug de la categoría") @NotBlank(message = "La categoría es obligatoria") String categoria,
        @Schema(example = "Mouse Gamer Void Pro") @NotBlank(message = "El nombre es obligatorio") @Size(max = 150, message = "El nombre no puede superar los 150 caracteres") String nombre,
        @Schema(example = "Mouse ligero con sensor óptico de alta precisión") @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres") String descripcion,
        @Schema(example = "Razer") @Size(max = 50) String marca,
        @Schema(example = "89990") @NotNull(message = "El precio es obligatorio") @Min(value = 0, message = "El precio no puede ser negativo") Integer precio,
        @Schema(example = "74990", description = "Opcional; debe ser menor al precio") @Min(value = 0, message = "El precio de oferta no puede ser negativo") Integer precioOferta,
        @Schema(example = "12") @NotNull(message = "El stock es obligatorio") @Min(value = 0, message = "El stock no puede ser negativo") Integer stock,
        @Schema(example = "3", description = "Opcional; 0 si no se envía") @Min(value = 0, message = "El stock crítico no puede ser negativo") Integer stockCritico,
        @Schema(example = "mouse/mouse4.png", description = "Opcional; ruta dentro de public/images del frontend") @Size(max = 500) String imagen,
        @Schema(example = "Nuevo!", description = "Opcional; insignia de la tarjeta") @Size(max = 40) String etiqueta,
        @Schema(description = "Opcional; false si no se envía") Boolean destacado,
        @Schema(description = "Opcional; una línea por característica") List<@Size(max = 300) String> especificaciones
) {
}
