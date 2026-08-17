package com.ezequielnascimento.highcontention.product.domain.exceptions;

public class ProductNotActiveException extends RuntimeException {
    public ProductNotActiveException (String message) {
        super(message);
    }
}
