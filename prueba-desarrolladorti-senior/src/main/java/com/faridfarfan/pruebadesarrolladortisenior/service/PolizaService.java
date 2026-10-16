package com.faridfarfan.pruebadesarrolladortisenior.service;

import com.faridfarfan.pruebadesarrolladortisenior.config.AppProperties;
import com.faridfarfan.pruebadesarrolladortisenior.domain.EstadoPoliza;
import com.faridfarfan.pruebadesarrolladortisenior.domain.Poliza;
import com.faridfarfan.pruebadesarrolladortisenior.domain.Riesgo;
import com.faridfarfan.pruebadesarrolladortisenior.domain.TipoPoliza;
import com.faridfarfan.pruebadesarrolladortisenior.dto.AgregarRiesgoRequest;
import com.faridfarfan.pruebadesarrolladortisenior.dto.PolizaResponse;
import com.faridfarfan.pruebadesarrolladortisenior.dto.RiesgoResponse;
import com.faridfarfan.pruebadesarrolladortisenior.exception.RecursoNoEncontradoException;
import com.faridfarfan.pruebadesarrolladortisenior.exception.ReglaNegocioException;
import com.faridfarfan.pruebadesarrolladortisenior.integration.core.OperacionPoliza;
import com.faridfarfan.pruebadesarrolladortisenior.integration.core.PolizaModificadaEvent;
import com.faridfarfan.pruebadesarrolladortisenior.repository.PolizaRepository;
import com.faridfarfan.pruebadesarrolladortisenior.repository.RiesgoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolizaService {

    private final PolizaRepository polizaRepository;
    private final RiesgoRepository riesgoRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AppProperties properties;

    @Transactional(readOnly = true)
    public List<PolizaResponse> listar(TipoPoliza tipo, EstadoPoliza estado) {
        return polizaRepository.buscar(tipo, estado).stream()
                .map(PolizaResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PolizaResponse obtener(Long polizaId) {
        return PolizaResponse.from(buscarPoliza(polizaId));
    }

    @Transactional(readOnly = true)
    public List<RiesgoResponse> listarRiesgos(Long polizaId) {
        if (!polizaRepository.existsById(polizaId)) {
            throw new RecursoNoEncontradoException("Póliza", polizaId);
        }
        return riesgoRepository.findByPolizaIdOrderById(polizaId).stream()
                .map(RiesgoResponse::from)
                .toList();
    }

    @Transactional
    public PolizaResponse renovar(Long polizaId) {
        Poliza poliza = buscarPoliza(polizaId);
        poliza.renovar(properties.renovacion().ipc());
        log.info("Póliza renovada: id={}, nuevoCanon={}, nuevaPrima={}",
                polizaId, poliza.getCanonMensual(), poliza.getPrima());
        publicar(polizaId, OperacionPoliza.RENOVACION);
        return PolizaResponse.from(poliza);
    }

    @Transactional
    public PolizaResponse cancelar(Long polizaId) {
        Poliza poliza = buscarPoliza(polizaId);
        poliza.cancelar();
        log.info("Póliza cancelada junto con sus riesgos: id={}", polizaId);
        publicar(polizaId, OperacionPoliza.CANCELACION);
        return PolizaResponse.from(poliza);
    }

    @Transactional
    public RiesgoResponse agregarRiesgo(Long polizaId, AgregarRiesgoRequest request) {
        Poliza poliza = buscarPoliza(polizaId);
        if (poliza.getTipo() != TipoPoliza.COLECTIVA) {
            throw new ReglaNegocioException("Solo se pueden agregar riesgos a pólizas colectivas");
        }
        Riesgo riesgo = poliza.agregarRiesgo(
                request.direccionInmueble(), request.arrendatario(), request.arrendador());
        riesgoRepository.save(riesgo);
        log.info("Riesgo agregado: polizaId={}, riesgoId={}", polizaId, riesgo.getId());
        publicar(polizaId, OperacionPoliza.RIESGO_AGREGADO);
        return RiesgoResponse.from(riesgo);
    }

    private Poliza buscarPoliza(Long polizaId) {
        return polizaRepository.findById(polizaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Póliza", polizaId));
    }

    private void publicar(Long polizaId, OperacionPoliza operacion) {
        eventPublisher.publishEvent(new PolizaModificadaEvent(polizaId, operacion));
    }
}
