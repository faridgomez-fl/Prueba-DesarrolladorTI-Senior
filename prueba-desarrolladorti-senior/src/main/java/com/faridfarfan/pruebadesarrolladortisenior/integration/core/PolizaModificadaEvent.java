package com.faridfarfan.pruebadesarrolladortisenior.integration.core;

/** Cambio en una póliza o riesgo. */
public record PolizaModificadaEvent(Long polizaId, OperacionPoliza operacion) {
}
