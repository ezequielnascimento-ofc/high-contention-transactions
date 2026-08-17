package com.ezequielnascimento.highcontention.product.application;

import com.ezequielnascimento.highcontention.product.domain.exceptions.ProductNotFoundException;
import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import com.ezequielnascimento.highcontention.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetProductTest {
    private final ProductRepository productRepository = mock(ProductRepository.class);
    private final GetProduct getProduct = new GetProduct(productRepository);

    @Test
    void shouldReturnProductWhenProductExists() {
        Product product = Product.create(
                "Mechanical Keyboard",
                "High-performance mechanical keyboard",
                new BigDecimal("499.90")
        );

        when(productRepository.findById(product.id()))
                .thenReturn(Optional.of(product));

        Product result = getProduct.execute(product.id());

        assertNotNull(result);
        assertEquals(product.id(), result.id());
        assertEquals(product.name(), result.name());
        assertEquals(product.description(), result.description());
        assertEquals(product.price(), result.price());
        assertEquals(product.status(), result.status());

        verify(productRepository).findById(product.id());
    }

    @Test
    void shouldThrowExceptionWhenProductDoesNotExist() {
        ProductId productId = ProductId.generate();

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> getProduct.execute(productId)
        );

        verify(productRepository).findById(productId);
    }
}
