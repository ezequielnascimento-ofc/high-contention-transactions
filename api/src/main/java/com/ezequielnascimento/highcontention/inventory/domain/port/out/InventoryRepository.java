package com.ezequielnascimento.highcontention.inventory.domain.port.out;

import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;

import java.util.Optional;

public interface InventoryRepository {

    Inventory save(Inventory inventory);
    Optional<Inventory> findById(InventoryId id);
    Optional<Inventory> findByProductId(ProductId productId);
    boolean existsByProductId(ProductId productId);
    void delete(Inventory inventory);
}
