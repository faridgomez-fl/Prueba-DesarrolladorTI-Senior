package com.faridfarfan.pruebadesarrolladortisenior.dto;

import com.faridfarfan.pruebadesarrolladortisenior.domain.EstadoPoliza;
import com.faridfarfan.pruebadesarrolladortisenior.domain.Poliza;
import com.faridfarfan.pruebadesarrolladortisenior.domain.TipoPoliza;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PolizaResponse(
        Long id,
        String numero,
        TipoPoliza tipo,
        EstadoPoliza estado,
        String tomador,
        LocalDate fechaInicioVigencia,
        LocalDate fechaFinVigencia,
        int mesesVigencia,
        BigDecimal canonMensual,
        BigDecimal prima) {

    public static PolizaResponse from(Poliza poliza) {
        return new PolizaResponse(
                poliza.getId(),
                poliza.getNumero(),
                poliza.getTipo(),
                poliza.getEstado(),
                poliza.getTomador(),
                poliza.getFechaInicioVigencia(),
                poliza.getFechaFinVigencia(),
                poliza.getMesesVigencia(),
                poliza.getCanonMensual(),
                poliza.getPrima());
    }
}
