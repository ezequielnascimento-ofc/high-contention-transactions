package com.ezequielnascimento.highcontention.unit.product.application;

import com.ezequielnascimento.highcontention.product.application.RenameProductService;
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
class RenameProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private RenameProductService renameProductService;

    @Nested
    class SuccessfulRename {

        @Test
        void shouldRenameProductToNewValidName() {
            Product product = Product.create("Keyboard", "Mechanical keyboard", new BigDecimal("499.90"));

            when(productRepository.findById(product.id())).thenReturn(Optional.of(product));
            when(productRepository.save(product)).thenReturn(product);

            Product result = renameProductService.execute(product.id(), "Gaming Keyboard");

            assertEquals("Gaming Keyboard", result.name());
            verify(productRepository).save(product);
        }
    }

    @Nested
    class RenameValidation {

        @Test
        void shouldRejectBlankName() {
            Product product = Product.create("Keyboard", "Mechanical keyboard", new BigDecimal("499.90"));
            when(productRepository.findById(product.id())).thenReturn(Optional.of(product));

            assertThrows(InvalidProductException.class,
                    () -> renameProductService.execute(product.id(), " "));

            verify(productRepository, never()).save(any());
        }

        @Test
        void shouldRejectNullName() {
            Product product = Product.create("Keyboard", "Mechanical keyboard", new BigDecimal("499.90"));
            when(productRepository.findById(product.id())).thenReturn(Optional.of(product));

            assertThrows(InvalidProductException.class,
                    () -> renameProductService.execute(product.id(), null));

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
                    () -> renameProductService.execute(productId, "Gaming Keyboard"));

            verify(productRepository, never()).save(any());
        }
    }
}