package com.ezequielnascimento.highcontention.inventory.infrastructure.config;

import com.ezequielnascimento.highcontention.inventory.application.*;
import com.ezequielnascimento.highcontention.inventory.domain.port.in.GetInventoryByProductIdUseCase;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.InventoryRepository;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.InventoryStockNotifier;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.StockDecrementer;
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
    IncreaseStockService increaseStockService(
            InventoryRepository inventoryRepository, InventoryStockNotifier inventoryStockNotifier) {
        return new IncreaseStockService(inventoryRepository, inventoryStockNotifier);
    }

    @Bean
    DecreaseStockService decreaseStockService(
            StockDecrementer stockDecrementer, InventoryStockNotifier inventoryStockNotifier) {
        return new DecreaseStockService(stockDecrementer, inventoryStockNotifier);
    }

    @Bean
    public GetInventoryByProductIdUseCase getInventoryByProductIdUseCase(InventoryRepository inventoryRepository) {
        return new GetInventoryByProductIdService(inventoryRepository);
    }
}