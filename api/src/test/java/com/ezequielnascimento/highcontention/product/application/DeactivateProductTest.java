package com.ezequielnascimento.highcontention.product.application;

import com.ezequielnascimento.highcontention.product.domain.exceptions.ProductNotFoundException;
import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import com.ezequielnascimento.highcontention.product.domain.model.ProductStatus;
import com.ezequielnascimento.highcontention.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class DeactivateProductTest {

    @Test
    void shouldDeactivateProduct() {
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

        DeactivateProduct useCase = new DeactivateProduct(repository);

        Product result = useCase.execute(product.id());

        assertEquals(ProductStatus.INACTIVE, result.status());

        verify(repository).findById(product.id());
        verify(repository).save(product);
    }

    @Test
    void shouldRejectWhenProductDoesNotExist() {
        ProductRepository repository = mock(ProductRepository.class);

        ProductId productId = new ProductId(UUID.randomUUID());

        when(repository.findById(productId))
                .thenReturn(Optional.empty());

        DeactivateProduct useCase = new DeactivateProduct(repository);

        assertThrows(
                ProductNotFoundException.class,
                () -> useCase.execute(productId)
        );

        verify(repository).findById(productId);
        verify(repository, never()).save(any());
    }
}
