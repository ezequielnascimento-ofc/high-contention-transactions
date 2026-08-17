package com.ezequielnascimento.highcontention.product.domain.exceptions;

public class InvalidProductException extends ProductDomainException {
    public InvalidProductException (String message) {
        super(message);
    }
}
