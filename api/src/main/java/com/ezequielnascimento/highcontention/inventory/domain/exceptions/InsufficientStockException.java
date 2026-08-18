package com.ezequielnascimento.highcontention.inventory.domain.exceptions;

import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;

public class InsufficientStockException extends InventoryDomainException {
    public InsufficientStockException(InventoryId inventoryId, int available, int requested) {
        super("Insufficient stock for inventory " + inventoryId.value()
                + ": available=" + available + ", requested=" + requested);
    }
}
