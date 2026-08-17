package com.ezequielnascimento.highcontention.integration.inventory.infrastructure;

import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.InventoryRepository;
import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import com.ezequielnascimento.highcontention.product.domain.port.out.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class JdbcInventoryRepositoryTest {

    @Autowired
    private InventoryRepository repository;

    @Autowired
    private ProductRepository productRepository;

    private Product product;

    @BeforeEach
    void setUp() {
        product = Product.create(
                "Mechanical Keyboard",
                "High-performance mechanical keyboard",
                new BigDecimal("499.90")
        );

        productRepository.save(product);
    }

    @Test
    void shouldSaveInventory() {
        Inventory inventory = Inventory.create(
                product.id(),
                100
        );

        Inventory saved = repository.save(inventory);
        assertEquals(inventory.id(), saved.id());
    }

    @Test
    void shouldFindInventoryById() {
        Inventory inventory = Inventory.create(
                product.id(),
                100
        );

        repository.save(inventory);

        Optional<Inventory> result = repository.findById(inventory.id());

        assertTrue(result.isPresent());

        Inventory found = result.get();

        assertEquals(inventory.id(), found.id());
        assertEquals(inventory.productId(), found.productId());
        assertEquals(inventory.quantity(), found.quantity());
        assertEquals(inventory.createdAt(), found.createdAt());
        assertEquals(inventory.updatedAt(), found.updatedAt());
    }

    @Test
    void shouldFindInventoryByProductId() {
        Inventory inventory = Inventory.create(
                product.id(),
                100
        );

        repository.save(inventory);

        Optional<Inventory> result =
                repository.findByProductId(product.id());

        assertTrue(result.isPresent());

        Inventory found = result.get();

        assertEquals(inventory.id(), found.id());
        assertEquals(inventory.productId(), found.productId());
        assertEquals(inventory.quantity(), found.quantity());
    }

    @Test
    void shouldReturnEmptyWhenInventoryDoesNotExistForProduct() {
        ProductId productId = ProductId.generate();

        Optional<Inventory> result =
                repository.findByProductId(productId);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnTrueWhenInventoryExistsForProduct() {
        Inventory inventory = Inventory.create(
                product.id(),
                100
        );

        repository.save(inventory);

        assertTrue(repository.existsByProductId(product.id()));
    }

    @Test
    void shouldReturnFalseWhenInventoryDoesNotExistForProduct() {
        ProductId productId = ProductId.generate();

        assertFalse(repository.existsByProductId(productId));
    }

    @Test
    void shouldDeleteInventory() {
        Inventory inventory = Inventory.create(
                product.id(),
                100
        );

        repository.save(inventory);
        assertTrue(repository.findById(inventory.id()).isPresent());

        repository.delete(inventory);
        assertTrue(repository.findById(inventory.id()).isEmpty());
    }
}
