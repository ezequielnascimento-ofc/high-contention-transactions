package com.ezequielnascimento.highcontention.inventory.domain.exceptions;

import com.ezequielnascimento.highcontention.product.domain.model.ProductId;

public class InventoryAlreadyExistsException extends InventoryDomainException {

    public InventoryAlreadyExistsException(ProductId productId) {
        super("Inventory already exists for product: " + productId.value());
    }
}
