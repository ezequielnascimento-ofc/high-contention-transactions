package com.ezequielnascimento.highcontention.product.infrastructure.config;

import com.ezequielnascimento.highcontention.product.application.*;
import com.ezequielnascimento.highcontention.product.domain.port.in.ListProductsUseCase;
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

    @Bean
    public ListProductsUseCase listProductsUseCase(ProductRepository productRepository) {
        return new ListProductsService(productRepository);
    }
}