package com.ezequielnascimento.highcontention.product.application;

import com.ezequielnascimento.highcontention.product.domain.exceptions.ProductNotFoundException;
import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import com.ezequielnascimento.highcontention.product.domain.repository.ProductRepository;

import java.math.BigDecimal;

public class ChangeProductPrice {

    private final ProductRepository productRepository;

    public ChangeProductPrice(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product execute(ProductId productId, BigDecimal price) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId));
        product.changePrice(price);
        return productRepository.save(product);
    }
}
