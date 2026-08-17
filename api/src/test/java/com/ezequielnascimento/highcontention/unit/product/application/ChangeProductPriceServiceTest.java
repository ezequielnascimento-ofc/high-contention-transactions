package com.ezequielnascimento.highcontention.unit.product.application;

import com.ezequielnascimento.highcontention.product.application.ChangeProductPriceService;
import com.ezequielnascimento.highcontention.product.domain.exceptions.InvalidProductException;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChangeProductPriceServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ChangeProductPriceService changeProductPriceService;

    @Nested
    class SuccessfulPriceChange {

        @Test
        void shouldChangeProductPriceToNewValidValue() {
            Product product = Product.create("Keyboard", "Mechanical keyboard", new BigDecimal("499.90"));

            when(productRepository.findById(product.id())).thenReturn(Optional.of(product));
            when(productRepository.save(product)).thenReturn(product);

            Product result = changeProductPriceService.execute(product.id(), new BigDecimal("599.90"));

            assertEquals(new BigDecimal("599.90"), result.price());
            verify(productRepository).save(product);
        }
    }

    @Nested
    class PriceValidation {

        @Test
        void shouldRejectNegativePrice() {
            Product product = Product.create("Keyboard", "Mechanical keyboard", new BigDecimal("499.90"));
            when(productRepository.findById(product.id())).thenReturn(Optional.of(product));

            assertThrows(InvalidProductException.class,
                    () -> changeProductPriceService.execute(product.id(), new BigDecimal("-50.00")));

            verify(productRepository, never()).save(any());
        }

        @Test
        void shouldRejectNullPrice() {
            Product product = Product.create("Keyboard", "Mechanical keyboard", new BigDecimal("499.90"));
            when(productRepository.findById(product.id())).thenReturn(Optional.of(product));

            assertThrows(InvalidProductException.class,
                    () -> changeProductPriceService.execute(product.id(), null));

            verify(productRepository, never()).save(any());
        }
    }

    @Nested
    class WhenProductDoesNotExist {

        @Test
        void shouldThrowProductNotFoundException() {
            ProductId productId = ProductId.generate();
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            assertThrows(ProductNotFoundException.class,
                    () -> changeProductPriceService.execute(productId, new BigDecimal("599.90")));

            verify(productRepository, never()).save(any());
        }
    }
}