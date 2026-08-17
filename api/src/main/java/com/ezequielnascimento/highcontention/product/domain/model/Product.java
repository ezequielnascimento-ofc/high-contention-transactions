package com.ezequielnascimento.highcontention.product.domain.model;

import com.ezequielnascimento.highcontention.product.domain.exceptions.InvalidProductException;

import java.math.BigDecimal;
import java.time.Instant;

public class Product {
    private ProductId id;
    private String name;
    private String description;
    private BigDecimal price;
    private ProductStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    private Product (
            ProductId id,
            String name,
            String description,
            BigDecimal price,
            ProductStatus status,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        validate();
    }

    public static Product create (
            String name,
            String description,
            BigDecimal price
    ) {
        Instant now = Instant.now();
        return new Product(
                ProductId.generate(),
                name,
                description,
                price,
                ProductStatus.ACTIVE,
                now,
                now
        );
    }

    private void validate() {
        if (name == null || name.isBlank()) {
            throw new InvalidProductException("Product name must not be blank");
        }
        if (price == null || price.signum() < 0) {
            throw new InvalidProductException("Product price must not be negative");
        }
    }

    public void rename (String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidProductException("Product name must not be blank");
        }
        this.name = name;
        touch();
    }

    public void changeDescription (String description) {
        this.description = description;
        touch();
    }

    public void changePrice (BigDecimal price) {
        if (price == null || price.signum() < 0) {
            throw new InvalidProductException("Product price must not be negative");
        }
        this.price = price;
        touch();
    }

    public void activate() {
        this.status = ProductStatus.ACTIVE;
        touch();
    }

    public void deactivate() {
        this.status = ProductStatus.INACTIVE;
        touch();
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    public ProductId id () {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public BigDecimal price() {
        return price;
    }

    public ProductStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
