package com.ezequielnascimento.highcontention.product.application;

import com.ezequielnascimento.highcontention.product.domain.exceptions.ProductNotFoundException;
import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import com.ezequielnascimento.highcontention.product.domain.repository.ProductRepository;

public class DeactivateProduct {

    private final ProductRepository productRepository;

    public DeactivateProduct(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product execute(ProductId productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.deactivate();

        return productRepository.save(product);
    }
}
