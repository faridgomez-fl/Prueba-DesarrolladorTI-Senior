package com.faridfarfan.pruebadesarrolladortisenior.integration.core;

/** Puerto hacia el CORE. */
public interface CoreEdicionClient {

    void notificarActualizacion(Long polizaId, OperacionPoliza operacion);
}
