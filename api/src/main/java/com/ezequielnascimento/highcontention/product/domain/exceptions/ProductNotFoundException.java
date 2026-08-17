package com.ezequielnascimento.highcontention.product.domain.exceptions;

import com.ezequielnascimento.highcontention.product.domain.model.ProductId;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException (ProductId productId) {
        super("Product not found: " + productId);
    }
}
