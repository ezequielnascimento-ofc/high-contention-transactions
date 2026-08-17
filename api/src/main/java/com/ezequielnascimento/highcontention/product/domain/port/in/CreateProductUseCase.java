package com.ezequielnascimento.highcontention.product.domain.port.in;

import com.ezequielnascimento.highcontention.product.domain.model.Product;

import java.math.BigDecimal;

public interface CreateProductUseCase {
    Product execute(String name, String description, BigDecimal price);
}
