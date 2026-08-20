package com.ezequielnascimento.highcontention.inventory.domain.port.in;

import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;

public interface GetInventoryByProductIdUseCase {
    Inventory execute(ProductId productId);
}
