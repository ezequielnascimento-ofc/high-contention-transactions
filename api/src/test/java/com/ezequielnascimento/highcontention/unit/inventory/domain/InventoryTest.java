package com.ezequielnascimento.highcontention.unit.inventory.domain;

import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InsufficientStockException;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InvalidInventoryException;
import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class InventoryTest {

    @Nested
    class InventoryCreation {

        @Test
        void shouldCreateInventoryWithValidProductIdAndQuantity() {
            ProductId productId = ProductId.generate();

            Inventory inventory = Inventory.create(productId, 100);

            assertNotNull(inventory.id());
            assertEquals(productId, inventory.productId());
            assertEquals(100, inventory.quantity());
            assertNotNull(inventory.createdAt());
            assertNotNull(inventory.updatedAt());
        }
    }

    @Nested
    class InventoryCreationValidation {

        @Test
        void shouldRejectCreationWithNegativeQuantity() {
            ProductId productId = ProductId.generate();

            assertThrows(InvalidInventoryException.class,
                    () -> Inventory.create(productId, -1));
        }

        @Test
        void shouldRejectCreationWithNullProductId() {
            assertThrows(InvalidInventoryException.class,
                    () -> Inventory.create(null, 100));
        }

        @Test
        void shouldAcceptZeroAsValidInitialQuantity() {
            Inventory inventory = Inventory.create(ProductId.generate(), 0);

            assertEquals(0, inventory.quantity());
        }
    }

    @Nested
    class StockIncrease {

        @Test
        void shouldIncreaseQuantityByGivenAmount() {
            Inventory inventory = Inventory.create(ProductId.generate(), 100);

            inventory.increase(50);

            assertEquals(150, inventory.quantity());
        }

        @Test
        void shouldRejectZeroAsIncreaseAmount() {
            Inventory inventory = Inventory.create(ProductId.generate(), 100);

            assertThrows(InvalidInventoryException.class, () -> inventory.increase(0));
            assertEquals(100, inventory.quantity());
        }

        @Test
        void shouldRejectNegativeIncreaseAmount() {
            Inventory inventory = Inventory.create(ProductId.generate(), 100);

            assertThrows(InvalidInventoryException.class, () -> inventory.increase(-1));
            assertEquals(100, inventory.quantity());
        }
    }

    @Nested
    class StockDecrease {

        @Test
        void shouldDecreaseQuantityByGivenAmount() {
            Inventory inventory = Inventory.create(ProductId.generate(), 100);

            inventory.decrease(30);

            assertEquals(70, inventory.quantity());
        }

        @Test
        void shouldAllowDecreaseThatExactlyZeroesQuantity() {
            Inventory inventory = Inventory.create(ProductId.generate(), 100);

            inventory.decrease(100);

            assertEquals(0, inventory.quantity());
        }

        @Test
        void shouldRejectDecreaseWhenQuantityIsInsufficient() {
            Inventory inventory = Inventory.create(ProductId.generate(), 100);

            assertThrows(InsufficientStockException.class, () -> inventory.decrease(101));
            assertEquals(100, inventory.quantity());
        }

        @Test
        void shouldRejectZeroAsDecreaseAmount() {
            Inventory inventory = Inventory.create(ProductId.generate(), 100);

            assertThrows(InvalidInventoryException.class, () -> inventory.decrease(0));
            assertEquals(100, inventory.quantity());
        }

        @Test
        void shouldRejectNegativeDecreaseAmount() {
            Inventory inventory = Inventory.create(ProductId.generate(), 100);

            assertThrows(InvalidInventoryException.class, () -> inventory.decrease(-1));
            assertEquals(100, inventory.quantity());
        }
    }

    @Nested
    class InventoryTimestamps {

        @Test
        void shouldUpdateUpdatedAtTimestampWhenQuantityIncreases() {
            Inventory inventory = Inventory.create(ProductId.generate(), 100);
            Instant originalUpdatedAt = inventory.updatedAt();

            inventory.increase(10);

            assertNotNull(inventory.updatedAt());
            assertFalse(inventory.updatedAt().isBefore(originalUpdatedAt));
        }

        @Test
        void shouldUpdateUpdatedAtTimestampWhenQuantityDecreases() {
            Inventory inventory = Inventory.create(ProductId.generate(), 100);
            Instant originalUpdatedAt = inventory.updatedAt();

            inventory.decrease(10);

            assertNotNull(inventory.updatedAt());
            assertFalse(inventory.updatedAt().isBefore(originalUpdatedAt));
        }

        @Test
        void shouldNotChangeCreatedAtWhenQuantityChanges() {
            Inventory inventory = Inventory.create(ProductId.generate(), 100);
            Instant originalCreatedAt = inventory.createdAt();

            inventory.increase(10);
            inventory.decrease(5);

            assertEquals(originalCreatedAt, inventory.createdAt());
        }
    }

    @Nested
    class InventoryIdentityAndEquality {

        @Test
        void shouldKeepSameIdentityAfterQuantityChanges() {
            Inventory inventory = Inventory.create(ProductId.generate(), 100);
            InventoryId expectedId = inventory.id();

            inventory.increase(10);
            inventory.decrease(5);

            assertEquals(expectedId, inventory.id());
        }

        @Test
        void shouldConsiderInventoriesEqualWhenIdsMatchRegardlessOfOtherFields() {
            InventoryId id = InventoryId.generate();
            ProductId productId = ProductId.generate();
            Instant timestamp = Instant.now();

            Inventory first = Inventory.reconstitute(id, productId, 100, timestamp, timestamp);
            Inventory second = Inventory.reconstitute(id, productId, 50, timestamp, timestamp);

            assertEquals(first, second);
            assertEquals(first.hashCode(), second.hashCode());
        }

        @Test
        void shouldConsiderInventoriesDifferentWhenIdsDiffer() {
            ProductId productId = ProductId.generate();
            Inventory first = Inventory.create(productId, 100);
            Inventory second = Inventory.create(productId, 100);

            assertNotEquals(first, second);
        }
    }

    @Nested
    class InventoryReconstitution {

        @Test
        void shouldReconstituteInventoryPreservingAllPersistedFields() {
            InventoryId id = InventoryId.generate();
            ProductId productId = ProductId.generate();
            Instant createdAt = Instant.now().minusSeconds(60);
            Instant updatedAt = Instant.now();

            Inventory inventory = Inventory.reconstitute(id, productId, 42, createdAt, updatedAt);

            assertEquals(id, inventory.id());
            assertEquals(productId, inventory.productId());
            assertEquals(42, inventory.quantity());
            assertEquals(createdAt, inventory.createdAt());
            assertEquals(updatedAt, inventory.updatedAt());
        }
    }
}