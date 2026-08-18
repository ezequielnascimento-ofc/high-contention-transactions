package com.ezequielnascimento.highcontention.product.infrastructure.adapter.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank String name,
        String description,
        @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal price
) {
}
