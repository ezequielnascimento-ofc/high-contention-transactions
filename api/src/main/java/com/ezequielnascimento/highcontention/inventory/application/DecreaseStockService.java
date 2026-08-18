package com.ezequielnascimento.highcontention.inventory.application;

import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InsufficientStockException;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InvalidInventoryException;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InventoryNotFoundException;
import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;
import com.ezequielnascimento.highcontention.inventory.domain.port.in.DecreaseStockUseCase;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.InventoryRepository;
import org.springframework.transaction.annotation.Transactional;

public class DecreaseStockService implements DecreaseStockUseCase {

    private final InventoryRepository inventoryRepository;

    public DecreaseStockService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    @Transactional
    public Inventory execute(InventoryId inventoryId, int quantity) {
        if (quantity <= 0) {
            throw new InvalidInventoryException("Decrease quantity must be greater than zero");
        }

        boolean decreased = inventoryRepository.decreaseQuantity(inventoryId, quantity);

        if (!decreased) {
            Inventory current = inventoryRepository.findById(inventoryId)
                    .orElseThrow(() -> new InventoryNotFoundException(inventoryId));

            throw new InsufficientStockException(inventoryId, current.quantity(), quantity);
        }

        return inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new InventoryNotFoundException(inventoryId));
    }
}