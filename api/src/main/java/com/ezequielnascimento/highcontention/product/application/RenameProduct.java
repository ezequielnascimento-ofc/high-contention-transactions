package com.ezequielnascimento.highcontention.product.application;

import com.ezequielnascimento.highcontention.product.domain.exceptions.ProductNotFoundException;
import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import com.ezequielnascimento.highcontention.product.domain.repository.ProductRepository;

public class RenameProduct {

    private final ProductRepository productRepository;

    public RenameProduct(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product execute(ProductId productId, String name) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.rename(name);

        return productRepository.save(product);
    }
}
