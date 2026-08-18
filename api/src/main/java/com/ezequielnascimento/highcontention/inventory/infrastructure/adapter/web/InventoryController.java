package com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.web;

import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;
import com.ezequielnascimento.highcontention.inventory.domain.port.in.CreateInventoryUseCase;
import com.ezequielnascimento.highcontention.inventory.domain.port.in.DecreaseStockUseCase;
import com.ezequielnascimento.highcontention.inventory.domain.port.in.GetInventoryUseCase;
import com.ezequielnascimento.highcontention.inventory.domain.port.in.IncreaseStockUseCase;
import com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.web.dto.AdjustStockRequest;
import com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.web.dto.CreateInventoryRequest;
import com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.web.dto.InventoryResponse;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventories")
public class InventoryController {

    private final CreateInventoryUseCase createInventoryUseCase;
    private final GetInventoryUseCase getInventoryUseCase;
    private final IncreaseStockUseCase increaseStockUseCase;
    private final DecreaseStockUseCase decreaseStockUseCase;

    public InventoryController(
            CreateInventoryUseCase createInventoryUseCase,
            GetInventoryUseCase getInventoryUseCase,
            IncreaseStockUseCase increaseStockUseCase,
            DecreaseStockUseCase decreaseStockUseCase
    ) {
        this.createInventoryUseCase = createInventoryUseCase;
        this.getInventoryUseCase = getInventoryUseCase;
        this.increaseStockUseCase = increaseStockUseCase;
        this.decreaseStockUseCase = decreaseStockUseCase;
    }

    @PostMapping
    public ResponseEntity<InventoryResponse> create(@Valid @RequestBody CreateInventoryRequest request) {
        var inventory = createInventoryUseCase.execute(new ProductId(request.productId()), request.quantity());
        return ResponseEntity.status(HttpStatus.CREATED).body(InventoryResponse.from(inventory));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryResponse> get(@PathVariable UUID id) {
        var inventory = getInventoryUseCase.execute(new InventoryId(id));
        return ResponseEntity.ok(InventoryResponse.from(inventory));
    }

    @PostMapping("/{id}/increase")
    public ResponseEntity<InventoryResponse> increase(
            @PathVariable UUID id, @Valid @RequestBody AdjustStockRequest request) {
        var inventory = increaseStockUseCase.execute(new InventoryId(id), request.quantity());
        return ResponseEntity.ok(InventoryResponse.from(inventory));
    }

    @PostMapping("/{id}/decrease")
    public ResponseEntity<InventoryResponse> decrease(
            @PathVariable UUID id, @Valid @RequestBody AdjustStockRequest request) {
        var inventory = decreaseStockUseCase.execute(new InventoryId(id), request.quantity());
        return ResponseEntity.ok(InventoryResponse.from(inventory));
    }
}