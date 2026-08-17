package com.ezequielnascimento.highcontention.inventory.infrastructure.config;

import com.ezequielnascimento.highcontention.inventory.application.CreateInventoryService;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.InventoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InventoryBeanConfig {

    @Bean
    CreateInventoryService createInventory(InventoryRepository inventoryRepository) {
        return new CreateInventoryService(inventoryRepository);
    }
}
