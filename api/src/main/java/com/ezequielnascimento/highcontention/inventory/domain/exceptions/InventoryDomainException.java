package com.ezequielnascimento.highcontention.inventory.domain.exceptions;

public abstract class InventoryDomainException extends RuntimeException {
    protected InventoryDomainException(String message) {
        super(message);
    }
}
