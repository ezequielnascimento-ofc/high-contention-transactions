package com.ezequielnascimento.highcontention.product.domain.exceptions;

import com.ezequielnascimento.highcontention.product.domain.model.ProductId;

public class ProductNotActiveException extends ProductDomainException {
    public ProductNotActiveException (ProductId productId) {
        super("Product is not active: " + productId.value());
    }
}
