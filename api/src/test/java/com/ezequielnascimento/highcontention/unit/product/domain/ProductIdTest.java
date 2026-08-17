package com.ezequielnascimento.highcontention.unit.product.domain;

import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductIdTest {

    @Nested
    class ProductIdCreation {

        @Test
        void shouldCreateProductIdWithValidUuidValue() {
            UUID value = UUID.randomUUID();
            ProductId productId = new ProductId(value);

            assertEquals(value, productId.value());
        }

        @Test
        void shouldRejectCreationWithNullValue() {
            assertThrows(IllegalArgumentException.class, () -> new ProductId(null));
        }
    }

    @Nested
    class ProductIdGeneration {

        @Test
        void shouldGenerateDistinctIdsOnEachCall() {
            ProductId first = ProductId.generate();
            ProductId second = ProductId.generate();

            assertNotEquals(first, second);
        }
    }

    @Nested
    class ProductIdEquality {

        @Test
        void shouldConsiderIdsEqualWhenUnderlyingValueMatches() {
            UUID value = UUID.randomUUID();

            ProductId first = new ProductId(value);
            ProductId second = new ProductId(value);

            assertEquals(first, second);
            assertEquals(first.hashCode(), second.hashCode());
        }

        @Test
        void shouldConsiderIdsDifferentWhenUnderlyingValueDiffers() {
            ProductId first = new ProductId(UUID.randomUUID());
            ProductId second = new ProductId(UUID.randomUUID());

            assertNotEquals(first, second);
        }
    }
}