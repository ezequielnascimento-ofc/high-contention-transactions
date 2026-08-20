package com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Positive;

public record AdjustStockRequest(@Positive int quantity) {
}
