package com.ezequielnascimento.highcontention.product.domain.port.in;

import com.ezequielnascimento.highcontention.product.domain.model.Product;

import java.util.List;

public interface ListProductsUseCase {
    List<Product> execute();
}
