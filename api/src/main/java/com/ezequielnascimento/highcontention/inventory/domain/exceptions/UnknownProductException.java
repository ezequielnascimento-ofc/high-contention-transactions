package com.ezequielnascimento.highcontention.inventory.domain.exceptions;

import com.ezequielnascimento.highcontention.product.domain.model.ProductId;

public class UnknownProductException extends InventoryDomainException {

    public UnknownProductException(ProductId productId) {
        super("Product does not exist: " + productId.value());
    }
}
