package com.ezequielnascimento.highcontention.shared.infrastructure.config;

import com.ezequielnascimento.highcontention.shared.infrastructure.web.ProblemResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * OpenAPI description served at {@code /v3/api-docs} and rendered by Swagger UI
 * at {@code /swagger-ui.html}.
 * <p>
 * Every operation documents its Problem Details errors: 400 and 500 always,
 * 404 when it addresses a resource by id, and the business conflicts
 * (409, 422) declared on the endpoints that can raise them.
 */
@Configuration
public class OpenApiConfig {

    static final String PROBLEM_JSON = "application/problem+json";
    static final String PROBLEM_REF = "#/components/schemas/Problem";

    @Bean
    OpenAPI highContentionOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("High-Contention Transactions API")
                        .version("v1")
                        .description("""
                                Products and their inventory. The decrease endpoint is the contended \
                                operation: many requests competing for the same stock row must never \
                                oversell. Errors are RFC 9457 Problem Details.""")
                        .license(new License().name("MIT")
                                .url("https://github.com/ezequielnascimento-ofc/high-contention-transactions/blob/main/LICENSE")))
                .externalDocs(new ExternalDocumentation()
                        .description("Source, architecture decisions and error catalogue")
                        .url("https://github.com/ezequielnascimento-ofc/high-contention-transactions"));
    }

    @Bean
    OpenApiCustomizer problemDetailsCustomizer() {
        return openApi -> {
            Map<String, Schema> schemas = ModelConverters.getInstance().readAll(ProblemResponse.class);
            schemas.forEach(openApi.getComponents()::addSchemas);

            openApi.getPaths().forEach((path, item) -> item.readOperations().forEach(operation -> {
                ApiResponses responses = operation.getResponses();
                responses.putIfAbsent("400", problem("Invalid request: validation, malformed body or bad parameter"));
                if (addressesAResource(path, operation)) {
                    responses.putIfAbsent("404", problem("Resource not found"));
                }
                responses.putIfAbsent("500", problem("Unexpected error; no internal detail is exposed"));
            }));
        };
    }

    private static boolean addressesAResource(String path, Operation operation) {
        if (path.contains("{")) {
            return true;
        }
        return operation.getParameters() != null
                && operation.getParameters().stream().map(Parameter::getIn).anyMatch("query"::equals);
    }

    static ApiResponse problem(String description) {
        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType(PROBLEM_JSON,
                        new MediaType().schema(new Schema<>().$ref(PROBLEM_REF))));
    }
}
