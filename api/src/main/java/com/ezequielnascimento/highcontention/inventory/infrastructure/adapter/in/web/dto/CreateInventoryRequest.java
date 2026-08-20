package com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record CreateInventoryRequest(
        @NotNull UUID productId,
        @Positive int quantity
) {
}
