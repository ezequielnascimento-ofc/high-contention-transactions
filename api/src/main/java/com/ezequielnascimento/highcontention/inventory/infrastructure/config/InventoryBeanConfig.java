package com.ezequielnascimento.highcontention.inventory.infrastructure.config;

import com.ezequielnascimento.highcontention.inventory.application.CreateInventoryService;
import com.ezequielnascimento.highcontention.inventory.application.DecreaseStockService;
import com.ezequielnascimento.highcontention.inventory.application.GetInventoryService;
import com.ezequielnascimento.highcontention.inventory.application.IncreaseStockService;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.InventoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InventoryBeanConfig {

    @Bean
    CreateInventoryService createInventoryService(InventoryRepository inventoryRepository) {
        return new CreateInventoryService(inventoryRepository);
    }

    @Bean
    GetInventoryService getInventoryService(InventoryRepository inventoryRepository) {
        return new GetInventoryService(inventoryRepository);
    }

    @Bean
    IncreaseStockService increaseStockService(InventoryRepository inventoryRepository) {
        return new IncreaseStockService(inventoryRepository);
    }

    @Bean
    DecreaseStockService decreaseStockService(InventoryRepository inventoryRepository) {
        return new DecreaseStockService(inventoryRepository);
    }
}