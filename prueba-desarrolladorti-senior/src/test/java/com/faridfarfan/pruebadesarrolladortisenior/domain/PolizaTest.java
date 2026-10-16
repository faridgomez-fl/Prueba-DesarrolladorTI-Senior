package com.faridfarfan.pruebadesarrolladortisenior.domain;

import com.faridfarfan.pruebadesarrolladortisenior.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PolizaTest {

    private static final LocalDate INICIO = LocalDate.of(2026, 1, 1);

    @Test
    void laPrimaEsElCanonPorLosMesesDeVigencia() {
        Poliza poliza = individual();

        assertThat(poliza.getPrima()).isEqualByComparingTo("18000000.00");
        assertThat(poliza.getFechaFinVigencia()).isEqualTo(LocalDate.of(2027, 1, 1));
        assertThat(poliza.getEstado()).isEqualTo(EstadoPoliza.ACTIVA);
    }

    @Test
    void renovarIncrementaCanonYPrimaPorIpcYExtiendeLaVigencia() {
        Poliza poliza = individual();

        poliza.renovar(new BigDecimal("0.0520"));

        assertThat(poliza.getCanonMensual()).isEqualByComparingTo("1578000.00");
        assertThat(poliza.getPrima()).isEqualByComparingTo("18936000.00");
        assertThat(poliza.getFechaInicioVigencia()).isEqualTo(LocalDate.of(2027, 1, 1));
        assertThat(poliza.getFechaFinVigencia()).isEqualTo(LocalDate.of(2028, 1, 1));
        assertThat(poliza.getEstado()).isEqualTo(EstadoPoliza.RENOVADA);
    }

    @Test
    void noSePuedeRenovarUnaPolizaCancelada() {
        Poliza poliza = individual();
        poliza.cancelar();

        assertThatThrownBy(() -> poliza.renovar(new BigDecimal("0.05")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("renovar");
    }

    @Test
    void cancelarLaPolizaCancelaTodosSusRiesgos() {
        Poliza poliza = colectiva();
        poliza.agregarRiesgo("Apto 101", "Arrendatario 1", "Arrendador");
        poliza.agregarRiesgo("Apto 102", "Arrendatario 2", "Arrendador");

        poliza.cancelar();

        assertThat(poliza.getEstado()).isEqualTo(EstadoPoliza.CANCELADA);
        assertThat(poliza.getRiesgos()).allMatch(r -> r.getEstado() == EstadoRiesgo.CANCELADO);
    }

    @Test
    void unaPolizaIndividualSoloAdmiteUnRiesgo() {
        Poliza poliza = individual();
        poliza.agregarRiesgo("Apto 301", "Juan", "María");

        assertThatThrownBy(() -> poliza.agregarRiesgo("Apto 302", "Juan", "María"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("un riesgo");
    }

    @Test
    void unaPolizaColectivaAdmiteVariosRiesgos() {
        Poliza poliza = colectiva();
        poliza.agregarRiesgo("Apto 101", "A", "X");
        poliza.agregarRiesgo("Apto 102", "B", "X");

        assertThat(poliza.getRiesgosActivos()).hasSize(2);
    }

    @Test
    void cancelarUnRiesgoDeUnaColectivaNoAfectaLosDemas() {
        Poliza poliza = colectiva();
        Riesgo primero = poliza.agregarRiesgo("Apto 101", "A", "X");
        poliza.agregarRiesgo("Apto 102", "B", "X");

        primero.cancelarIndividualmente();

        assertThat(primero.getEstado()).isEqualTo(EstadoRiesgo.CANCELADO);
        assertThat(poliza.getRiesgosActivos()).hasSize(1);
        assertThat(poliza.getEstado()).isEqualTo(EstadoPoliza.ACTIVA);
    }

    @Test
    void noSePuedeCancelarIndividualmenteElRiesgoDeUnaPolizaIndividual() {
        Riesgo riesgo = individual().agregarRiesgo("Apto 301", "Juan", "María");

        assertThatThrownBy(riesgo::cancelarIndividualmente)
                .isInstanceOf(ReglaNegocioException.class);
    }

    private static Poliza individual() {
        return Poliza.nueva("IND-1", TipoPoliza.INDIVIDUAL, "Juan", INICIO, 12, new BigDecimal("1500000"));
    }

    private static Poliza colectiva() {
        return Poliza.nueva("COL-1", TipoPoliza.COLECTIVA, "Inmobiliaria", INICIO, 12, new BigDecimal("4500000"));
    }
}
