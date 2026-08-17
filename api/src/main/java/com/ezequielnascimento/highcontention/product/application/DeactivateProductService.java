package com.ezequielnascimento.highcontention.product.application;


import com.ezequielnascimento.highcontention.product.domain.exceptions.ProductNotFoundException;
import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import com.ezequielnascimento.highcontention.product.domain.port.in.DeactivateProductUseCase;
import com.ezequielnascimento.highcontention.product.domain.port.out.ProductRepository;
import org.springframework.transaction.annotation.Transactional;

public class DeactivateProductService implements DeactivateProductUseCase {

    private final ProductRepository productRepository;

    public DeactivateProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }


    @Override
    @Transactional
    public Product execute(ProductId productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.deactivate();
        return productRepository.save(product);
    }
}
