package com.ezequielnascimento.highcontention.inventory.domain.model;

import java.util.UUID;

public record InventoryId(UUID value) {

    public InventoryId {
        if (value == null) {
            throw new IllegalArgumentException("Inventory id must not be null");
        }
    }

    public static InventoryId generate() {
        return new InventoryId(UUID.randomUUID());
    }
}
