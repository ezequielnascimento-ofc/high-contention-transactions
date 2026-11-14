package com.ezequielnascimento.highcontention.shared.infrastructure.web;

import com.ezequielnascimento.highcontention.inventory.domain.exceptions.ConcurrencyConflictException;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InsufficientStockException;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InvalidInventoryException;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InventoryAlreadyExistsException;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InventoryNotFoundException;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.UnknownProductException;
import com.ezequielnascimento.highcontention.product.domain.exceptions.InvalidProductException;
import com.ezequielnascimento.highcontention.product.domain.exceptions.ProductNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.util.List;

/**
 * Every error leaves the API as RFC 9457 Problem Details
 * ({@code application/problem+json}). The {@code type} URIs point to
 * {@code docs/errors.md}, which documents each problem.
 * <p>
 * Spring MVC's own exceptions (malformed body, type mismatch, unknown route,
 * unsupported method…) are handled by {@link ResponseEntityExceptionHandler},
 * so the catch-all below only sees genuinely unexpected failures.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    static final String TYPE_BASE =
            "https://github.com/ezequielnascimento-ofc/high-contention-transactions/blob/main/docs/errors.md#";

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public record FieldViolation(String field, String message) {
    }

    @ExceptionHandler({ProductNotFoundException.class, InventoryNotFoundException.class})
    ProblemDetail handleNotFound(RuntimeException ex) {
        return problem(HttpStatus.NOT_FOUND, "resource-not-found", "Resource not found", ex.getMessage());
    }

    @ExceptionHandler({InvalidProductException.class, InvalidInventoryException.class})
    ProblemDetail handleInvalid(RuntimeException ex) {
        return problem(HttpStatus.BAD_REQUEST, "invalid-request", "Invalid request", ex.getMessage());
    }

    @ExceptionHandler(InsufficientStockException.class)
    ProblemDetail handleInsufficientStock(InsufficientStockException ex) {
        return problem(HttpStatus.CONFLICT, "insufficient-stock", "Insufficient stock", ex.getMessage());
    }

    @ExceptionHandler(ConcurrencyConflictException.class)
    ProblemDetail handleConcurrencyConflict(ConcurrencyConflictException ex) {
        return problem(HttpStatus.CONFLICT, "concurrency-conflict", "Concurrency conflict", ex.getMessage());
    }

    @ExceptionHandler(InventoryAlreadyExistsException.class)
    ProblemDetail handleInventoryAlreadyExists(InventoryAlreadyExistsException ex) {
        return problem(HttpStatus.CONFLICT, "inventory-already-exists", "Inventory already exists", ex.getMessage());
    }

    @ExceptionHandler(UnknownProductException.class)
    ProblemDetail handleUnknownProduct(UnknownProductException ex) {
        return problem(HttpStatus.UNPROCESSABLE_CONTENT, "unknown-product", "Unknown product", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "internal-error", "Internal error",
                "An unexpected error occurred");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<FieldViolation> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldViolation(error.getField(), error.getDefaultMessage()))
                .toList();
        ProblemDetail body = problem(HttpStatus.BAD_REQUEST, "validation-failed", "Validation failed",
                "The request body has invalid fields");
        body.setProperty("errors", errors);
        return handleExceptionInternal(ex, body, headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail body = problem(HttpStatus.BAD_REQUEST, "malformed-request", "Malformed request",
                "The request body is missing or is not valid JSON");
        return handleExceptionInternal(ex, body, headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String expected = ex.getRequiredType() == null ? "a valid value" : "a valid " + ex.getRequiredType().getSimpleName();
        ProblemDetail body = problem(HttpStatus.BAD_REQUEST, "invalid-parameter", "Invalid parameter",
                "Parameter '" + ex.getPropertyName() + "' must be " + expected);
        return handleExceptionInternal(ex, body, headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(
            NoResourceFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail body = problem(HttpStatus.NOT_FOUND, "resource-not-found", "Resource not found",
                "No endpoint " + ex.getHttpMethod() + " /" + ex.getResourcePath());
        return handleExceptionInternal(ex, body, headers, HttpStatus.NOT_FOUND, request);
    }

    private static ProblemDetail problem(HttpStatus status, String type, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create(TYPE_BASE + type));
        problem.setTitle(title);
        return problem;
    }
}
