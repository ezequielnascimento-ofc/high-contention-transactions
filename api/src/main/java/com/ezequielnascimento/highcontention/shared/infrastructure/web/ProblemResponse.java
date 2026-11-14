package com.ezequielnascimento.highcontention.shared.infrastructure.web;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Documentation-only shape of the RFC 9457 Problem Details that
 * {@link GlobalExceptionHandler} returns. The runtime body is Spring's
 * {@link org.springframework.http.ProblemDetail}.
 */
@Schema(name = "Problem", description = "RFC 9457 Problem Details (application/problem+json). See docs/errors.md.")
public record ProblemResponse(
        @Schema(example = GlobalExceptionHandler.TYPE_BASE + "insufficient-stock",
                description = "Identifies the problem; links to its section in docs/errors.md")
        String type,
        @Schema(example = "Insufficient stock")
        String title,
        @Schema(example = "409")
        int status,
        @Schema(example = "Insufficient stock for inventory 0b6f…: available=0, requested=1",
                description = "Human-readable explanation; do not parse it")
        String detail,
        @Schema(example = "/api/v1/inventories/0b6f…/decrease")
        String instance,
        @Schema(description = "Present only for validation-failed")
        List<GlobalExceptionHandler.FieldViolation> errors
) {
}
