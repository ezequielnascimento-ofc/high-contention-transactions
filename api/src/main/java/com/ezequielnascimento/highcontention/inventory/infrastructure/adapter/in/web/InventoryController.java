package com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.in.web;

import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;
import com.ezequielnascimento.highcontention.inventory.domain.port.in.*;
import com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.in.web.dto.AdjustStockRequest;
import com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.in.web.dto.CreateInventoryRequest;
import com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.in.web.dto.InventoryResponse;
import com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.out.sse.SseInventoryStockNotifier;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@Tag(name = "Inventory", description = "Stock per product; the contended resource")
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

    @Operation(summary = "Stream stock changes", description = "Server-Sent Events: one stock-update event after each committed change to this inventory.")
    @GetMapping(value = "/{id}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable UUID id) {
        return sseInventoryStockNotifier.subscribe(new InventoryId(id));
    }

    @Operation(summary = "Create the inventory of a product")
    @ApiResponse(responseCode = "409", description = "The product already has an inventory (inventory-already-exists)",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(ref = "#/components/schemas/Problem")))
    @ApiResponse(responseCode = "422", description = "The product does not exist (unknown-product)",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(ref = "#/components/schemas/Problem")))
    @PostMapping
    public ResponseEntity<InventoryResponse> create(@Valid @RequestBody CreateInventoryRequest request) {
        var inventory = createInventoryUseCase.execute(new ProductId(request.productId()), request.quantity());
        return ResponseEntity.status(HttpStatus.CREATED).body(InventoryResponse.from(inventory));
    }

    @Operation(summary = "Get an inventory")
    @GetMapping("/{id}")
    public ResponseEntity<InventoryResponse> get(@PathVariable UUID id) {
        var inventory = getInventoryUseCase.execute(new InventoryId(id));
        return ResponseEntity.ok(InventoryResponse.from(inventory));
    }

    @Operation(summary = "Increase stock")
    @PostMapping("/{id}/increase")
    public ResponseEntity<InventoryResponse> increase(
            @PathVariable UUID id, @Valid @RequestBody AdjustStockRequest request) {
        var inventory = increaseStockUseCase.execute(new InventoryId(id), request.quantity());
        return ResponseEntity.ok(InventoryResponse.from(inventory));
    }

    @Operation(summary = "Decrease stock", description = "The contended operation. A single conditional UPDATE decreases the stock only if enough is available, so concurrent requests can never oversell.")
    @ApiResponse(responseCode = "409", description = "Not enough stock; nothing was changed (insufficient-stock)",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(ref = "#/components/schemas/Problem")))
    @PostMapping("/{id}/decrease")
    public ResponseEntity<InventoryResponse> decrease(
            @PathVariable UUID id, @Valid @RequestBody AdjustStockRequest request) {
        var inventory = decreaseStockUseCase.execute(new InventoryId(id), request.quantity());
        return ResponseEntity.ok(InventoryResponse.from(inventory));
    }

    @Operation(summary = "Find the inventory of a product")
    @GetMapping(params = "productId")
    public ResponseEntity<InventoryResponse> getByProductId(@RequestParam UUID productId) {
        var inventory = getInventoryByProductIdUseCase.execute(new ProductId(productId));
        return ResponseEntity.ok(InventoryResponse.from(inventory));
    }
}