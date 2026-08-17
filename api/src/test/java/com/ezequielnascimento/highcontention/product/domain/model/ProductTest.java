package com.ezequielnascimento.highcontention.product.domain.model;

import com.ezequielnascimento.highcontention.product.domain.exceptions.InvalidProductException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void shouldCreateProductWithValidData() {
        BigDecimal price = new BigDecimal("499.90");
        Product product = Product.create("Mechanical Keyboard", "High-performance mechanical keyboard", price);

        assertNotNull(product.id());
        assertEquals(
                "High-performance mechanical keyboard",
                product.description()
        );

        assertEquals(new BigDecimal("499.90"), product.price());
        assertEquals(ProductStatus.ACTIVE, product.status());
        assertNotNull(product.createdAt());
        assertNotNull(product.updatedAt());
    }

    @Test
    void shouldRejectBlankProductName() {
        BigDecimal price = new BigDecimal("100.00");
        assertThrows(
                InvalidProductException.class,
                () -> Product.create(" ", "description", price));
    }

    @Test
    void shouldRejectNullProductName() {
        BigDecimal price = new BigDecimal("100.00");
        assertThrows(
                InvalidProductException.class,
                () -> Product.create(null, "Description", price)
        );
    }

    @Test
    void shouldRejectNegativePrice() {
        BigDecimal price = new BigDecimal("-1.00");
        assertThrows(
                InvalidProductException.class,
                () -> Product.create("Keyboard", "Mechanical Keyboard", price));
    }

    @Test
    void shouldRejectNullPrice() {
        assertThrows(
                InvalidProductException.class,
                () -> Product.create("Keyboard", "Mechanical Keyboard", null)
        );
    }

    @Test
    void shouldAcceptZeroPrice() {
        BigDecimal price = BigDecimal.ZERO;
        Product product = Product.create("Keyboard", "Mechanical keyboard", price);
        assertEquals(BigDecimal.ZERO, product.price());
    }

    @Test
    void shouldChangeProductPrice() {
        BigDecimal price = new BigDecimal("499.90");
        Product product = Product.create("Keyboard", "Mechanical keyboard", price);
        product.changePrice(new BigDecimal("599.90"));
        assertEquals(new BigDecimal("599.90"), product.price());
    }

    @Test
    void shouldRejectNegativePriceWhenChangingPrice() {
        BigDecimal price = new BigDecimal("499.90");
        Product product = Product.create("Keyboard", "Mechanical keyboard", price);
        BigDecimal invalidPrice = new BigDecimal("-50.00");
        assertThrows(InvalidProductException.class, () -> product.changePrice(invalidPrice)
        );
    }

    @Test
    void shouldRenameProduct() {
        BigDecimal price = new BigDecimal("499.90");
        Product product = Product.create("Keyboard", "Mechanical keyboard", price);
        product.rename("Gaming Keyboard");
        assertEquals("Gaming Keyboard", product.name());
    }

    @Test
    void shouldRejectBlankNameWhenRenamingProduct() {
        BigDecimal price = new BigDecimal("499.90");
        Product product = Product.create("Keyboard", "Mechanical keyboard", price);
        assertThrows(InvalidProductException.class, () -> product.rename(""));
    }

    @Test
    void shouldCreateProductAsActive() {
        BigDecimal price = new BigDecimal("499.90");
        Product product = Product.create("Keyboard", "Mechanical keyboard", price);
        assertEquals(ProductStatus.ACTIVE, product.status());
    }

    @Test
    void shouldDeactivateProduct() {
        BigDecimal price = new BigDecimal("499.90");

        Product product = Product.create("Keyboard", "Mechanical keyboard", price);
        product.deactivate();

        assertEquals(ProductStatus.INACTIVE, product.status());
    }

    @Test
    void shouldActivateInactiveProduct() {
        BigDecimal price = new BigDecimal("499.90");
        Product product = Product.create("Keyboard", "Mechanical keyboard", price);

        product.deactivate();
        product.activate();

        assertEquals(ProductStatus.ACTIVE, product.status());
    }

    @Test
    void shouldKeepProductIdentityWhenProductChanges() {
        BigDecimal initialPrice = new BigDecimal("499.90");
        BigDecimal updatedPrice = new BigDecimal("599.90");

        Product product = Product.create("Keyboard", "Mechanical keyboard", initialPrice);
        ProductId expectedId = product.id();

        product.rename("Gaming Keyboard");
        product.changePrice(updatedPrice);
        product.deactivate();

        assertEquals(expectedId, product.id());
    }

    @Test
    void shouldUpdateUpdatedAtWhenProductChanges() {
        BigDecimal initialPrice = new BigDecimal("499.90");
        BigDecimal updatedPrice = new BigDecimal("599.90");

        Product product = Product.create(
                "Keyboard",
                "Mechanical keyboard",
                initialPrice
        );

        var originalUpdatedAt = product.updatedAt();

        product.changePrice(updatedPrice);

        assertNotNull(product.updatedAt());
        assertFalse(product.updatedAt().isBefore(originalUpdatedAt));
    }
}
