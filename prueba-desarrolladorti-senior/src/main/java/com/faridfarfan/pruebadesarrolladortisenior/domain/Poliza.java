package com.faridfarfan.pruebadesarrolladortisenior.domain;

import com.faridfarfan.pruebadesarrolladortisenior.exception.ReglaNegocioException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Póliza de arrendamiento. */
@Entity
@Table(name = "poliza")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // requerido por JPA
public class Poliza {

    private static final int ESCALA_MONETARIA = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String numero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoPoliza tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPoliza estado;

    /** Arrendatario o inmobiliaria. */
    @Column(nullable = false, length = 150)
    private String tomador;

    @Column(nullable = false)
    private LocalDate fechaInicioVigencia;

    @Column(nullable = false)
    private LocalDate fechaFinVigencia;

    @Column(nullable = false)
    private int mesesVigencia;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal canonMensual;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal prima;

    @OneToMany(mappedBy = "poliza", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Riesgo> riesgos = new ArrayList<>();

    /** Bloqueo optimista. */
    @Version
    private Long version;

    public static Poliza nueva(String numero, TipoPoliza tipo, String tomador, LocalDate fechaInicio,
                               int mesesVigencia, BigDecimal canonMensual) {
        if (mesesVigencia <= 0) {
            throw new IllegalArgumentException("La vigencia debe ser de al menos un mes");
        }
        if (canonMensual == null || canonMensual.signum() <= 0) {
            throw new IllegalArgumentException("El canon mensual debe ser mayor que cero");
        }
        Poliza poliza = new Poliza();
        poliza.numero = numero;
        poliza.tipo = tipo;
        poliza.tomador = tomador;
        poliza.estado = EstadoPoliza.ACTIVA;
        poliza.mesesVigencia = mesesVigencia;
        poliza.fechaInicioVigencia = fechaInicio;
        poliza.fechaFinVigencia = fechaInicio.plusMonths(mesesVigencia);
        poliza.canonMensual = canonMensual.setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP);
        poliza.prima = poliza.calcularPrima();
        return poliza;
    }

    public Riesgo agregarRiesgo(String direccionInmueble, String arrendatario, String arrendador) {
        validarNoCancelada("agregar riesgos a");
        if (tipo == TipoPoliza.INDIVIDUAL && !getRiesgosActivos().isEmpty()) {
            throw new ReglaNegocioException("Una póliza individual solo puede tener un riesgo");
        }
        Riesgo riesgo = new Riesgo(this, direccionInmueble, arrendatario, arrendador);
        riesgos.add(riesgo);
        return riesgo;
    }

    public void renovar(BigDecimal ipc) {
        validarNoCancelada("renovar");
        if (ipc == null || ipc.signum() < 0) {
            throw new IllegalArgumentException("El IPC debe ser un valor positivo");
        }
        this.canonMensual = canonMensual.multiply(BigDecimal.ONE.add(ipc))
                .setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP);
        this.prima = calcularPrima();
        this.fechaInicioVigencia = fechaFinVigencia;
        this.fechaFinVigencia = fechaFinVigencia.plusMonths(mesesVigencia);
        this.estado = EstadoPoliza.RENOVADA;
    }

    /** Cancela también sus riesgos. */
    public void cancelar() {
        validarNoCancelada("cancelar");
        this.estado = EstadoPoliza.CANCELADA;
        riesgos.forEach(Riesgo::cancelar);
    }

    public boolean estaCancelada() {
        return estado == EstadoPoliza.CANCELADA;
    }

    public List<Riesgo> getRiesgos() {
        return Collections.unmodifiableList(riesgos);
    }

    public List<Riesgo> getRiesgosActivos() {
        return riesgos.stream().filter(Riesgo::estaActivo).toList();
    }

    private BigDecimal calcularPrima() {
        return canonMensual.multiply(BigDecimal.valueOf(mesesVigencia))
                .setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP);
    }

    void validarNoCancelada(String accion) {
        if (estaCancelada()) {
            throw new ReglaNegocioException("No se puede " + accion + " una póliza cancelada");
        }
    }
}
