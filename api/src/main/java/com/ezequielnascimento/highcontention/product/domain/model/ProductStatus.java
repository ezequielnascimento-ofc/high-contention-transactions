package com.ezequielnascimento.highcontention.product.domain.model;

public enum ProductStatus {
    ACTIVE,
    INACTIVE;

    public boolean isActive() {
        return this == ACTIVE;
    }
}
