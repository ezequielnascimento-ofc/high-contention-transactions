package com.ezequielnascimento.highcontention.inventory.domain.port.in;

import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;

public interface GetInventoryUseCase {
    Inventory execute(InventoryId inventoryId);
}