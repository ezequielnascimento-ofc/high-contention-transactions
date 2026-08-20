package com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.in.web.dto;

import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;

import java.time.Instant;
import java.util.UUID;

public record InventoryResponse(
        UUID id,
        UUID productId,
        int quantity,
        Instant createdAt,
        Instant updatedAt
) {
    public static InventoryResponse from(Inventory inventory) {
        return new InventoryResponse(
                inventory.id().value(),
                inventory.productId().value(),
                inventory.quantity(),
                inventory.createdAt(),
                inventory.updatedAt()
        );
    }
}