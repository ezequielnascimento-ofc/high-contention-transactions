package com.ezequielnascimento.highcontention.unit.product.application;

import com.ezequielnascimento.highcontention.product.application.GetProductService;
import com.ezequielnascimento.highcontention.product.domain.exceptions.ProductNotFoundException;
import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import com.ezequielnascimento.highcontention.product.domain.port.out.ProductRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private GetProductService getProductService;

    @Nested
    class WhenProductExists {

        @Test
        void shouldReturnProductWithAllFieldsPreserved() {
            Product product = Product.create(
                    "Mechanical Keyboard",
                    "High-performance mechanical keyboard",
                    new BigDecimal("499.90")
            );

            when(productRepository.findById(product.id())).thenReturn(Optional.of(product));

            Product result = getProductService.execute(product.id());

            assertEquals(product.id(), result.id());
            assertEquals(product.name(), result.name());
            assertEquals(product.description(), result.description());
            assertEquals(product.price(), result.price());
            assertEquals(product.status(), result.status());
        }

        @Test
        void shouldQueryRepositoryByGivenId() {
            Product product = Product.create("Keyboard", "Mechanical keyboard", new BigDecimal("499.90"));
            when(productRepository.findById(product.id())).thenReturn(Optional.of(product));
            getProductService.execute(product.id());
            verify(productRepository).findById(product.id());
        }
    }

    @Nested
    class WhenProductDoesNotExist {

        @Test
        void shouldThrowProductNotFoundException() {
            ProductId productId = ProductId.generate();
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            assertThrows(ProductNotFoundException.class, () -> getProductService.execute(productId));
        }
    }
}