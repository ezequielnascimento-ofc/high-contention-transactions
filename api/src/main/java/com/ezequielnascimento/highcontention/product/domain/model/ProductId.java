package com.ezequielnascimento.highcontention.product.domain.model;

import java.util.UUID;

public record ProductId(UUID value) {
    public ProductId {
        if (value == null) {
            throw new IllegalArgumentException("Product id must not be null");
        }
    }

    public static ProductId generate() {
        return new ProductId(UUID.randomUUID());
    }
}
