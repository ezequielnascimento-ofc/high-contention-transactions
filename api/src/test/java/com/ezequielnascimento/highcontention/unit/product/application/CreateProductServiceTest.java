package com.ezequielnascimento.highcontention.unit.product.application;

import com.ezequielnascimento.highcontention.product.application.CreateProductService;
import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.port.out.ProductRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CreateProductService createProductService;

    @Nested
    class SuccessfulCreation {

        @Test
        void shouldCreateProductWithGivenNameDescriptionAndPrice() {
            BigDecimal price = new BigDecimal("499.90");
            Product savedProduct = Product.create("Keyboard", "Mechanical keyboard", price);

            when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
            Product result = createProductService.execute("Keyboard", "Mechanical keyboard", price);

            assertNotNull(result);
            assertEquals("Keyboard", result.name());
            assertEquals("Mechanical keyboard", result.description());
            assertEquals(price, result.price());
        }

        @Test
        void shouldReturnExactProductPersistedByRepository() {
            BigDecimal price = new BigDecimal("499.90");
            Product savedProduct = Product.create("Keyboard", "Mechanical keyboard", price);

            when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
            Product result = createProductService.execute("Keyboard", "Mechanical keyboard", price);
            assertSame(savedProduct, result);
        }

        @Test
        void shouldPersistProductThroughRepository() {
            BigDecimal price = new BigDecimal("499.90");
            Product savedProduct = Product.create("Keyboard", "Mechanical keyboard", price);

            when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
            createProductService.execute("Keyboard", "Mechanical keyboard", price);
            verify(productRepository).save(any(Product.class));
        }
    }
}