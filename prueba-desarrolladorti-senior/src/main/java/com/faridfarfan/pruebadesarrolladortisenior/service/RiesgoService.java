package com.faridfarfan.pruebadesarrolladortisenior.service;

import com.faridfarfan.pruebadesarrolladortisenior.domain.Riesgo;
import com.faridfarfan.pruebadesarrolladortisenior.dto.RiesgoResponse;
import com.faridfarfan.pruebadesarrolladortisenior.exception.RecursoNoEncontradoException;
import com.faridfarfan.pruebadesarrolladortisenior.integration.core.OperacionPoliza;
import com.faridfarfan.pruebadesarrolladortisenior.integration.core.PolizaModificadaEvent;
import com.faridfarfan.pruebadesarrolladortisenior.repository.RiesgoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiesgoService {

    private final RiesgoRepository riesgoRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public RiesgoResponse cancelar(Long riesgoId) {
        Riesgo riesgo = riesgoRepository.findById(riesgoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Riesgo", riesgoId));
        riesgo.cancelarIndividualmente();
        Long polizaId = riesgo.getPoliza().getId();
        log.info("Riesgo cancelado: riesgoId={}, polizaId={}", riesgoId, polizaId);
        eventPublisher.publishEvent(new PolizaModificadaEvent(polizaId, OperacionPoliza.RIESGO_CANCELADO));
        return RiesgoResponse.from(riesgo);
    }
}
