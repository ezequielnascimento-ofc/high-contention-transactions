package com.ezequielnascimento.highcontention.inventory.application;

import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InvalidInventoryException;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InventoryNotFoundException;
import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;
import com.ezequielnascimento.highcontention.inventory.domain.port.in.IncreaseStockUseCase;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.InventoryRepository;
import org.springframework.transaction.annotation.Transactional;

public class IncreaseStockService implements IncreaseStockUseCase {

    private final InventoryRepository inventoryRepository;

    public IncreaseStockService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    @Transactional
    public Inventory execute(InventoryId inventoryId, int quantity) {
        if (quantity <= 0) {
            throw new InvalidInventoryException("Increase quantity must be greater than zero");
        }

        boolean increased = inventoryRepository.increaseQuantity(inventoryId, quantity);

        if (!increased) {
            throw new InventoryNotFoundException(inventoryId);
        }

        return inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new InventoryNotFoundException(inventoryId));
    }
}