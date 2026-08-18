package com.ezequielnascimento.highcontention.product.infrastructure.adapter.web.dto;

import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.model.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        ProductStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.id().value(),
                product.name(),
                product.description(),
                product.price(),
                product.status(),
                product.createdAt(),
                product.updatedAt()
        );
    }
}
