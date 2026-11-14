package com.ezequielnascimento.highcontention.inventory.application;

import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InvalidInventoryException;
import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;
import com.ezequielnascimento.highcontention.inventory.domain.port.in.DecreaseStockUseCase;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.InventoryStockNotifier;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.StockDecrementer;

/**
 * Not {@code @Transactional} on purpose: each {@link StockDecrementer} owns its
 * transactions, because the retrying strategies need a fresh transaction per
 * attempt and the serializable one needs its own isolation level. By the time
 * {@code decrement} returns, the change is committed, so subscribers are
 * notified right away.
 */
public class DecreaseStockService implements DecreaseStockUseCase {

    private final StockDecrementer stockDecrementer;
    private final InventoryStockNotifier inventoryStockNotifier;

    public DecreaseStockService(StockDecrementer stockDecrementer, InventoryStockNotifier inventoryStockNotifier) {
        this.stockDecrementer = stockDecrementer;
        this.inventoryStockNotifier = inventoryStockNotifier;
    }

    @Override
    public Inventory execute(InventoryId inventoryId, int quantity) {
        if (quantity <= 0) {
            throw new InvalidInventoryException("Decrease quantity must be greater than zero");
        }
        Inventory updated = stockDecrementer.decrement(inventoryId, quantity);
        inventoryStockNotifier.notifyStockChanged(inventoryId, updated.quantity());
        return updated;
    }
}
