package com.ezequielnascimento.highcontention.unit.inventory.application;

import com.ezequielnascimento.highcontention.inventory.application.DecreaseStockService;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.ConcurrencyConflictException;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InsufficientStockException;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InvalidInventoryException;
import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.InventoryStockNotifier;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.StockDecrementer;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DecreaseStockServiceTest {

    @Mock
    private StockDecrementer stockDecrementer;
    @Mock
    private InventoryStockNotifier inventoryStockNotifier;
    @InjectMocks
    private DecreaseStockService decreaseStockService;

    @Nested
    class SuccessfulDecrease {

        @Test
        void shouldReturnTheInventoryCommittedByTheDecrementer() {
            Inventory updated = Inventory.create(ProductId.generate(), 70);
            when(stockDecrementer.decrement(updated.id(), 30)).thenReturn(updated);

            Inventory result = decreaseStockService.execute(updated.id(), 30);

            assertEquals(70, result.quantity());
        }

        @Test
        void shouldNotifySubscribersWithTheNewQuantity() {
            Inventory updated = Inventory.create(ProductId.generate(), 70);
            when(stockDecrementer.decrement(updated.id(), 30)).thenReturn(updated);

            decreaseStockService.execute(updated.id(), 30);

            verify(inventoryStockNotifier).notifyStockChanged(updated.id(), 70);
        }
    }

    @Nested
    class QuantityValidation {

        @ParameterizedTest(name = "quantity {0}")
        @ValueSource(ints = {0, -1})
        void shouldRejectNonPositiveQuantitiesWithoutTouchingTheStock(int quantity) {
            InventoryId inventoryId = InventoryId.generate();

            assertThrows(InvalidInventoryException.class, () -> decreaseStockService.execute(inventoryId, quantity));

            verifyNoInteractions(stockDecrementer, inventoryStockNotifier);
        }
    }

    @Nested
    class WhenTheDecreaseIsRefused {

        @Test
        void shouldPropagateInsufficientStockAndNotNotify() {
            InventoryId inventoryId = InventoryId.generate();
            when(stockDecrementer.decrement(inventoryId, 150))
                    .thenThrow(new InsufficientStockException(inventoryId, 100, 150));

            assertThrows(InsufficientStockException.class, () -> decreaseStockService.execute(inventoryId, 150));

            verify(inventoryStockNotifier, never()).notifyStockChanged(any(), anyInt());
        }

        @Test
        void shouldPropagateConcurrencyConflictAndNotNotify() {
            InventoryId inventoryId = InventoryId.generate();
            when(stockDecrementer.decrement(inventoryId, 1)).thenThrow(new ConcurrencyConflictException(inventoryId, 10));

            assertThrows(ConcurrencyConflictException.class, () -> decreaseStockService.execute(inventoryId, 1));

            verify(inventoryStockNotifier, never()).notifyStockChanged(any(), anyInt());
        }
    }
}
