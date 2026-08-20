package com.ezequielnascimento.highcontention.inventory.domain.port.out;

import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;

public interface InventoryStockNotifier {
    void notifyStockChanged(InventoryId inventoryId, int quantity);
}
