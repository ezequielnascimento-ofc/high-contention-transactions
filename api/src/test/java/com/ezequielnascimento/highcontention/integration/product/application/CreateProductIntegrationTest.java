package com.ezequielnascimento.highcontention.integration.product.application;

import com.ezequielnascimento.highcontention.product.application.CreateProductService;
import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.port.out.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CreateProductIntegrationTest {

    @Autowired
    private CreateProductService createProductService;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void shouldCreateProductAndPersistItInDatabase() {
        BigDecimal price = new BigDecimal("499.90");

        Product product = createProductService.execute("Keyboard", "Mechanical keyboard", price);

        assertNotNull(product);
        assertNotNull(product.id());

        Product persistedProduct = productRepository.findById(product.id()).orElseThrow();

        assertEquals(product.id(), persistedProduct.id());
        assertEquals("Keyboard", persistedProduct.name());
        assertEquals("Mechanical keyboard", persistedProduct.description());
        assertEquals(price, persistedProduct.price());
        assertEquals(product.status(), persistedProduct.status());
    }
}