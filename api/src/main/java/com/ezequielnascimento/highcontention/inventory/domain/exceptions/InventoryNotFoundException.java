package com.ezequielnascimento.highcontention.inventory.domain.exceptions;

import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;

public class InventoryNotFoundException extends InventoryDomainException {
    public InventoryNotFoundException(InventoryId inventoryId) {
        super("Inventory not found: " + inventoryId.value());
    }
}
