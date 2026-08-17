package com.ezequielnascimento.highcontention.product.application;

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

class ChangeProductPriceTest {

    @Test
     void shouldChangeProductPrice() {
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

        ChangeProductPrice useCase = new ChangeProductPrice(repository);

        Product result = useCase.execute(
                product.id(),
                new BigDecimal("599.90")
        );

        assertEquals(new BigDecimal("599.90"), result.price());

        verify(repository).findById(product.id());
        verify(repository).save(product);
    }

    @Test
    void shouldRejectNegativePrice() {
        ProductRepository repository = mock(ProductRepository.class);

        Product product = Product.create(
                "Keyboard",
                "Mechanical keyboard",
                new BigDecimal("499.90")
        );

        when(repository.findById(product.id()))
                .thenReturn(Optional.of(product));

        ChangeProductPrice useCase = new ChangeProductPrice(repository);

        assertThrows(
                RuntimeException.class,
                () -> useCase.execute(
                        product.id(),
                        new BigDecimal("-50.00")
                )
        );

        verify(repository).findById(product.id());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectNullPrice() {
        ProductRepository repository = mock(ProductRepository.class);

        Product product = Product.create(
                "Keyboard",
                "Mechanical keyboard",
                new BigDecimal("499.90")
        );

        when(repository.findById(product.id()))
                .thenReturn(Optional.of(product));

        ChangeProductPrice useCase = new ChangeProductPrice(repository);

        assertThrows(
                RuntimeException.class,
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

        ChangeProductPrice useCase = new ChangeProductPrice(repository);

        assertThrows(
                ProductNotFoundException.class,
                () -> useCase.execute(
                        productId,
                        new BigDecimal("599.90")
                )
        );

        verify(repository).findById(productId);
        verify(repository, never()).save(any());
    }
}
