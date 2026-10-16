package com.faridfarfan.pruebadesarrolladortisenior.integration.core;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Notifica al CORE después del commit. */
@Component
@RequiredArgsConstructor
public class CoreEventoListener {

    private final CoreEdicionClient coreEdicionClient;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alModificarPoliza(PolizaModificadaEvent evento) {
        coreEdicionClient.notificarActualizacion(evento.polizaId(), evento.operacion());
    }
}
