package com.ezequielnascimento.highcontention.inventory.application;

import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InvalidInventoryException;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InventoryNotFoundException;
import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;
import com.ezequielnascimento.highcontention.inventory.domain.port.in.IncreaseStockUseCase;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.InventoryRepository;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.InventoryStockNotifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class IncreaseStockService implements IncreaseStockUseCase {

    private final InventoryRepository inventoryRepository;
    private final InventoryStockNotifier inventoryStockNotifier;

    public IncreaseStockService(InventoryRepository inventoryRepository, InventoryStockNotifier inventoryStockNotifier) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryStockNotifier = inventoryStockNotifier;
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

        Inventory updated = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new InventoryNotFoundException(inventoryId));

        notifyAfterCommit(inventoryId, updated.quantity());

        return updated;
    }

    private void notifyAfterCommit(InventoryId inventoryId, int quantity) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    inventoryStockNotifier.notifyStockChanged(inventoryId, quantity);
                }
            });
        } else {
            inventoryStockNotifier.notifyStockChanged(inventoryId, quantity);
        }
    }
}