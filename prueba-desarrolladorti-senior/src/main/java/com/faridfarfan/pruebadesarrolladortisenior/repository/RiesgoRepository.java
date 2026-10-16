package com.faridfarfan.pruebadesarrolladortisenior.repository;

import com.faridfarfan.pruebadesarrolladortisenior.domain.Riesgo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RiesgoRepository extends JpaRepository<Riesgo, Long> {

    List<Riesgo> findByPolizaIdOrderById(Long polizaId);
}
