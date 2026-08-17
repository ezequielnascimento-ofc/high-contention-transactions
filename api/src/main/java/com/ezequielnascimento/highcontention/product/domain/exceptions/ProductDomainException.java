package com.ezequielnascimento.highcontention.product.domain.exceptions;

public abstract class ProductDomainException extends RuntimeException {
    protected ProductDomainException (String message) {
        super(message);
    }
}
