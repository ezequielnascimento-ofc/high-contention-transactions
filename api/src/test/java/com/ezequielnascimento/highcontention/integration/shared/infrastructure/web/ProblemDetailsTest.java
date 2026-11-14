package com.ezequielnascimento.highcontention.integration.shared.infrastructure.web;

import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InsufficientStockException;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InventoryAlreadyExistsException;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.UnknownProductException;
import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;
import com.ezequielnascimento.highcontention.inventory.domain.port.in.CreateInventoryUseCase;
import com.ezequielnascimento.highcontention.inventory.domain.port.in.DecreaseStockUseCase;
import com.ezequielnascimento.highcontention.inventory.domain.port.in.GetInventoryByProductIdUseCase;
import com.ezequielnascimento.highcontention.inventory.domain.port.in.GetInventoryUseCase;
import com.ezequielnascimento.highcontention.inventory.domain.port.in.IncreaseStockUseCase;
import com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.in.web.InventoryController;
import com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.out.sse.SseInventoryStockNotifier;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Every error path returns RFC 9457 Problem Details with a documented type.
 */
@WebMvcTest(InventoryController.class)
class ProblemDetailsTest {

    private static final String TYPES =
            "https://github.com/ezequielnascimento-ofc/high-contention-transactions/blob/main/docs/errors.md#";

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
    @MockitoBean
    private GetInventoryByProductIdUseCase getInventoryByProductIdUseCase;

    @Test
    void malformedBodyIs400MalformedRequest() throws Exception {
        expectProblem(mockMvc.perform(post("/api/v1/inventories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":")),
                400, "malformed-request");
    }

    @Test
    void invalidFieldsAre400ValidationFailedWithFieldErrors() throws Exception {
        expectProblem(mockMvc.perform(post("/api/v1/inventories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":null,\"quantity\":0}")),
                400, "validation-failed")
                .andExpect(jsonPath("$.errors.length()").value(2))
                .andExpect(jsonPath("$.errors[*].field", org.hamcrest.Matchers.containsInAnyOrder("productId", "quantity")));
    }

    @Test
    void nonUuidPathVariableIs400InvalidParameter() throws Exception {
        expectProblem(mockMvc.perform(get("/api/v1/inventories/{id}", "not-a-uuid")), 400, "invalid-parameter")
                .andExpect(jsonPath("$.detail").value("Parameter 'id' must be a valid UUID"));
    }

    @Test
    void unknownRouteIs404ResourceNotFound() throws Exception {
        expectProblem(mockMvc.perform(get("/api/v1/does-not-exist")), 404, "resource-not-found");
    }

    @Test
    void insufficientStockIs409WithTheRequestPathAsInstance() throws Exception {
        InventoryId id = InventoryId.generate();
        when(decreaseStockUseCase.execute(id, 5)).thenThrow(new InsufficientStockException(id, 1, 5));

        expectProblem(mockMvc.perform(post("/api/v1/inventories/{id}/decrease", id.value())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":5}")),
                409, "insufficient-stock")
                .andExpect(jsonPath("$.instance").value("/api/v1/inventories/" + id.value() + "/decrease"));
    }

    @Test
    void duplicateInventoryIs409InventoryAlreadyExists() throws Exception {
        ProductId productId = ProductId.generate();
        when(createInventoryUseCase.execute(any(), anyInt())).thenThrow(new InventoryAlreadyExistsException(productId));

        expectProblem(createInventory(productId), 409, "inventory-already-exists");
    }

    @Test
    void inventoryForAMissingProductIs422UnknownProduct() throws Exception {
        ProductId productId = ProductId.generate();
        when(createInventoryUseCase.execute(any(), anyInt())).thenThrow(new UnknownProductException(productId));

        expectProblem(createInventory(productId), 422, "unknown-product");
    }

    @Test
    void unexpectedFailureIs500WithoutInternalDetails() throws Exception {
        when(createInventoryUseCase.execute(any(), anyInt()))
                .thenThrow(new IllegalStateException("connection to db-internal:5432 refused"));

        expectProblem(createInventory(ProductId.generate()), 500, "internal-error")
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred"))
                .andExpect(content().string(not(containsString("db-internal"))));
    }

    private ResultActions createInventory(ProductId productId) throws Exception {
        return mockMvc.perform(post("/api/v1/inventories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\":\"" + productId.value() + "\",\"quantity\":10}"));
    }

    private static ResultActions expectProblem(ResultActions result, int status, String type) throws Exception {
        return result
                .andExpect(status().is(status))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value(TYPES + type))
                .andExpect(jsonPath("$.status").value(status))
                .andExpect(jsonPath("$.title").isNotEmpty())
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }
}
