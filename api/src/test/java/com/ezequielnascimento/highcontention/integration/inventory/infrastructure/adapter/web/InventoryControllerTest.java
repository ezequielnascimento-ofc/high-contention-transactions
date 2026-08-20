package com.ezequielnascimento.highcontention.integration.inventory.infrastructure.adapter.web;

import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InsufficientStockException;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InventoryNotFoundException;
import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;
import com.ezequielnascimento.highcontention.inventory.domain.port.in.CreateInventoryUseCase;
import com.ezequielnascimento.highcontention.inventory.domain.port.in.DecreaseStockUseCase;
import com.ezequielnascimento.highcontention.inventory.domain.port.in.GetInventoryUseCase;
import com.ezequielnascimento.highcontention.inventory.domain.port.in.IncreaseStockUseCase;
import com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.in.web.InventoryController;
import com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.out.sse.SseInventoryStockNotifier;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SseInventoryStockNotifier sseInventoryStockNotifier;

    @MockitoBean
    private CreateInventoryUseCase createInventoryUseCase;
    @MockitoBean
    private GetInventoryUseCase getInventoryUseCase;
    @MockitoBean
    private IncreaseStockUseCase increaseStockUseCase;
    @MockitoBean
    private DecreaseStockUseCase decreaseStockUseCase;

    private Inventory sampleInventory(int quantity) {
        return Inventory.create(ProductId.generate(), quantity);
    }

    @Nested
    class Create {

        @Test
        void shouldReturn201WhenInventoryIsCreated() throws Exception {
            ProductId productId = ProductId.generate();
            Inventory inventory = Inventory.create(productId, 100);
            when(createInventoryUseCase.execute(eq(productId), eq(100))).thenReturn(inventory);

            String requestBody = """
                    {"productId":"%s","quantity":100}
                    """.formatted(productId.value());

            mockMvc.perform(post("/api/v1/inventories")
                            .contentType("application/json")
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.quantity").value(100));
        }

        @Test
        void shouldReturn400WhenQuantityIsNotPositive() throws Exception {
            String requestBody = """
                    {"productId":"%s","quantity":0}
                    """.formatted(ProductId.generate().value());

            mockMvc.perform(post("/api/v1/inventories")
                            .contentType("application/json")
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn400WhenProductIdIsMissing() throws Exception {
            mockMvc.perform(post("/api/v1/inventories")
                            .contentType("application/json")
                            .content("{\"quantity\":10}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class Get {

        @Test
        void shouldReturn200WithInventoryWhenItExists() throws Exception {
            Inventory inventory = sampleInventory(100);
            when(getInventoryUseCase.execute(inventory.id())).thenReturn(inventory);

            mockMvc.perform(get("/api/v1/inventories/{id}", inventory.id().value()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.quantity").value(100));
        }

        @Test
        void shouldReturn404WhenInventoryDoesNotExist() throws Exception {
            InventoryId id = InventoryId.generate();
            when(getInventoryUseCase.execute(id)).thenThrow(new InventoryNotFoundException(id));

            mockMvc.perform(get("/api/v1/inventories/{id}", id.value()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class Increase {

        @Test
        void shouldReturn200WithIncreasedQuantity() throws Exception {
            InventoryId id = InventoryId.generate();
            Inventory updated = Inventory.reconstitute(id, ProductId.generate(), 150,
                    java.time.Instant.now(), java.time.Instant.now());
            when(increaseStockUseCase.execute(eq(id), eq(50))).thenReturn(updated);

            mockMvc.perform(post("/api/v1/inventories/{id}/increase", id.value())
                            .contentType("application/json")
                            .content("{\"quantity\":50}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.quantity").value(150));
        }

        @Test
        void shouldReturn400WhenQuantityIsNotPositive() throws Exception {
            InventoryId id = InventoryId.generate();

            mockMvc.perform(post("/api/v1/inventories/{id}/increase", id.value())
                            .contentType("application/json")
                            .content("{\"quantity\":0}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class Decrease {

        @Test
        void shouldReturn200WithDecreasedQuantity() throws Exception {
            InventoryId id = InventoryId.generate();
            Inventory updated = Inventory.reconstitute(id, ProductId.generate(), 70,
                    java.time.Instant.now(), java.time.Instant.now());
            when(decreaseStockUseCase.execute(eq(id), eq(30))).thenReturn(updated);

            mockMvc.perform(post("/api/v1/inventories/{id}/decrease", id.value())
                            .contentType("application/json")
                            .content("{\"quantity\":30}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.quantity").value(70));
        }

        @Test
        void shouldReturn409WhenStockIsInsufficient() throws Exception {
            InventoryId id = InventoryId.generate();
            when(decreaseStockUseCase.execute(eq(id), eq(150)))
                    .thenThrow(new InsufficientStockException(id, 100, 150));

            mockMvc.perform(post("/api/v1/inventories/{id}/decrease", id.value())
                            .contentType("application/json")
                            .content("{\"quantity\":150}"))
                    .andExpect(status().isConflict());
        }

        @Test
        void shouldReturn404WhenInventoryDoesNotExist() throws Exception {
            InventoryId id = InventoryId.generate();
            when(decreaseStockUseCase.execute(eq(id), eq(10)))
                    .thenThrow(new InventoryNotFoundException(id));

            mockMvc.perform(post("/api/v1/inventories/{id}/decrease", id.value())
                            .contentType("application/json")
                            .content("{\"quantity\":10}"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class Stream {

        @Test
        void shouldOpenSseConnectionWithCorrectContentType() throws Exception {
            InventoryId id = InventoryId.generate();
            SseEmitter emitter = new SseEmitter();
            when(sseInventoryStockNotifier.subscribe(id)).thenReturn(emitter);

            MvcResult result = mockMvc.perform(get("/api/v1/inventories/{id}/stream", id.value()))
                    .andExpect(request().asyncStarted())
                    .andReturn();

            emitter.complete();

            mockMvc.perform(asyncDispatch(result))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM));
        }

        @Test
        void shouldSubscribeUsingThePathVariableInventoryId() throws Exception {
            InventoryId id = InventoryId.generate();
            SseEmitter emitter = new SseEmitter();
            when(sseInventoryStockNotifier.subscribe(id)).thenReturn(emitter);

            MvcResult result = mockMvc.perform(get("/api/v1/inventories/{id}/stream", id.value()))
                    .andExpect(request().asyncStarted())
                    .andReturn();

            emitter.complete();
            mockMvc.perform(asyncDispatch(result));

            verify(sseInventoryStockNotifier).subscribe(id);
        }

        @Test
        void shouldReturnDistinctEmitterPerSubscription() throws Exception {
            InventoryId firstId = InventoryId.generate();
            InventoryId secondId = InventoryId.generate();
            SseEmitter firstEmitter = new SseEmitter();
            SseEmitter secondEmitter = new SseEmitter();
            when(sseInventoryStockNotifier.subscribe(firstId)).thenReturn(firstEmitter);
            when(sseInventoryStockNotifier.subscribe(secondId)).thenReturn(secondEmitter);

            MvcResult firstResult = mockMvc.perform(get("/api/v1/inventories/{id}/stream", firstId.value()))
                    .andExpect(request().asyncStarted())
                    .andReturn();
            firstEmitter.complete();
            mockMvc.perform(asyncDispatch(firstResult)).andExpect(status().isOk());

            MvcResult secondResult = mockMvc.perform(get("/api/v1/inventories/{id}/stream", secondId.value()))
                    .andExpect(request().asyncStarted())
                    .andReturn();
            secondEmitter.complete();
            mockMvc.perform(asyncDispatch(secondResult)).andExpect(status().isOk());

            verify(sseInventoryStockNotifier).subscribe(firstId);
            verify(sseInventoryStockNotifier).subscribe(secondId);
        }
    }
}