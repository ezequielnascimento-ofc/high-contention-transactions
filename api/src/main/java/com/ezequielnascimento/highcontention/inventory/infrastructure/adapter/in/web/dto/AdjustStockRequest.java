package com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

public record AdjustStockRequest(@Schema(example = "1", description = "Units to add or remove, greater than zero") @Positive int quantity) {
}
