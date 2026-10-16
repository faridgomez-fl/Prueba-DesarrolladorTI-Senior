package com.faridfarfan.pruebadesarrolladortisenior.controller;

import com.faridfarfan.pruebadesarrolladortisenior.domain.Poliza;
import com.faridfarfan.pruebadesarrolladortisenior.domain.Riesgo;
import com.faridfarfan.pruebadesarrolladortisenior.domain.TipoPoliza;
import com.faridfarfan.pruebadesarrolladortisenior.integration.core.CoreEdicionClient;
import com.faridfarfan.pruebadesarrolladortisenior.integration.core.OperacionPoliza;
import com.faridfarfan.pruebadesarrolladortisenior.repository.PolizaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.datos-demo=false")
@AutoConfigureMockMvc
class PolizaApiIntegrationTest {

    private static final String API_KEY = "123456";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PolizaRepository polizaRepository;

    @MockitoBean
    private CoreEdicionClient coreEdicionClient;

    private Poliza individual;
    private Poliza colectiva;
    private Long riesgoColectivaId;

    @BeforeEach
    void prepararDatos() {
        polizaRepository.deleteAll();
        LocalDate inicio = LocalDate.of(2026, 1, 1);

        individual = Poliza.nueva("IND-1", TipoPoliza.INDIVIDUAL, "Juan Pérez", inicio, 12, new BigDecimal("1000000"));
        individual.agregarRiesgo("Calle 1", "Juan Pérez", "María");

        colectiva = Poliza.nueva("COL-1", TipoPoliza.COLECTIVA, "Inmobiliaria", inicio, 12, new BigDecimal("2000000"));
        colectiva.agregarRiesgo("Apto 101", "Laura", "Carlos");
        colectiva.agregarRiesgo("Apto 102", "Andrés", "Carlos");

        polizaRepository.save(individual);
        polizaRepository.save(colectiva);
        riesgoColectivaId = colectiva.getRiesgos().stream().map(Riesgo::getId).findFirst().orElseThrow();
    }

    @Test
    void sinApiKeyResponde401() throws Exception {
        mockMvc.perform(get("/polizas"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/polizas").header("x-api-key", "otra"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listaPolizasFiltrandoPorTipoYEstado() throws Exception {
        mockMvc.perform(conApiKey(get("/polizas")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(conApiKey(get("/polizas").param("tipo", "COLECTIVA").param("estado", "ACTIVA")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].numero").value("COL-1"));
    }

    @Test
    void filtroConValorInvalidoResponde400() throws Exception {
        mockMvc.perform(conApiKey(get("/polizas").param("tipo", "OTRO")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listaLosRiesgosDeUnaPoliza() throws Exception {
        mockMvc.perform(conApiKey(get("/polizas/{id}/riesgos", colectiva.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].estado").value("ACTIVO"));
    }

    @Test
    void polizaInexistenteResponde404() throws Exception {
        mockMvc.perform(conApiKey(get("/polizas/{id}/riesgos", 9999)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Póliza con id 9999 no existe"));
    }

    @Test
    void renovarIncrementaPorIpcYNotificaAlCore() throws Exception {
        mockMvc.perform(conApiKey(post("/polizas/{id}/renovar", individual.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RENOVADA"))
                .andExpect(jsonPath("$.canonMensual").value(1052000.00))
                .andExpect(jsonPath("$.prima").value(12624000.00));

        verify(coreEdicionClient).notificarActualizacion(individual.getId(), OperacionPoliza.RENOVACION);
    }

    @Test
    void noSePuedeRenovarUnaPolizaCancelada() throws Exception {
        mockMvc.perform(conApiKey(post("/polizas/{id}/cancelar", individual.getId())))
                .andExpect(status().isOk());

        mockMvc.perform(conApiKey(post("/polizas/{id}/renovar", individual.getId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("No se puede renovar una póliza cancelada"));
    }

    @Test
    void cancelarPolizaCancelaSusRiesgosYNotificaAlCore() throws Exception {
        mockMvc.perform(conApiKey(post("/polizas/{id}/cancelar", colectiva.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));

        mockMvc.perform(conApiKey(get("/polizas/{id}/riesgos", colectiva.getId())))
                .andExpect(jsonPath("$[0].estado").value("CANCELADO"))
                .andExpect(jsonPath("$[1].estado").value("CANCELADO"));

        verify(coreEdicionClient).notificarActualizacion(colectiva.getId(), OperacionPoliza.CANCELACION);
    }

    @Test
    void agregaRiesgoAUnaColectiva() throws Exception {
        mockMvc.perform(conApiKey(post("/polizas/{id}/riesgos", colectiva.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"direccionInmueble":"Apto 103","arrendatario":"Sofía","arrendador":"Carlos"}"""))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.polizaId").value(colectiva.getId()))
                .andExpect(jsonPath("$.estado").value("ACTIVO"));

        verify(coreEdicionClient).notificarActualizacion(colectiva.getId(), OperacionPoliza.RIESGO_AGREGADO);
    }

    @Test
    void noSePuedeAgregarRiesgoAUnaIndividual() throws Exception {
        mockMvc.perform(conApiKey(post("/polizas/{id}/riesgos", individual.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"direccionInmueble":"Calle 2","arrendatario":"Juan","arrendador":"María"}"""))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Solo se pueden agregar riesgos a pólizas colectivas"));

        verify(coreEdicionClient, never()).notificarActualizacion(anyLong(), any());
    }

    @Test
    void agregarRiesgoValidaLosCampos() throws Exception {
        mockMvc.perform(conApiKey(post("/polizas/{id}/riesgos", colectiva.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"direccionInmueble\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.direccionInmueble").exists())
                .andExpect(jsonPath("$.errores.arrendatario").exists());
    }

    @Test
    void cancelaUnRiesgo() throws Exception {
        mockMvc.perform(conApiKey(post("/riesgos/{id}/cancelar", riesgoColectivaId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADO"));

        mockMvc.perform(conApiKey(post("/riesgos/{id}/cancelar", riesgoColectivaId)))
                .andExpect(status().isConflict());

        verify(coreEdicionClient).notificarActualizacion(colectiva.getId(), OperacionPoliza.RIESGO_CANCELADO);
    }

    @Test
    void elMockDelCoreRecibeElEvento() throws Exception {
        mockMvc.perform(conApiKey(post("/core-mock/evento"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"evento":"ACTUALIZACION","polizaId":555}"""))
                .andExpect(status().isAccepted());
    }

    private static MockHttpServletRequestBuilder conApiKey(MockHttpServletRequestBuilder request) {
        return request.header("x-api-key", API_KEY);
    }
}
