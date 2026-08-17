package com.ezequielnascimento.highcontention.unit.inventory.application;

import com.ezequielnascimento.highcontention.inventory.application.GetInventoryService;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InventoryNotFoundException;
import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.InventoryRepository;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class GetInventoryServiceTest {


    @Test
    void shouldReturnInventory() {
        InventoryRepository repository = mock(InventoryRepository.class);

        Inventory inventory = Inventory.create(
                ProductId.generate(),
                100
        );

        when(repository.findById(inventory.id()))
                .thenReturn(Optional.of(inventory));

        GetInventoryService useCase = new GetInventoryService(repository);

        Inventory result = useCase.execute(inventory.id());

        assertSame(inventory, result);

        verify(repository).findById(inventory.id());
    }

    @Test
    void shouldThrowWhenInventoryDoesNotExist() {
        InventoryRepository repository = mock(InventoryRepository.class);

        InventoryId inventoryId = InventoryId.generate();

        when(repository.findById(inventoryId))
                .thenReturn(Optional.empty());

        GetInventoryService useCase = new GetInventoryService(repository);

        assertThrows(
                InventoryNotFoundException.class,
                () -> useCase.execute(inventoryId)
        );

        verify(repository).findById(inventoryId);
    }
}
