package com.ezequielnascimento.highcontention.product.domain.port.in;

import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;

import java.math.BigDecimal;

public interface ChangeProductPriceUseCase {
    Product execute(ProductId productId, BigDecimal price);
}
