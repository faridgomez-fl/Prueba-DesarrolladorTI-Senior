package com.faridfarfan.pruebadesarrolladortisenior.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        @Valid @NotNull Renovacion renovacion,
        @Valid @NotNull Seguridad seguridad,
        @Valid @NotNull Core core,
        boolean datosDemo) {

    /** IPC: 0.0520 = 5,20 %. */
    public record Renovacion(@NotNull @DecimalMin("0.0") BigDecimal ipc) {
    }

    public record Seguridad(@NotBlank String apiKey) {
    }

    public record Core(@NotBlank String url) {
    }
}
