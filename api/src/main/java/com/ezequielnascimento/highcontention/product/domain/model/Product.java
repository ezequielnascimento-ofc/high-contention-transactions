package com.ezequielnascimento.highcontention.product.domain.model;

import com.ezequielnascimento.highcontention.product.domain.exceptions.InvalidProductException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class Product {

    private final ProductId id;
    private String name;
    private String description;
    private BigDecimal price;
    private ProductStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private Product(
            ProductId id,
            String name,
            String description,
            BigDecimal price,
            ProductStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.name = validateName(name);
        this.description = description;
        this.price = validatePrice(price);
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Product create(String name, String description, BigDecimal price) {
        Instant now = now();
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

    public static Product reconstitute(
            ProductId id,
            String name,
            String description,
            BigDecimal price,
            ProductStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new Product(id, name, description, price, status, createdAt, updatedAt);
    }

    private static String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidProductException("Product name must not be blank");
        }
        return name;
    }

    private static BigDecimal validatePrice(BigDecimal price) {
        if (price == null || price.signum() < 0) {
            throw new InvalidProductException("Product price must not be negative");
        }
        return price;
    }

    public void rename(String name) {
        this.name = validateName(name);
        touch();
    }

    public void changeDescription(String description) {
        // description é opcional por design — null/blank são valores válidos
        this.description = description;
        touch();
    }

    public void changePrice(BigDecimal price) {
        this.price = validatePrice(price);
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
        this.updatedAt = now();
    }

    private static Instant now() {
        return Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    public ProductId id() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}