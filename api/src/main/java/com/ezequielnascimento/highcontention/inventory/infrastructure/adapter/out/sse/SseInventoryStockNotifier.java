package com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.out.sse;

import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.InventoryStockNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory SSE registry and publisher for inventory stock changes.
 * <p>
 * Holds active {@link SseEmitter} connections per {@link InventoryId} and
 * pushes stock-update events to all subscribers when notified.
 * <p>
 * This is a single-instance, in-memory implementation: it only reaches
 * clients connected to the same application instance that processed the
 * update. Broadcasting across multiple instances would require an external
 * pub/sub mechanism (e.g., Redis Pub/Sub) — intentionally out of scope here
 * (see docs/problem.md, "Deferred / Not Yet Implemented").
 */
@Component
public class SseInventoryStockNotifier implements InventoryStockNotifier {

    private static final Logger log = LoggerFactory.getLogger(SseInventoryStockNotifier.class);
    private static final long EMITTER_TIMEOUT_MILLIS = 30 * 60 * 1000L; // 30 minutes

    private final Map<InventoryId, List<SseEmitter>> emittersByInventoryId = new ConcurrentHashMap<>();

    public SseEmitter subscribe(InventoryId inventoryId) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MILLIS);

        emittersByInventoryId
                .computeIfAbsent(inventoryId, id -> new CopyOnWriteArrayList<>())
                .add(emitter);

        emitter.onCompletion(() -> removeEmitter(inventoryId, emitter));
        emitter.onTimeout(() -> {
            emitter.complete();
            removeEmitter(inventoryId, emitter);
        });
        emitter.onError(throwable -> removeEmitter(inventoryId, emitter));

        return emitter;
    }

    @Override
    public void notifyStockChanged(InventoryId inventoryId, int quantity) {
        List<SseEmitter> emitters = emittersByInventoryId.get(inventoryId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        StockUpdateEvent payload = new StockUpdateEvent(inventoryId.value(), quantity, Instant.now());

        for (SseEmitter emitter : emitters) {
            sendSafely(inventoryId, emitter, payload);
        }
    }

    private void sendSafely(InventoryId inventoryId, SseEmitter emitter, StockUpdateEvent payload) {
        try {
            emitter.send(SseEmitter.event()
                    .name("stock-update")
                    .data(payload));
        } catch (IOException | IllegalStateException e) {
            log.debug("Removing dead SSE emitter for inventory {}: {}", inventoryId.value(), e.getMessage());
            emitter.complete(); // trocado de completeWithError(e)
            removeEmitter(inventoryId, emitter);
        }
    }

    private void removeEmitter(InventoryId inventoryId, SseEmitter emitter) {
        List<SseEmitter> emitters = emittersByInventoryId.get(inventoryId);
        if (emitters == null) {
            return;
        }

        emitters.remove(emitter);

        if (emitters.isEmpty()) {
            emittersByInventoryId.remove(inventoryId, emitters);
        }
    }
}