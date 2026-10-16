package com.faridfarfan.pruebadesarrolladortisenior.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AgregarRiesgoRequest(
        @NotBlank @Size(max = 200) String direccionInmueble,
        @NotBlank @Size(max = 150) String arrendatario,
        @NotBlank @Size(max = 150) String arrendador) {
}
