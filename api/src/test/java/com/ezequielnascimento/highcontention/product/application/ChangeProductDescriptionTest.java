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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChangeProductDescriptionTest {
    @Test
    void shouldChangeProductDescription() {
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

        ChangeProductDescription useCase =
                new ChangeProductDescription(repository);

        Product result = useCase.execute(
                product.id(),
                "Gaming mechanical keyboard"
        );

        assertEquals(
                "Gaming mechanical keyboard",
                result.description()
        );

        verify(repository).findById(product.id());
        verify(repository).save(product);
    }

    @Test
    void shouldAllowNullDescription() {
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

        ChangeProductDescription useCase =
                new ChangeProductDescription(repository);

        Product result = useCase.execute(
                product.id(),
                null
        );

        assertEquals(null, result.description());

        verify(repository).findById(product.id());
        verify(repository).save(product);
    }

    @Test
    void shouldRejectWhenProductDoesNotExist() {
        ProductRepository repository = mock(ProductRepository.class);

        ProductId productId = new ProductId(UUID.randomUUID());

        when(repository.findById(productId))
                .thenReturn(Optional.empty());

        ChangeProductDescription useCase =
                new ChangeProductDescription(repository);

        assertThrows(
                ProductNotFoundException.class,
                () -> useCase.execute(
                        productId,
                        "Gaming mechanical keyboard"
                )
        );

        verify(repository).findById(productId);
        verify(repository, never()).save(any());
    }
}
