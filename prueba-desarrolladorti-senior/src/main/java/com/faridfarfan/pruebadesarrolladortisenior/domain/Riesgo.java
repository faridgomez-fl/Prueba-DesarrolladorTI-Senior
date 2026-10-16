package com.faridfarfan.pruebadesarrolladortisenior.domain;

import com.faridfarfan.pruebadesarrolladortisenior.exception.ReglaNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Inmueble arrendado. */
@Entity
@Table(name = "riesgo")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Riesgo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "poliza_id", nullable = false)
    private Poliza poliza;

    @Column(nullable = false, length = 200)
    private String direccionInmueble;

    /** Asegurado. */
    @Column(nullable = false, length = 150)
    private String arrendatario;

    /** Beneficiario. */
    @Column(nullable = false, length = 150)
    private String arrendador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoRiesgo estado;

    @Version
    private Long version;

    Riesgo(Poliza poliza, String direccionInmueble, String arrendatario, String arrendador) {
        this.poliza = poliza;
        this.direccionInmueble = direccionInmueble;
        this.arrendatario = arrendatario;
        this.arrendador = arrendador;
        this.estado = EstadoRiesgo.ACTIVO;
    }

    /** Cancelación en cascada. */
    void cancelar() {
        this.estado = EstadoRiesgo.CANCELADO;
    }

    public void cancelarIndividualmente() {
        poliza.validarNoCancelada("cancelar riesgos de");
        if (poliza.getTipo() != TipoPoliza.COLECTIVA) {
            throw new ReglaNegocioException(
                    "Solo se pueden cancelar riesgos de pólizas colectivas; para una individual cancele la póliza");
        }
        if (!estaActivo()) {
            throw new ReglaNegocioException("El riesgo ya está cancelado");
        }
        cancelar();
    }

    public boolean estaActivo() {
        return estado == EstadoRiesgo.ACTIVO;
    }
}
