package com.ezequielnascimento.highcontention.product.infrastructure.adapter.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateProductRequest(
        @Schema(example = "Mechanical Keyboard") @NotBlank String name,
        @Schema(example = "Hot-swappable switches, 75% layout") String description,
        @Schema(example = "499.90") @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal price
) {
}
