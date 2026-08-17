package com.ezequielnascimento.highcontention.unit.inventory.application;

import com.ezequielnascimento.highcontention.inventory.application.IncreaseStockService;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InvalidInventoryException;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InventoryNotFoundException;
import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.InventoryRepository;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class IncreaseStockServiceTest {

    @Test
    void shouldIncreaseStock() {
        InventoryRepository repository = mock(InventoryRepository.class);

        Inventory inventory = Inventory.create(
                ProductId.generate(),
                100
        );

        when(repository.findById(inventory.id()))
                .thenReturn(Optional.of(inventory));

        when(repository.save(inventory))
                .thenReturn(inventory);

        IncreaseStockService useCase = new IncreaseStockService(repository);

        Inventory result = useCase.execute(
                inventory.id(),
                50
        );

        assertEquals(150, result.quantity());

        verify(repository).findById(inventory.id());
        verify(repository).save(inventory);
    }

    @Test
    void shouldRejectInvalidQuantity() {
        InventoryRepository repository = mock(InventoryRepository.class);

        Inventory inventory = Inventory.create(
                ProductId.generate(),
                100
        );

        when(repository.findById(inventory.id()))
                .thenReturn(Optional.of(inventory));

        IncreaseStockService useCase = new IncreaseStockService(repository);

        assertThrows(
                InvalidInventoryException.class,
                () -> useCase.execute(inventory.id(), -1)
        );

        assertEquals(100, inventory.quantity());

        verify(repository).findById(inventory.id());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowWhenInventoryDoesNotExist() {
        InventoryRepository repository = mock(InventoryRepository.class);

        var inventoryId = com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId.generate();

        when(repository.findById(inventoryId))
                .thenReturn(Optional.empty());

        IncreaseStockService useCase = new IncreaseStockService(repository);

        assertThrows(
                InventoryNotFoundException.class,
                () -> useCase.execute(inventoryId, 50)
        );

        verify(repository).findById(inventoryId);
        verify(repository, never()).save(any());
    }
}
