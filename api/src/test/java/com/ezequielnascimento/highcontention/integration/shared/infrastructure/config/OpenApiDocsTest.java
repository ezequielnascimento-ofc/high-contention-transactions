package com.ezequielnascimento.highcontention.integration.shared.infrastructure.config;

import com.ezequielnascimento.highcontention.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
@AutoConfigureMockMvc
class OpenApiDocsTest {

    private static final String DECREASE = "$.paths['/api/v1/inventories/{id}/decrease'].post";
    private static final String CREATE_INVENTORY = "$.paths['/api/v1/inventories'].post";
    private static final String LIST_PRODUCTS = "$.paths['/api/v1/products'].get";
    private static final String PROBLEM = "content['application/problem+json'].schema['$ref']";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void servesTheOpenApiDescription() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("High-Contention Transactions API"))
                .andExpect(jsonPath("$.components.schemas.Problem.properties.type").exists());
    }

    @Test
    void documentsTheBusinessConflictsAsProblemDetails() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath(DECREASE + ".responses['409']." + PROBLEM).value("#/components/schemas/Problem"))
                .andExpect(jsonPath(CREATE_INVENTORY + ".responses['409']." + PROBLEM).value("#/components/schemas/Problem"))
                .andExpect(jsonPath(CREATE_INVENTORY + ".responses['422']." + PROBLEM).value("#/components/schemas/Problem"));
    }

    @Test
    void addsGenericErrorsOnlyWhereTheyCanHappen() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath(DECREASE + ".responses", hasKey("404")))
                .andExpect(jsonPath(DECREASE + ".responses", hasKey("400")))
                .andExpect(jsonPath(DECREASE + ".responses", hasKey("500")))
                .andExpect(jsonPath(LIST_PRODUCTS + ".responses", not(hasKey("404"))))
                .andExpect(jsonPath(LIST_PRODUCTS + ".responses", not(hasKey("409"))));
    }

    @Test
    void servesSwaggerUi() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}
