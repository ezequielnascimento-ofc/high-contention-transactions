package com.ezequielnascimento.highcontention.integration.product.infrastructure.adapter.web;

import com.ezequielnascimento.highcontention.product.domain.exceptions.InvalidProductException;
import com.ezequielnascimento.highcontention.product.domain.exceptions.ProductNotFoundException;
import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import com.ezequielnascimento.highcontention.product.domain.port.in.ActivateProductUseCase;
import com.ezequielnascimento.highcontention.product.domain.port.in.ChangeProductDescriptionUseCase;
import com.ezequielnascimento.highcontention.product.domain.port.in.ChangeProductPriceUseCase;
import com.ezequielnascimento.highcontention.product.domain.port.in.CreateProductUseCase;
import com.ezequielnascimento.highcontention.product.domain.port.in.DeactivateProductUseCase;
import com.ezequielnascimento.highcontention.product.domain.port.in.GetProductUseCase;
import com.ezequielnascimento.highcontention.product.domain.port.in.RenameProductUseCase;
import com.ezequielnascimento.highcontention.product.infrastructure.adapter.web.ProductController;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateProductUseCase createProductUseCase;
    @MockitoBean
    private GetProductUseCase getProductUseCase;
    @MockitoBean
    private RenameProductUseCase renameProductUseCase;
    @MockitoBean
    private ChangeProductPriceUseCase changeProductPriceUseCase;
    @MockitoBean
    private ChangeProductDescriptionUseCase changeProductDescriptionUseCase;
    @MockitoBean
    private ActivateProductUseCase activateProductUseCase;
    @MockitoBean
    private DeactivateProductUseCase deactivateProductUseCase;

    private Product sampleProduct() {
        return Product.create("Mechanical Keyboard", "High-performance mechanical keyboard", new BigDecimal("499.90"));
    }

    @Nested
    class Create {

        @Test
        void shouldReturn201WhenProductIsCreated() throws Exception {
            Product product = sampleProduct();
            when(createProductUseCase.execute(eq("Mechanical Keyboard"), eq("High-performance mechanical keyboard"), eq(new BigDecimal("499.90"))))
                    .thenReturn(product);

            String requestBody = """
                    {"name":"Mechanical Keyboard","description":"High-performance mechanical keyboard","price":499.90}
                    """;

            mockMvc.perform(post("/api/v1/products")
                            .contentType("application/json")
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("Mechanical Keyboard"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        void shouldReturn400WhenNameIsBlank() throws Exception {
            String requestBody = """
                    {"name":"","description":"desc","price":100.00}
                    """;

            mockMvc.perform(post("/api/v1/products")
                            .contentType("application/json")
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details").isNotEmpty());
        }

        @Test
        void shouldReturn400WhenPriceIsNegative() throws Exception {
            String requestBody = """
                    {"name":"Keyboard","description":"desc","price":-10.00}
                    """;

            mockMvc.perform(post("/api/v1/products")
                            .contentType("application/json")
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class Get {

        @Test
        void shouldReturn200WithProductWhenItExists() throws Exception {
            Product product = sampleProduct();
            when(getProductUseCase.execute(product.id())).thenReturn(product);

            mockMvc.perform(get("/api/v1/products/{id}", product.id().value()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(product.id().value().toString()));
        }

        @Test
        void shouldReturn404WhenProductDoesNotExist() throws Exception {
            ProductId id = ProductId.generate();
            when(getProductUseCase.execute(id)).thenThrow(new ProductNotFoundException(id));

            mockMvc.perform(get("/api/v1/products/{id}", id.value()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    @Nested
    class Rename {

        @Test
        void shouldReturn200WithUpdatedName() throws Exception {
            Product product = sampleProduct();
            product.rename("Gaming Keyboard");
            when(renameProductUseCase.execute(eq(product.id()), eq("Gaming Keyboard"))).thenReturn(product);

            mockMvc.perform(patch("/api/v1/products/{id}/name", product.id().value())
                            .contentType("application/json")
                            .content("{\"name\":\"Gaming Keyboard\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Gaming Keyboard"));
        }

        @Test
        void shouldReturn400WhenNameIsInvalid() throws Exception {
            ProductId id = ProductId.generate();
            when(renameProductUseCase.execute(eq(id), eq(" ")))
                    .thenThrow(new InvalidProductException("Product name must not be blank"));

            mockMvc.perform(patch("/api/v1/products/{id}/name", id.value())
                            .contentType("application/json")
                            .content("{\"name\":\" \"}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class ChangePrice {

        @Test
        void shouldReturn200WithUpdatedPrice() throws Exception {
            Product product = sampleProduct();
            product.changePrice(new BigDecimal("599.90"));
            when(changeProductPriceUseCase.execute(eq(product.id()), eq(new BigDecimal("599.90")))).thenReturn(product);

            mockMvc.perform(patch("/api/v1/products/{id}/price", product.id().value())
                            .contentType("application/json")
                            .content("{\"price\":599.90}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.price").value(599.90));
        }

        @Test
        void shouldReturn400WhenPriceIsMissing() throws Exception {
            ProductId id = ProductId.generate();

            mockMvc.perform(patch("/api/v1/products/{id}/price", id.value())
                            .contentType("application/json")
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class ChangeDescription {

        @Test
        void shouldReturn200WithUpdatedDescription() throws Exception {
            Product product = sampleProduct();
            product.changeDescription("Updated description");
            when(changeProductDescriptionUseCase.execute(eq(product.id()), eq("Updated description"))).thenReturn(product);

            mockMvc.perform(patch("/api/v1/products/{id}/description", product.id().value())
                            .contentType("application/json")
                            .content("{\"description\":\"Updated description\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.description").value("Updated description"));
        }

        @Test
        void shouldAllowNullDescription() throws Exception {
            Product product = sampleProduct();
            product.changeDescription(null);
            when(changeProductDescriptionUseCase.execute(eq(product.id()), eq(null))).thenReturn(product);

            mockMvc.perform(patch("/api/v1/products/{id}/description", product.id().value())
                            .contentType("application/json")
                            .content("{\"description\":null}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.description").doesNotExist());
        }
    }

    @Nested
    class Activate {

        @Test
        void shouldReturn200WithActiveStatus() throws Exception {
            Product product = sampleProduct();
            when(activateProductUseCase.execute(product.id())).thenReturn(product);

            mockMvc.perform(post("/api/v1/products/{id}/activate", product.id().value()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        void shouldReturn404WhenProductDoesNotExist() throws Exception {
            ProductId id = ProductId.generate();
            when(activateProductUseCase.execute(id)).thenThrow(new ProductNotFoundException(id));

            mockMvc.perform(post("/api/v1/products/{id}/activate", id.value()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class Deactivate {

        @Test
        void shouldReturn200WithInactiveStatus() throws Exception {
            Product product = sampleProduct();
            product.deactivate();
            when(deactivateProductUseCase.execute(product.id())).thenReturn(product);

            mockMvc.perform(post("/api/v1/products/{id}/deactivate", product.id().value()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("INACTIVE"));
        }
    }
}