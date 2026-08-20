package com.ezequielnascimento.highcontention.unit.inventory.application;

import com.ezequielnascimento.highcontention.inventory.application.DecreaseStockService;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InsufficientStockException;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InvalidInventoryException;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InventoryNotFoundException;
import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.InventoryRepository;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.InventoryStockNotifier;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DecreaseStockServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryStockNotifier inventoryStockNotifier;

    @InjectMocks
    private DecreaseStockService decreaseStockService;

    @Nested
    class SuccessfulDecrease {

        @Test
        void shouldDecreaseStockAndReturnUpdatedInventory() {
            Inventory inventory = Inventory.create(ProductId.generate(), 100);
            Inventory updated = Inventory.reconstitute(
                    inventory.id(), inventory.productId(), 70, inventory.createdAt(), inventory.updatedAt());

            when(inventoryRepository.decreaseQuantity(inventory.id(), 30)).thenReturn(true);
            when(inventoryRepository.findById(inventory.id())).thenReturn(Optional.of(updated));

            Inventory result = decreaseStockService.execute(inventory.id(), 30);

            assertEquals(70, result.quantity());
        }

        @Test
        void shouldCallDecreaseQuantityOnRepository() {
            InventoryId inventoryId = InventoryId.generate();
            Inventory updated = Inventory.create(ProductId.generate(), 70);

            when(inventoryRepository.decreaseQuantity(inventoryId, 30)).thenReturn(true);
            when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.of(updated));

            decreaseStockService.execute(inventoryId, 30);

            verify(inventoryRepository).decreaseQuantity(inventoryId, 30);
        }
    }

    @Nested
    class QuantityValidation {

        @Test
        void shouldRejectZeroAsDecreaseAmount() {
            InventoryId inventoryId = InventoryId.generate();

            assertThrows(InvalidInventoryException.class,
                    () -> decreaseStockService.execute(inventoryId, 0));

            verify(inventoryRepository, never())
                    .decreaseQuantity(ArgumentMatchers.any(), ArgumentMatchers.anyInt());
        }

        @Test
        void shouldRejectNegativeDecreaseAmount() {
            InventoryId inventoryId = InventoryId.generate();

            assertThrows(InvalidInventoryException.class,
                    () -> decreaseStockService.execute(inventoryId, -1));

            verify(inventoryRepository, never())
                    .decreaseQuantity(ArgumentMatchers.any(), ArgumentMatchers.anyInt());
        }
    }

    @Nested
    class WhenStockIsInsufficient {

        @Test
        void shouldThrowInsufficientStockExceptionWithCurrentAndRequestedAmounts() {
            Inventory inventory = Inventory.create(ProductId.generate(), 100);

            when(inventoryRepository.decreaseQuantity(inventory.id(), 150)).thenReturn(false);
            when(inventoryRepository.findById(inventory.id())).thenReturn(Optional.of(inventory));

            assertThrows(InsufficientStockException.class,
                    () -> decreaseStockService.execute(inventory.id(), 150));
        }
    }

    @Nested
    class WhenInventoryDoesNotExist {

        @Test
        void shouldThrowInventoryNotFoundException() {
            InventoryId inventoryId = InventoryId.generate();

            when(inventoryRepository.decreaseQuantity(inventoryId, 30)).thenReturn(false);
            when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.empty());

            assertThrows(InventoryNotFoundException.class,
                    () -> decreaseStockService.execute(inventoryId, 30));
        }
    }
}