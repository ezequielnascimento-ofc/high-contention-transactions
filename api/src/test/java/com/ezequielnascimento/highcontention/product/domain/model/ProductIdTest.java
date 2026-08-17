package com.ezequielnascimento.highcontention.product.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductIdTest {

    @Test
    void shouldCreateProductIdWithValidValue() {
        UUID value = UUID.randomUUID();
        ProductId productId = new ProductId(value);
        assertEquals(value, productId.value());
    }

    @Test
    void shouldRejectNullValue() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ProductId(null)
        );
    }

    @Test
    void shouldGenerateUniqueProductIds() {
        ProductId first = ProductId.generate();
        ProductId second = ProductId.generate();

        assertNotEquals(first, second);
    }

    @Test
    void shouldPreserveValueEquality() {
        UUID value = UUID.randomUUID();

        ProductId first = new ProductId(value);
        ProductId second = new ProductId(value);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
}
