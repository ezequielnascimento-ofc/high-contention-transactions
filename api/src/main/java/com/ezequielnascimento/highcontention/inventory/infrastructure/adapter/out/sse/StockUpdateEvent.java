package com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.out.sse;

import java.time.Instant;
import java.util.UUID;

public record StockUpdateEvent(UUID inventoryId, int quantity, Instant timestamp) {
}