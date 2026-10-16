package com.faridfarfan.pruebadesarrolladortisenior.controller;

import com.faridfarfan.pruebadesarrolladortisenior.domain.EstadoPoliza;
import com.faridfarfan.pruebadesarrolladortisenior.domain.TipoPoliza;
import com.faridfarfan.pruebadesarrolladortisenior.dto.AgregarRiesgoRequest;
import com.faridfarfan.pruebadesarrolladortisenior.dto.PolizaResponse;
import com.faridfarfan.pruebadesarrolladortisenior.dto.RiesgoResponse;
import com.faridfarfan.pruebadesarrolladortisenior.service.PolizaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Tag(name = "Pólizas")
@RestController
@RequestMapping("/polizas")
@RequiredArgsConstructor
public class PolizaController {

    private final PolizaService polizaService;

    @Operation(summary = "Lista pólizas, filtrando opcionalmente por tipo y estado")
    @GetMapping
    public List<PolizaResponse> listar(@RequestParam(required = false) TipoPoliza tipo,
                                       @RequestParam(required = false) EstadoPoliza estado) {
        return polizaService.listar(tipo, estado);
    }

    @Operation(summary = "Consulta una póliza por id")
    @GetMapping("/{id}")
    public PolizaResponse obtener(@PathVariable Long id) {
        return polizaService.obtener(id);
    }

    @Operation(summary = "Lista los riesgos de una póliza")
    @GetMapping("/{id}/riesgos")
    public List<RiesgoResponse> listarRiesgos(@PathVariable Long id) {
        return polizaService.listarRiesgos(id);
    }

    @Operation(summary = "Renueva la póliza: canon y prima suben según el IPC y el estado pasa a RENOVADA")
    @PostMapping("/{id}/renovar")
    public PolizaResponse renovar(@PathVariable Long id) {
        return polizaService.renovar(id);
    }

    @Operation(summary = "Cancela la póliza y todos sus riesgos")
    @PostMapping("/{id}/cancelar")
    public PolizaResponse cancelar(@PathVariable Long id) {
        return polizaService.cancelar(id);
    }

    @Operation(summary = "Agrega un riesgo (solo pólizas colectivas)")
    @PostMapping("/{id}/riesgos")
    public ResponseEntity<RiesgoResponse> agregarRiesgo(@PathVariable Long id,
                                                        @Valid @RequestBody AgregarRiesgoRequest request) {
        RiesgoResponse riesgo = polizaService.agregarRiesgo(id, request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/riesgos/{id}").buildAndExpand(riesgo.id()).toUri();
        return ResponseEntity.created(ubicacion).body(riesgo);
    }
}
