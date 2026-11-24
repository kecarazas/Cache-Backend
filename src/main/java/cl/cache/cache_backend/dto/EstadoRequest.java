package cl.cache.cache_backend.dto;

import cl.cache.cache_backend.model.EstadoPedido;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record EstadoRequest(@Schema(example = "ENVIADO") @NotNull(message = "El estado es obligatorio") EstadoPedido estado) {
}
