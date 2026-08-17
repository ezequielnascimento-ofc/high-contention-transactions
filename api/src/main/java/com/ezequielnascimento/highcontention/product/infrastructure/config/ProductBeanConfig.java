package com.ezequielnascimento.highcontention.product.infrastructure.config;

import com.ezequielnascimento.highcontention.product.application.ActivateProductService;
import com.ezequielnascimento.highcontention.product.application.ChangeProductDescriptionService;
import com.ezequielnascimento.highcontention.product.application.ChangeProductPriceService;
import com.ezequielnascimento.highcontention.product.application.CreateProductService;
import com.ezequielnascimento.highcontention.product.application.DeactivateProductService;
import com.ezequielnascimento.highcontention.product.application.GetProductService;
import com.ezequielnascimento.highcontention.product.application.RenameProductService;
import com.ezequielnascimento.highcontention.product.domain.port.out.ProductRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProductBeanConfig {

    @Bean
    CreateProductService createProductService(ProductRepository productRepository) {
        return new CreateProductService(productRepository);
    }

    @Bean
    GetProductService getProductService(ProductRepository productRepository) {
        return new GetProductService(productRepository);
    }

    @Bean
    RenameProductService renameProductService(ProductRepository productRepository) {
        return new RenameProductService(productRepository);
    }

    @Bean
    ChangeProductPriceService changeProductPriceService(ProductRepository productRepository) {
        return new ChangeProductPriceService(productRepository);
    }

    @Bean
    ChangeProductDescriptionService changeProductDescriptionService(ProductRepository productRepository) {
        return new ChangeProductDescriptionService(productRepository);
    }

    @Bean
    ActivateProductService activateProductService(ProductRepository productRepository) {
        return new ActivateProductService(productRepository);
    }

    @Bean
    DeactivateProductService deactivateProductService(ProductRepository productRepository) {
        return new DeactivateProductService(productRepository);
    }
}