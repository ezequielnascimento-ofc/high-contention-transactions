package com.ezequielnascimento.highcontention.inventory.application;

import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.port.in.CreateInventoryUseCase;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.InventoryRepository;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import org.springframework.transaction.annotation.Transactional;

public class CreateInventoryService implements CreateInventoryUseCase {

    private final InventoryRepository inventoryRepository;

    public CreateInventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    @Transactional
    public Inventory execute(ProductId productId, int quantity) {
        Inventory inventory = Inventory.create(productId, quantity);
        return inventoryRepository.save(inventory);
    }
}