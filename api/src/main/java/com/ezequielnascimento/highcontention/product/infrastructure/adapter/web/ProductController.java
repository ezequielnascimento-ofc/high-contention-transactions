package com.ezequielnascimento.highcontention.product.infrastructure.adapter.web;

import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import com.ezequielnascimento.highcontention.product.domain.port.in.*;
import com.ezequielnascimento.highcontention.product.infrastructure.adapter.web.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final GetProductUseCase getProductUseCase;
    private final RenameProductUseCase renameProductUseCase;
    private final ChangeProductPriceUseCase changeProductPriceUseCase;
    private final ChangeProductDescriptionUseCase changeProductDescriptionUseCase;
    private final ActivateProductUseCase activateProductUseCase;
    private final DeactivateProductUseCase deactivateProductUseCase;
    private final ListProductsUseCase listProductsUseCase; // novo campo


    public ProductController(
            CreateProductUseCase createProductUseCase,
            GetProductUseCase getProductUseCase,
            RenameProductUseCase renameProductUseCase,
            ChangeProductPriceUseCase changeProductPriceUseCase,
            ChangeProductDescriptionUseCase changeProductDescriptionUseCase,
            ActivateProductUseCase activateProductUseCase,
            DeactivateProductUseCase deactivateProductUseCase, ListProductsUseCase listProductsUseCase
    ) {
        this.createProductUseCase = createProductUseCase;
        this.getProductUseCase = getProductUseCase;
        this.renameProductUseCase = renameProductUseCase;
        this.changeProductPriceUseCase = changeProductPriceUseCase;
        this.changeProductDescriptionUseCase = changeProductDescriptionUseCase;
        this.activateProductUseCase = activateProductUseCase;
        this.deactivateProductUseCase = deactivateProductUseCase;
        this.listProductsUseCase = listProductsUseCase;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        var product = createProductUseCase.execute(request.name(), request.description(), request.price());
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductResponse.from(product));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> get(@PathVariable UUID id) {
        var product = getProductUseCase.execute(new ProductId(id));
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    @PatchMapping("/{id}/name")
    public ResponseEntity<ProductResponse> rename(
            @PathVariable UUID id, @Valid @RequestBody RenameProductRequest request) {
        var product = renameProductUseCase.execute(new ProductId(id), request.name());
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    @PatchMapping("/{id}/price")
    public ResponseEntity<ProductResponse> changePrice(
            @PathVariable UUID id, @Valid @RequestBody ChangeProductPriceRequest request) {
        var product = changeProductPriceUseCase.execute(new ProductId(id), request.price());
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    @PatchMapping("/{id}/description")
    public ResponseEntity<ProductResponse> changeDescription(
            @PathVariable UUID id, @RequestBody ChangeProductDescriptionRequest request) {
        var product = changeProductDescriptionUseCase.execute(new ProductId(id), request.description());
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ProductResponse> activate(@PathVariable UUID id) {
        var product = activateProductUseCase.execute(new ProductId(id));
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ProductResponse> deactivate(@PathVariable UUID id) {
        var product = deactivateProductUseCase.execute(new ProductId(id));
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> list() {
        var products = listProductsUseCase.execute()
                .stream()
                .map(ProductResponse::from)
                .toList();
        return ResponseEntity.ok(products);
    }
}