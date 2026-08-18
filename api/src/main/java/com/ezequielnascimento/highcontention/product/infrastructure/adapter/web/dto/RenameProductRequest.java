package com.ezequielnascimento.highcontention.product.infrastructure.adapter.web.dto;

import jakarta.validation.constraints.NotBlank;

public record RenameProductRequest(@NotBlank String name) {
}
