package com.ezequielnascimento.highcontention.unit.product.application;

import com.ezequielnascimento.highcontention.product.application.DeactivateProductService;
import com.ezequielnascimento.highcontention.product.domain.exceptions.ProductNotFoundException;
import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import com.ezequielnascimento.highcontention.product.domain.model.ProductStatus;
import com.ezequielnascimento.highcontention.product.domain.port.out.ProductRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeactivateProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private DeactivateProductService deactivateProductService;

    @Nested
    class SuccessfulDeactivation {

        @Test
        void shouldDeactivateActiveProduct() {
            Product product = Product.create("Keyboard", "Mechanical keyboard", new BigDecimal("499.90"));

            when(productRepository.findById(product.id())).thenReturn(Optional.of(product));
            when(productRepository.save(product)).thenReturn(product);

            Product result = deactivateProductService.execute(product.id());

            assertEquals(ProductStatus.INACTIVE, result.status());
            verify(productRepository).save(product);
        }

        @Test
        void shouldKeepProductInactiveWhenAlreadyInactive() {
            Product product = Product.create("Keyboard", "Mechanical keyboard", new BigDecimal("499.90"));
            product.deactivate();

            when(productRepository.findById(product.id())).thenReturn(Optional.of(product));
            when(productRepository.save(product)).thenReturn(product);

            Product result = deactivateProductService.execute(product.id());

            assertEquals(ProductStatus.INACTIVE, result.status());
        }
    }

    @Nested
    class WhenProductDoesNotExist {

        @Test
        void shouldThrowProductNotFoundException() {
            ProductId productId = ProductId.generate();
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            assertThrows(ProductNotFoundException.class,
                    () -> deactivateProductService.execute(productId));

            verify(productRepository, never()).save(any());
        }
    }
}