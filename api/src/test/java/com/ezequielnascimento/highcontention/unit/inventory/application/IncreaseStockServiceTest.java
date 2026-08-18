package com.ezequielnascimento.highcontention.unit.inventory.application;

import com.ezequielnascimento.highcontention.inventory.application.IncreaseStockService;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InvalidInventoryException;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncreaseStockServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private IncreaseStockService increaseStockService;

    @Nested
    class SuccessfulIncrease {

        @Test
        void shouldIncreaseStockAndReturnUpdatedInventory() {
            Inventory inventory = Inventory.create(ProductId.generate(), 100);
            Inventory updated = Inventory.reconstitute(
                    inventory.id(), inventory.productId(), 150, inventory.createdAt(), inventory.updatedAt());

            when(inventoryRepository.increaseQuantity(inventory.id(), 50)).thenReturn(true);
            when(inventoryRepository.findById(inventory.id())).thenReturn(Optional.of(updated));

            Inventory result = increaseStockService.execute(inventory.id(), 50);

            assertEquals(150, result.quantity());
        }

        @Test
        void shouldCallIncreaseQuantityOnRepository() {
            InventoryId inventoryId = InventoryId.generate();
            Inventory updated = Inventory.create(ProductId.generate(), 150);

            when(inventoryRepository.increaseQuantity(inventoryId, 50)).thenReturn(true);
            when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.of(updated));

            increaseStockService.execute(inventoryId, 50);

            verify(inventoryRepository).increaseQuantity(inventoryId, 50);
        }
    }

    @Nested
    class QuantityValidation {

        @Test
        void shouldRejectZeroAsIncreaseAmount() {
            InventoryId inventoryId = InventoryId.generate();

            assertThrows(InvalidInventoryException.class,
                    () -> increaseStockService.execute(inventoryId, 0));

            verify(inventoryRepository, never()).increaseQuantity(any(), anyInt());
        }

        @Test
        void shouldRejectNegativeIncreaseAmount() {
            InventoryId inventoryId = InventoryId.generate();

            assertThrows(InvalidInventoryException.class,
                    () -> increaseStockService.execute(inventoryId, -1));

            verify(inventoryRepository, never()).increaseQuantity(any(), anyInt());
        }
    }

    @Nested
    class WhenInventoryDoesNotExist {

        @Test
        void shouldThrowInventoryNotFoundExceptionWhenRepositoryReportsNoRowsAffected() {
            InventoryId inventoryId = InventoryId.generate();
            when(inventoryRepository.increaseQuantity(inventoryId, 50)).thenReturn(false);

            assertThrows(InventoryNotFoundException.class,
                    () -> increaseStockService.execute(inventoryId, 50));
        }
    }

    private InventoryId any() {
        return org.mockito.ArgumentMatchers.any(InventoryId.class);
    }
}