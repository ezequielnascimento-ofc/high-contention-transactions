package com.ezequielnascimento.highcontention.inventory.domain.model;

import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InsufficientStockException;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InvalidInventoryException;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class Inventory {

    private final InventoryId id;
    private final ProductId productId;
    private int quantity;
    private final Instant createdAt;
    private Instant updatedAt;

    private Inventory(
            InventoryId id,
            ProductId productId,
            int quantity,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        validate();
    }

    public static Inventory create(ProductId productId, int quantity) {
        Instant now = now();
        return new Inventory(
                InventoryId.generate(),
                productId,
                quantity,
                now,
                now
        );
    }

    public static Inventory reconstitute(
            InventoryId id,
            ProductId productId,
            int quantity,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new Inventory(id, productId, quantity, createdAt, updatedAt);
    }

    private void validate() {
        if (productId == null) {
            throw new InvalidInventoryException("Inventory product id must not be null");
        }

        if (quantity < 0) {
            throw new InvalidInventoryException("Inventory quantity must not be negative");
        }
    }

    public void increase(int quantity) {
        if (quantity <= 0) {
            throw new InvalidInventoryException("Increase quantity must be greater than zero");
        }

        this.quantity += quantity;
        touch();
    }

    public void decrease(int quantity) {
        if (quantity <= 0) {
            throw new InvalidInventoryException("Decrease quantity must be greater than zero");
        }

        if (this.quantity < quantity) {
            throw new InsufficientStockException(this.id, this.quantity, quantity);
        }

        this.quantity -= quantity;
        touch();
    }

    private void touch() {
        this.updatedAt = now();
    }

    private static Instant now() {
        return Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    public InventoryId id() {
        return id;
    }

    public ProductId productId() {
        return productId;
    }

    public int quantity() {
        return quantity;
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
        if (!(o instanceof Inventory other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}