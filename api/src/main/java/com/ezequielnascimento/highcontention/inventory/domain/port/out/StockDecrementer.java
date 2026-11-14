package com.ezequielnascimento.highcontention.inventory.domain.port.out;

import com.ezequielnascimento.highcontention.inventory.domain.exceptions.ConcurrencyConflictException;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InsufficientStockException;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InventoryNotFoundException;
import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;

/**
 * Decreases stock under concurrency. Implementations differ only in how they
 * keep the invariants (never negative, no lost update) when many callers hit
 * the same row; each one commits its own work before returning.
 */
public interface StockDecrementer {

    /**
     * @return the inventory as committed after the decrease
     * @throws InsufficientStockException  if the stock is lower than {@code quantity}; nothing changes
     * @throws InventoryNotFoundException   if the inventory does not exist
     * @throws ConcurrencyConflictException if a retrying strategy ran out of attempts; nothing changes
     */
    Inventory decrement(InventoryId inventoryId, int quantity);
}
