package com.ezequielnascimento.highcontention.inventory.domain.exceptions;

public class InvalidInventoryException extends RuntimeException {

    public InvalidInventoryException(String message) {
        super(message);
    }
}
