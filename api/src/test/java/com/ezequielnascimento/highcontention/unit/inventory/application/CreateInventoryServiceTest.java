package com.ezequielnascimento.highcontention.unit.inventory.application;

import com.ezequielnascimento.highcontention.inventory.application.CreateInventoryService;
import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.InventoryRepository;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateInventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private CreateInventoryService createInventoryService;

    @Nested
    class SuccessfulCreation {

        @Test
        void shouldCreateInventoryWithGivenProductIdAndQuantity() {
            ProductId productId = ProductId.generate();
            Inventory savedInventory = Inventory.create(productId, 100);

            when(inventoryRepository.save(any(Inventory.class))).thenReturn(savedInventory);

            Inventory result = createInventoryService.execute(productId, 100);

            assertNotNull(result);
            assertEquals(productId, result.productId());
            assertEquals(100, result.quantity());
        }

        @Test
        void shouldReturnExactInventoryPersistedByRepository() {
            ProductId productId = ProductId.generate();
            Inventory savedInventory = Inventory.create(productId, 100);

            when(inventoryRepository.save(any(Inventory.class))).thenReturn(savedInventory);

            Inventory result = createInventoryService.execute(productId, 100);

            assertSame(savedInventory, result);
        }

        @Test
        void shouldPersistInventoryThroughRepository() {
            ProductId productId = ProductId.generate();
            Inventory savedInventory = Inventory.create(productId, 100);

            when(inventoryRepository.save(any(Inventory.class))).thenReturn(savedInventory);

            createInventoryService.execute(productId, 100);

            verify(inventoryRepository).save(any(Inventory.class));
        }
    }
}
