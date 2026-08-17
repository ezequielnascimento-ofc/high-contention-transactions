package com.ezequielnascimento.highcontention.product.application;

import com.ezequielnascimento.highcontention.product.domain.exceptions.ProductNotFoundException;
import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import com.ezequielnascimento.highcontention.product.domain.repository.ProductRepository;

public class GetProduct {
    private final ProductRepository productRepository;

    public GetProduct(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product execute(ProductId productId) {
        return productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId));
    }
}
