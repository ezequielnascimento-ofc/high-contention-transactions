package com.ezequielnascimento.highcontention.inventory.application;

import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InsufficientStockException;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InvalidInventoryException;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InventoryNotFoundException;
import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;
import com.ezequielnascimento.highcontention.inventory.domain.port.in.DecreaseStockUseCase;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.InventoryRepository;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.InventoryStockNotifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class DecreaseStockService implements DecreaseStockUseCase {

    private final InventoryRepository inventoryRepository;
    private final InventoryStockNotifier inventoryStockNotifier;

    public DecreaseStockService(InventoryRepository inventoryRepository, InventoryStockNotifier inventoryStockNotifier) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryStockNotifier = inventoryStockNotifier;
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