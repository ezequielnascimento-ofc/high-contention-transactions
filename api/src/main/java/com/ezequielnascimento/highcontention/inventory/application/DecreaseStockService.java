package com.ezequielnascimento.highcontention.inventory.application;

import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InventoryNotFoundException;
import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.InventoryRepository;

public class DecreaseStockService {

    private final InventoryRepository inventoryRepository;

    public DecreaseStockService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public Inventory execute(InventoryId inventoryId, int quantity) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new InventoryNotFoundException(inventoryId));

        inventory.decrease(quantity);

        return inventoryRepository.save(inventory);
    }
}
