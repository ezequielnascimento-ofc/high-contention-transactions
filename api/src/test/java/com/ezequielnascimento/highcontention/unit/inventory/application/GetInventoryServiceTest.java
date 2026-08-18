package com.ezequielnascimento.highcontention.unit.inventory.application;

import com.ezequielnascimento.highcontention.inventory.application.GetInventoryService;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InventoryNotFoundException;
import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.InventoryRepository;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetInventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private GetInventoryService getInventoryService;

    @Nested
    class WhenInventoryExists {

        @Test
        void shouldReturnInventoryFoundByRepository() {
            Inventory inventory = Inventory.create(ProductId.generate(), 100);
            when(inventoryRepository.findById(inventory.id())).thenReturn(Optional.of(inventory));

            Inventory result = getInventoryService.execute(inventory.id());

            assertSame(inventory, result);
            verify(inventoryRepository).findById(inventory.id());
        }
    }

    @Nested
    class WhenInventoryDoesNotExist {

        @Test
        void shouldThrowInventoryNotFoundException() {
            InventoryId inventoryId = InventoryId.generate();
            when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.empty());

            assertThrows(InventoryNotFoundException.class,
                    () -> getInventoryService.execute(inventoryId));
        }
    }
}