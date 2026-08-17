package com.ezequielnascimento.highcontention.product.application;

import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.repository.ProductRepository;

import java.math.BigDecimal;

public class CreateProduct {
    private final ProductRepository productRepository;

    public CreateProduct(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product execute(String name, String description, BigDecimal price) {
        Product product = Product.create(name, description, price);
        return productRepository.save(product);
    }
}
