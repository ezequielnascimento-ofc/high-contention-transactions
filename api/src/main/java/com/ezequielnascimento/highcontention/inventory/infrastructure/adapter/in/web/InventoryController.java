package com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.in.web;

import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;
import com.ezequielnascimento.highcontention.inventory.domain.port.in.*;
import com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.in.web.dto.AdjustStockRequest;
import com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.in.web.dto.CreateInventoryRequest;
import com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.in.web.dto.InventoryResponse;
import com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.out.sse.SseInventoryStockNotifier;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventories")
public class InventoryController {

    private final CreateInventoryUseCase createInventoryUseCase;
    private final GetInventoryUseCase getInventoryUseCase;
    private final IncreaseStockUseCase increaseStockUseCase;
    private final DecreaseStockUseCase decreaseStockUseCase;
    private final SseInventoryStockNotifier sseInventoryStockNotifier;
    private final GetInventoryByProductIdUseCase getInventoryByProductIdUseCase;

    public InventoryController(
            CreateInventoryUseCase createInventoryUseCase,
            GetInventoryUseCase getInventoryUseCase,
            IncreaseStockUseCase increaseStockUseCase,
            DecreaseStockUseCase decreaseStockUseCase, SseInventoryStockNotifier sseInventoryStockNotifier, GetInventoryByProductIdUseCase getInventoryByProductIdUseCase
    ) {
        this.createInventoryUseCase = createInventoryUseCase;
        this.getInventoryUseCase = getInventoryUseCase;
        this.increaseStockUseCase = increaseStockUseCase;
        this.decreaseStockUseCase = decreaseStockUseCase;
        this.sseInventoryStockNotifier = sseInventoryStockNotifier;
        this.getInventoryByProductIdUseCase = getInventoryByProductIdUseCase;
    }

    @GetMapping(value = "/{id}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable UUID id) {
        return sseInventoryStockNotifier.subscribe(new InventoryId(id));
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

    @GetMapping(params = "productId")
    public ResponseEntity<InventoryResponse> getByProductId(@RequestParam UUID productId) {
        var inventory = getInventoryByProductIdUseCase.execute(new ProductId(productId));
        return ResponseEntity.ok(InventoryResponse.from(inventory));
    }
}