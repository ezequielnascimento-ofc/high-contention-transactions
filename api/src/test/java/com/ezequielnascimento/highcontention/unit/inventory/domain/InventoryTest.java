package com.ezequielnascimento.highcontention.unit.inventory.domain;

import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InvalidInventoryException;
import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InventoryTest {


    @Test
    void shouldCreateInventory() {
        ProductId productId = ProductId.generate();

        Inventory inventory = Inventory.create(
                productId,
                100
        );

        assertNotNull(inventory.id());
        assertEquals(productId, inventory.productId());
        assertEquals(100, inventory.quantity());
    }

    @Test
    void shouldRejectNegativeQuantity() {
        ProductId productId = ProductId.generate();

        assertThrows(
                InvalidInventoryException.class,
                () -> Inventory.create(productId, -1)
        );
    }

    @Test
    void shouldRejectNullProductId() {
        assertThrows(
                InvalidInventoryException.class,
                () -> Inventory.create(null, 100)
        );
    }

    @Test
    void shouldIncreaseQuantity() {
        Inventory inventory = Inventory.create(
                ProductId.generate(),
                100
        );

        inventory.increase(50);

        assertEquals(150, inventory.quantity());
    }

    @Test
    void shouldDecreaseQuantity() {
        Inventory inventory = Inventory.create(
                ProductId.generate(),
                100
        );

        inventory.decrease(30);

        assertEquals(70, inventory.quantity());
    }

    @Test
    void shouldRejectDecreaseWhenQuantityIsInsufficient() {
        Inventory inventory = Inventory.create(
                ProductId.generate(),
                100
        );

        assertThrows(
                InvalidInventoryException.class,
                () -> inventory.decrease(101)
        );

        assertEquals(100, inventory.quantity());
    }

    @Test
    void shouldRejectNegativeIncrease() {
        Inventory inventory = Inventory.create(
                ProductId.generate(),
                100
        );

        assertThrows(
                InvalidInventoryException.class,
                () -> inventory.increase(-1)
        );

        assertEquals(100, inventory.quantity());
    }

    @Test
    void shouldRejectNegativeDecrease() {
        Inventory inventory = Inventory.create(
                ProductId.generate(),
                100
        );

        assertThrows(
                InvalidInventoryException.class,
                () -> inventory.decrease(-1)
        );

        assertEquals(100, inventory.quantity());
    }
}
