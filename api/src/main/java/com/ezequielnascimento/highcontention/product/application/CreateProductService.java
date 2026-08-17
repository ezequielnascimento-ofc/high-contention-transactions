package com.ezequielnascimento.highcontention.product.application;

import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.port.in.CreateProductUseCase;
import com.ezequielnascimento.highcontention.product.domain.port.out.ProductRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

public class CreateProductService implements CreateProductUseCase {

    private final ProductRepository productRepository;

    public CreateProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public Product execute(String name, String description, BigDecimal price) {
        Product product = Product.create(name, description, price);
        return productRepository.save(product);
    }
}
