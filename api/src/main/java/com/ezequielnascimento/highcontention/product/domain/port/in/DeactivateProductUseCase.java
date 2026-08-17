package com.ezequielnascimento.highcontention.product.domain.port.in;

import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;

public interface DeactivateProductUseCase {
    Product execute(ProductId productId);
}
