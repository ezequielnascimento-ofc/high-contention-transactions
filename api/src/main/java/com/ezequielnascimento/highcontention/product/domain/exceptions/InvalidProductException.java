package com.ezequielnascimento.highcontention.product.domain.exceptions;

public class InvalidProductException extends RuntimeException {
    public InvalidProductException (String message) {
        super(message);
    }
}
