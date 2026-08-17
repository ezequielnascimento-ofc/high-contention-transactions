package com.ezequielnascimento.highcontention.product.application;

import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateProductTest {
    private ProductRepository productRepository;
    private CreateProduct createProduct;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        createProduct = new CreateProduct(productRepository);
    }

    @Test
    void shouldCreateAndSaveProduct() {
        BigDecimal price = new BigDecimal("499.90");

        Product savedProduct = Product.create(
                "Keyboard",
                "Mechanical keyboard",
                price
        );

        when(productRepository.save(any(Product.class)))
                .thenReturn(savedProduct);

        Product result = createProduct.execute(
                "Keyboard",
                "Mechanical keyboard",
                price
        );

        assertNotNull(result);
        assertEquals("Keyboard", result.name());
        assertEquals("Mechanical keyboard", result.description());
        assertEquals(price, result.price());

        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldReturnSavedProduct() {
        BigDecimal price = new BigDecimal("499.90");

        Product savedProduct = Product.create(
                "Keyboard",
                "Mechanical keyboard",
                price
        );

        when(productRepository.save(any(Product.class)))
                .thenReturn(savedProduct);

        Product result = createProduct.execute(
                "Keyboard",
                "Mechanical keyboard",
                price
        );

        assertSame(savedProduct, result);
    }
}
