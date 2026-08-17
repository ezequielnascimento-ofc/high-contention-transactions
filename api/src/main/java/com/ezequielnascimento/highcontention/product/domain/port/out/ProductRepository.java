package com.ezequielnascimento.highcontention.product.domain.port.out;

import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;

import java.util.Optional;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(ProductId id);
    boolean existsById(ProductId id);
    void delete(Product product);
}
