package com.ezequielnascimento.highcontention.inventory.domain.exceptions;

import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;

public class InventoryNotFoundException extends InventoryDomainException {
    public InventoryNotFoundException(InventoryId inventoryId) {
        super("Inventory not found: " + inventoryId.value());
    }

    public InventoryNotFoundException(ProductId productId) {
        super("Inventory not found for product: " + productId.value());
    }
}
