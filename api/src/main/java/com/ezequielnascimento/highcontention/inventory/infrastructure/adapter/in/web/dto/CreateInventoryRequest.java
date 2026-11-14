package com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record CreateInventoryRequest(
        @Schema(example = "3f2b8c1e-7d4a-4f6b-9c2e-1a5d8e9f0b7c") @NotNull UUID productId,
        @Schema(example = "100", description = "Initial stock, greater than zero") @Positive int quantity
) {
}
