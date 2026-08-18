package com.ezequielnascimento.highcontention.product.infrastructure.adapter.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ChangeProductPriceRequest(
        @NotNull @DecimalMin(value = "0", inclusive = true)BigDecimal price
        ) {
}
