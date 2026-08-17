package com.ezequielnascimento.highcontention.product.application;

import com.ezequielnascimento.highcontention.product.domain.exceptions.InvalidProductException;
import com.ezequielnascimento.highcontention.product.domain.exceptions.ProductNotFoundException;
import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import com.ezequielnascimento.highcontention.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class RenameProductTest {
    @Test
    void shouldRenameProduct() {
        ProductRepository repository = mock(ProductRepository.class);

        Product product = Product.create(
                "Keyboard",
                "Mechanical keyboard",
                new BigDecimal("499.90")
        );

        when(repository.findById(product.id()))
                .thenReturn(Optional.of(product));

        when(repository.save(product))
                .thenReturn(product);

        RenameProduct useCase = new RenameProduct(repository);

        Product result = useCase.execute(
                product.id(),
                "Gaming Keyboard"
        );

        assertEquals("Gaming Keyboard", result.name());

        verify(repository).findById(product.id());
        verify(repository).save(product);
    }

    @Test
    void shouldRejectBlankName() {
        ProductRepository repository = mock(ProductRepository.class);

        Product product = Product.create(
                "Keyboard",
                "Mechanical keyboard",
                new BigDecimal("499.90")
        );

        when(repository.findById(product.id()))
                .thenReturn(Optional.of(product));

        RenameProduct useCase = new RenameProduct(repository);

        assertThrows(
                InvalidProductException.class,
                () -> useCase.execute(product.id(), " ")
        );

        verify(repository).findById(product.id());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectNullName() {
        ProductRepository repository = mock(ProductRepository.class);

        Product product = Product.create(
                "Keyboard",
                "Mechanical keyboard",
                new BigDecimal("499.90")
        );

        when(repository.findById(product.id()))
                .thenReturn(Optional.of(product));

        RenameProduct useCase = new RenameProduct(repository);

        assertThrows(
                InvalidProductException.class,
                () -> useCase.execute(product.id(), null)
        );

        verify(repository).findById(product.id());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectWhenProductDoesNotExist() {
        ProductRepository repository = mock(ProductRepository.class);

        ProductId productId = new ProductId(UUID.randomUUID());

        when(repository.findById(productId))
                .thenReturn(Optional.empty());

        RenameProduct useCase = new RenameProduct(repository);

        assertThrows(
                ProductNotFoundException.class,
                () -> useCase.execute(productId, "Gaming Keyboard")
        );

        verify(repository).findById(productId);
        verify(repository, never()).save(any());
    }
}
