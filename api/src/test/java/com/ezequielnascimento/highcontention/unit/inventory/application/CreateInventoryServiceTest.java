package com.ezequielnascimento.highcontention.unit.inventory.application;

import com.ezequielnascimento.highcontention.inventory.application.CreateInventoryService;
import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.InventoryRepository;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateInventoryServiceTest {


    private InventoryRepository inventoryRepository;
    private CreateInventoryService createInventory;

    @BeforeEach
    void setUp() {
        inventoryRepository = mock(InventoryRepository.class);
        createInventory = new CreateInventoryService(inventoryRepository);
    }

    @Test
    void shouldCreateAndSaveInventory() {
        ProductId productId = ProductId.generate();

        Inventory savedInventory = Inventory.create(
                productId,
                100
        );

        when(inventoryRepository.save(any(Inventory.class)))
                .thenReturn(savedInventory);

        Inventory result = createInventory.execute(
                productId,
                100
        );

        assertNotNull(result);
        assertEquals(productId, result.productId());
        assertEquals(100, result.quantity());

        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void shouldReturnSavedInventory() {
        ProductId productId = ProductId.generate();

        Inventory savedInventory = Inventory.create(
                productId,
                100
        );

        when(inventoryRepository.save(any(Inventory.class)))
                .thenReturn(savedInventory);

        Inventory result = createInventory.execute(
                productId,
                100
        );

        assertSame(savedInventory, result);
    }
}
