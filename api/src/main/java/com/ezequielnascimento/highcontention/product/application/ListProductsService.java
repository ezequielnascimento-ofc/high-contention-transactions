package com.ezequielnascimento.highcontention.product.application;

import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.port.in.ListProductsUseCase;
import com.ezequielnascimento.highcontention.product.domain.port.out.ProductRepository;

import java.util.List;

public class ListProductsService implements ListProductsUseCase {

    private final ProductRepository productRepository;

    public ListProductsService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }


    @Override
    public List<Product> execute() {
        return productRepository.findAll();
    }
}
