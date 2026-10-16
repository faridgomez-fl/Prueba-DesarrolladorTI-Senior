package com.faridfarfan.pruebadesarrolladortisenior.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CoreEventoRequest(
        @NotBlank String evento,
        @NotNull @Positive Long polizaId) {
}
