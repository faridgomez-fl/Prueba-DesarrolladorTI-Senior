package com.faridfarfan.pruebadesarrolladortisenior.controller;

import com.faridfarfan.pruebadesarrolladortisenior.dto.CoreEventoRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Simula el CORE. */
@Slf4j
@Tag(name = "CORE (mock)")
@RestController
@RequestMapping("/core-mock")
public class CoreMockController {

    @Operation(summary = "Registra en el log que se intentó enviar una operación al CORE")
    @PostMapping("/evento")
    public ResponseEntity<Void> recibirEvento(@Valid @RequestBody CoreEventoRequest request) {
        log.info("[CORE-MOCK] Operación enviada al CORE: evento={}, polizaId={}", request.evento(), request.polizaId());
        return ResponseEntity.accepted().build();
    }
}
