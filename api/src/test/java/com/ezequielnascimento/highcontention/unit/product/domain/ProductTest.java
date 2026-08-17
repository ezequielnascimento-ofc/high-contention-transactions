package com.ezequielnascimento.highcontention.unit.product.domain;

import com.ezequielnascimento.highcontention.product.domain.exceptions.InvalidProductException;
import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import com.ezequielnascimento.highcontention.product.domain.model.ProductStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Nested
    class ProductCreation {

        @Test
        void shouldCreateProductWithValidNameDescriptionAndPrice() {
            BigDecimal price = new BigDecimal("499.90");
            Product product = Product.create("Mechanical Keyboard", "High-performance mechanical keyboard", price);

            assertNotNull(product.id());
            assertEquals("Mechanical Keyboard", product.name());
            assertEquals("High-performance mechanical keyboard", product.description());
            assertEquals(new BigDecimal("499.90"), product.price());
            assertNotNull(product.createdAt());
            assertNotNull(product.updatedAt());
        }

        @Test
        void shouldCreateProductWithActiveStatusByDefault() {
            BigDecimal price = new BigDecimal("499.90");
            Product product = Product.create("Keyboard", "Mechanical keyboard", price);

            assertEquals(ProductStatus.ACTIVE, product.status());
        }

        @Test
        void shouldAllowNullDescriptionWhenCreatingProduct() {
            BigDecimal price = new BigDecimal("499.90");
            Product product = Product.create("Keyboard", null, price);

            assertNull(product.description());
        }

        @Test
        void shouldAcceptZeroAsValidPrice() {
            BigDecimal price = BigDecimal.ZERO;
            Product product = Product.create("Keyboard", "Mechanical keyboard", price);

            assertEquals(BigDecimal.ZERO, product.price());
        }
    }

    @Nested
    class ProductCreationValidation {

        @Test
        void shouldRejectCreationWithBlankName() {
            BigDecimal price = new BigDecimal("100.00");

            assertThrows(InvalidProductException.class,
                    () -> Product.create(" ", "description", price));
        }

        @Test
        void shouldRejectCreationWithNullName() {
            BigDecimal price = new BigDecimal("100.00");

            assertThrows(InvalidProductException.class,
                    () -> Product.create(null, "Description", price));
        }

        @Test
        void shouldRejectCreationWithNegativePrice() {
            BigDecimal price = new BigDecimal("-1.00");

            assertThrows(InvalidProductException.class,
                    () -> Product.create("Keyboard", "Mechanical Keyboard", price));
        }

        @Test
        void shouldRejectCreationWithNullPrice() {
            assertThrows(InvalidProductException.class,
                    () -> Product.create("Keyboard", "Mechanical Keyboard", null));
        }
    }

    @Nested
    class ProductRenaming {

        @Test
        void shouldRenameProductToNewValidName() {
            BigDecimal price = new BigDecimal("499.90");
            Product product = Product.create("Keyboard", "Mechanical keyboard", price);

            product.rename("Gaming Keyboard");

            assertEquals("Gaming Keyboard", product.name());
        }

        @Test
        void shouldRejectRenamingToBlankName() {
            BigDecimal price = new BigDecimal("499.90");
            Product product = Product.create("Keyboard", "Mechanical keyboard", price);

            assertThrows(InvalidProductException.class, () -> product.rename(""));
        }

        @Test
        void shouldRejectRenamingToNullName() {
            BigDecimal price = new BigDecimal("499.90");
            Product product = Product.create("Keyboard", "Mechanical keyboard", price);

            assertThrows(InvalidProductException.class, () -> product.rename(null));
        }
    }

    @Nested
    class ProductDescriptionChange {

        @Test
        void shouldChangeProductDescriptionToNewValue() {
            BigDecimal price = new BigDecimal("499.90");
            Product product = Product.create("Keyboard", "Mechanical keyboard", price);

            product.changeDescription("Updated description");

            assertEquals("Updated description", product.description());
        }

        @Test
        void shouldAllowChangingDescriptionToNull() {
            BigDecimal price = new BigDecimal("499.90");
            Product product = Product.create("Keyboard", "Mechanical keyboard", price);

            product.changeDescription(null);

            assertNull(product.description());
        }
    }

    @Nested
    class ProductPriceChange {

        @Test
        void shouldChangeProductPriceToNewValidValue() {
            BigDecimal price = new BigDecimal("499.90");
            Product product = Product.create("Keyboard", "Mechanical keyboard", price);

            product.changePrice(new BigDecimal("599.90"));

            assertEquals(new BigDecimal("599.90"), product.price());
        }

        @Test
        void shouldRejectChangingPriceToNegativeValue() {
            BigDecimal price = new BigDecimal("499.90");
            Product product = Product.create("Keyboard", "Mechanical keyboard", price);
            BigDecimal invalidPrice = new BigDecimal("-50.00");

            assertThrows(InvalidProductException.class, () -> product.changePrice(invalidPrice));
        }

        @Test
        void shouldRejectChangingPriceToNull() {
            BigDecimal price = new BigDecimal("499.90");
            Product product = Product.create("Keyboard", "Mechanical keyboard", price);

            assertThrows(InvalidProductException.class, () -> product.changePrice(null));
        }
    }

    @Nested
    class ProductStatusTransition {

        @Test
        void shouldDeactivateActiveProduct() {
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
        void shouldKeepProductActiveWhenActivatingAlreadyActiveProduct() {
            BigDecimal price = new BigDecimal("499.90");
            Product product = Product.create("Keyboard", "Mechanical keyboard", price);

            assertDoesNotThrow(product::activate);
            assertEquals(ProductStatus.ACTIVE, product.status());
        }

        @Test
        void shouldKeepProductInactiveWhenDeactivatingAlreadyInactiveProduct() {
            BigDecimal price = new BigDecimal("499.90");
            Product product = Product.create("Keyboard", "Mechanical keyboard", price);
            product.deactivate();

            assertDoesNotThrow(product::deactivate);
            assertEquals(ProductStatus.INACTIVE, product.status());
        }
    }

    @Nested
    class ProductIdentityAndEquality {

        @Test
        void shouldKeepSameIdentityAfterMultipleFieldChanges() {
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
        void shouldConsiderProductsEqualWhenIdsMatchRegardlessOfOtherFields() {
            ProductId id = ProductId.generate();
            Instant timestamp = Instant.now();

            Product first = Product.reconstitute(
                    id, "Keyboard", "desc", new BigDecimal("100.00"),
                    ProductStatus.ACTIVE, timestamp, timestamp
            );
            Product second = Product.reconstitute(
                    id, "Different Name", "other desc", new BigDecimal("200.00"),
                    ProductStatus.INACTIVE, timestamp, timestamp
            );

            assertEquals(first, second);
            assertEquals(first.hashCode(), second.hashCode());
        }

        @Test
        void shouldConsiderProductsDifferentWhenIdsDiffer() {
            BigDecimal price = new BigDecimal("499.90");
            Product first = Product.create("Keyboard", "desc", price);
            Product second = Product.create("Keyboard", "desc", price);

            assertNotEquals(first, second);
        }
    }

    @Nested
    class ProductTimestamps {

        @Test
        void shouldUpdateUpdatedAtTimestampWhenProductChanges() {
            BigDecimal initialPrice = new BigDecimal("499.90");
            BigDecimal updatedPrice = new BigDecimal("599.90");

            Product product = Product.create("Keyboard", "Mechanical keyboard", initialPrice);
            Instant originalUpdatedAt = product.updatedAt();

            product.changePrice(updatedPrice);

            assertNotNull(product.updatedAt());
            assertFalse(product.updatedAt().isBefore(originalUpdatedAt));
        }
    }

    @Nested
    class ProductReconstitution {

        @Test
        void shouldReconstituteProductPreservingAllPersistedFields() {
            ProductId id = ProductId.generate();
            Instant createdAt = Instant.now().minusSeconds(60);
            Instant updatedAt = Instant.now();

            Product product = Product.reconstitute(
                    id, "Keyboard", "desc", new BigDecimal("499.90"),
                    ProductStatus.INACTIVE, createdAt, updatedAt
            );

            assertEquals(id, product.id());
            assertEquals("Keyboard", product.name());
            assertEquals("desc", product.description());
            assertEquals(new BigDecimal("499.90"), product.price());
            assertEquals(ProductStatus.INACTIVE, product.status());
            assertEquals(createdAt, product.createdAt());
            assertEquals(updatedAt, product.updatedAt());
        }
    }
}