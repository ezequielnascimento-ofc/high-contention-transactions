package com.ezequielnascimento.highcontention.product.application;

import com.ezequielnascimento.highcontention.product.domain.exceptions.ProductNotFoundException;
import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import com.ezequielnascimento.highcontention.product.domain.port.in.ChangeProductDescriptionUseCase;
import com.ezequielnascimento.highcontention.product.domain.port.out.ProductRepository;
import org.springframework.transaction.annotation.Transactional;

public class ChangeProductDescriptionService implements ChangeProductDescriptionUseCase {

    private final ProductRepository productRepository;

    public ChangeProductDescriptionService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public Product execute(ProductId productId, String description) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.changeDescription(description);
        return productRepository.save(product);
    }
}
