package com.faridfarfan.pruebadesarrolladortisenior.dto;

import com.faridfarfan.pruebadesarrolladortisenior.domain.EstadoRiesgo;
import com.faridfarfan.pruebadesarrolladortisenior.domain.Riesgo;

public record RiesgoResponse(
        Long id,
        Long polizaId,
        String direccionInmueble,
        String arrendatario,
        String arrendador,
        EstadoRiesgo estado) {

    public static RiesgoResponse from(Riesgo riesgo) {
        return new RiesgoResponse(
                riesgo.getId(),
                riesgo.getPoliza().getId(),
                riesgo.getDireccionInmueble(),
                riesgo.getArrendatario(),
                riesgo.getArrendador(),
                riesgo.getEstado());
    }
}
