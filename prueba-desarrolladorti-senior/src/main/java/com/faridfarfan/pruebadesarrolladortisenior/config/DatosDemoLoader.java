package com.faridfarfan.pruebadesarrolladortisenior.config;

import com.faridfarfan.pruebadesarrolladortisenior.domain.Poliza;
import com.faridfarfan.pruebadesarrolladortisenior.domain.TipoPoliza;
import com.faridfarfan.pruebadesarrolladortisenior.repository.PolizaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Datos de ejemplo al arrancar. */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.datos-demo", havingValue = "true")
public class DatosDemoLoader implements ApplicationRunner {

    private final PolizaRepository polizaRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (polizaRepository.count() > 0) {
            return;
        }
        LocalDate inicio = LocalDate.of(2026, 1, 1);

        Poliza individual1 = Poliza.nueva("POL-IND-001", TipoPoliza.INDIVIDUAL, "Juan Pérez",
                inicio, 12, new BigDecimal("1500000"));
        individual1.agregarRiesgo("Calle 100 # 15-20 Apto 301, Bogotá", "Juan Pérez", "María Rodríguez");

        Poliza individual2 = Poliza.nueva("POL-IND-002", TipoPoliza.INDIVIDUAL, "Ana Gómez",
                inicio, 6, new BigDecimal("2000000"));
        individual2.agregarRiesgo("Carrera 43A # 1-50 Apto 802, Medellín", "Ana Gómez", "Pedro Castro");

        Poliza colectiva1 = Poliza.nueva("POL-COL-001", TipoPoliza.COLECTIVA, "Inmobiliaria Los Andes S.A.S.",
                inicio, 12, new BigDecimal("4500000"));
        colectiva1.agregarRiesgo("Calle 72 # 10-07 Apto 101, Bogotá", "Laura Martínez", "Carlos Ruiz");
        colectiva1.agregarRiesgo("Calle 72 # 10-07 Apto 202, Bogotá", "Andrés López", "Carlos Ruiz");

        Poliza colectiva2 = Poliza.nueva("POL-COL-002", TipoPoliza.COLECTIVA, "Administración Torres del Parque",
                inicio, 12, new BigDecimal("3200000"));
        colectiva2.agregarRiesgo("Carrera 5 # 26-40 Apto 1503, Bogotá", "Sofía Herrera", "Jorge Díaz");
        colectiva2.cancelar();

        polizaRepository.saveAll(List.of(individual1, individual2, colectiva1, colectiva2));
        log.info("Datos de demostración cargados: {} pólizas", polizaRepository.count());
    }
}
