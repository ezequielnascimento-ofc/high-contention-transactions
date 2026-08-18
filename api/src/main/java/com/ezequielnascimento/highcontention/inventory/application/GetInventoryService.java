package com.ezequielnascimento.highcontention.inventory.application;

import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InventoryNotFoundException;
import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;
import com.ezequielnascimento.highcontention.inventory.domain.port.in.GetInventoryUseCase;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.InventoryRepository;
import org.springframework.transaction.annotation.Transactional;

public class GetInventoryService implements GetInventoryUseCase {

    private final InventoryRepository inventoryRepository;

    public GetInventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Inventory execute(InventoryId inventoryId) {
        return inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new InventoryNotFoundException(inventoryId));
    }
}