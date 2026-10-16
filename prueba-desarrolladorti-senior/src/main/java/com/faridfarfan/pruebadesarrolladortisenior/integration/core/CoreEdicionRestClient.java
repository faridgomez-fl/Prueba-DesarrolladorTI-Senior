package com.faridfarfan.pruebadesarrolladortisenior.integration.core;

import com.faridfarfan.pruebadesarrolladortisenior.config.AppProperties;
import com.faridfarfan.pruebadesarrolladortisenior.config.ApiKeyAuthenticationFilter;
import com.faridfarfan.pruebadesarrolladortisenior.dto.CoreEventoRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

/** Cliente HTTP del CORE. */
@Slf4j
@Component
public class CoreEdicionRestClient implements CoreEdicionClient {

    static final String EVENTO_ACTUALIZACION = "ACTUALIZACION";

    private final RestClient restClient;

    public CoreEdicionRestClient(AppProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(2));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = RestClient.builder()
                .baseUrl(properties.core().url())
                .requestFactory(requestFactory)
                .defaultHeader(ApiKeyAuthenticationFilter.HEADER, properties.seguridad().apiKey())
                .build();
    }

    @Override
    public void notificarActualizacion(Long polizaId, OperacionPoliza operacion) {
        log.info("Enviando al CORE: evento={}, polizaId={}, operacion={}", EVENTO_ACTUALIZACION, polizaId, operacion);
        try {
            restClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new CoreEventoRequest(EVENTO_ACTUALIZACION, polizaId))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            // La operación ya quedó guardada: no se revierte.
            log.error("No se pudo notificar al CORE: polizaId={}, operacion={}", polizaId, operacion, e);
        }
    }
}
