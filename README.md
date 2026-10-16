# API de Gestión de Pólizas

API REST para gestionar pólizas de arrendamiento **individuales** y **colectivas** y sus riesgos.
Construida con **Spring Boot 4.1 + Java 21**, organizada en capas *controller → service → repository*.

## Requisitos

- JDK 21
- No hace falta instalar Gradle ni una base de datos: el proyecto trae el *wrapper* de Gradle y usa H2 en memoria.

## Cómo ejecutar

```bash
# Windows
gradlew.bat bootRun
# Linux / macOS
./gradlew bootRun
```

O desde IntelliJ: ejecutar `PruebaDesarrolladortiSeniorApplication`.

La aplicación queda en `http://localhost:8080` y arranca con 4 pólizas de ejemplo:

| id | Número | Tipo | Estado | Riesgos |
|---|---|---|---|---|
| 1 | POL-IND-001 | INDIVIDUAL | ACTIVA | 1 |
| 2 | POL-IND-002 | INDIVIDUAL | ACTIVA | 1 |
| 3 | POL-COL-001 | COLECTIVA | ACTIVA | 2 |
| 4 | POL-COL-002 | COLECTIVA | CANCELADA | 1 (cancelado) |

## Cómo ejecutar las pruebas

```bash
gradlew.bat test      # Windows
./gradlew test        # Linux / macOS
```

Hay 22 pruebas: unitarias de las reglas de negocio (`PolizaTest`) y de integración de todos los
endpoints con seguridad, JPA y H2 (`PolizaApiIntegrationTest`).

## Cómo probar el API

Primero hay que levantar la aplicación (ver *Cómo ejecutar*). Luego se puede probar de dos formas:
**Swagger UI**, que viene incluido en la aplicación, o **Postman**, con la colección incluida en el proyecto.

En cualquiera de las dos, todas las peticiones deben llevar el header `x-api-key: 123456`;
sin él la respuesta es 401.

### Opción 1: Swagger UI (sin instalar nada)

1. Abrir en el navegador `http://localhost:8080/swagger-ui.html`.
2. Clic en **Authorize** (arriba a la derecha), escribir `123456`, clic en **Authorize** y luego **Close**.
3. Desplegar un endpoint, clic en **Try it out**, llenar los parámetros y clic en **Execute**.
4. La respuesta aparece abajo, en *Server response*.

### Opción 2: Postman

1. Abrir Postman y clic en **Import**.
2. Seleccionar el archivo [`postman/polizas.postman_collection.json`](postman/polizas.postman_collection.json).
3. Abrir cualquier petición de la colección **API de Gestión de Pólizas** y clic en **Send**.

La colección ya trae configurados la URL (`baseUrl = http://localhost:8080`) y la API key
(`apiKey = 123456`) como variables, e incluye 12 peticiones: los casos exitosos, los que deben
responder 409 por reglas de negocio y uno sin API key que responde 401.

Si se prefiere armar las peticiones a mano en Postman: en **Headers** agregar `x-api-key` = `123456`,
y para los POST con cuerpo usar **Body → raw → JSON**.

### Datos de ejemplo para probar

| id póliza | Tipo | Estado | Para probar |
|---|---|---|---|
| 1 | INDIVIDUAL | ACTIVA | Renovar; agregar riesgo da 409 (es individual) |
| 2 | INDIVIDUAL | ACTIVA | Cancelar |
| 3 | COLECTIVA | ACTIVA | Ver riesgos, agregar riesgo, cancelar riesgo (ids 3 y 4) |
| 4 | COLECTIVA | CANCELADA | Renovar da 409 (está cancelada) |

Cuerpo de ejemplo para `POST /polizas/3/riesgos`:

```json
{
  "direccionInmueble": "Calle 72 # 10-07 Apto 303",
  "arrendatario": "Sofía Herrera",
  "arrendador": "Carlos Ruiz"
}
```

### Con curl (opcional)

```bash
curl -H "x-api-key: 123456" "http://localhost:8080/polizas?tipo=COLECTIVA&estado=ACTIVA"
curl -H "x-api-key: 123456" http://localhost:8080/polizas/3/riesgos
curl -X POST -H "x-api-key: 123456" http://localhost:8080/polizas/1/renovar
curl -X POST -H "x-api-key: 123456" http://localhost:8080/polizas/2/cancelar
curl -X POST -H "x-api-key: 123456" -H "Content-Type: application/json" \
  -d '{"direccionInmueble":"Calle 72 # 10-07 Apto 303","arrendatario":"Sofía Herrera","arrendador":"Carlos Ruiz"}' \
  http://localhost:8080/polizas/3/riesgos
curl -X POST -H "x-api-key: 123456" http://localhost:8080/riesgos/3/cancelar
curl -X POST -H "x-api-key: 123456" -H "Content-Type: application/json" \
  -d '{"evento":"ACTUALIZACION","polizaId":555}' http://localhost:8080/core-mock/evento
```

Cada operación que cambia una póliza deja en la consola de la aplicación la línea
`[CORE-MOCK] Operación enviada al CORE: evento=ACTUALIZACION, polizaId=...`.

## Endpoints

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/polizas?tipo=&estado=` | Lista pólizas; ambos filtros son opcionales | 200, 400 si el valor del filtro no existe |
| GET | `/polizas/{id}` | Consulta una póliza (consumo del front) | 200, 404 |
| GET | `/polizas/{id}/riesgos` | Riesgos de la póliza | 200, 404 |
| POST | `/polizas/{id}/renovar` | Canon y prima suben según el IPC; la vigencia se corre un periodo; estado RENOVADA | 200, 404, 409 si está cancelada |
| POST | `/polizas/{id}/cancelar` | Cancela la póliza y todos sus riesgos | 200, 404, 409 si ya está cancelada |
| POST | `/polizas/{id}/riesgos` | Agrega un riesgo; solo pólizas colectivas | 201 + `Location`, 400, 404, 409 |
| POST | `/riesgos/{id}/cancelar` | Cancela un riesgo de una póliza colectiva | 200, 404, 409 |
| POST | `/core-mock/evento` | Simula el CORE: solo registra en el log | 202 |

Valores: `tipo` = `INDIVIDUAL` | `COLECTIVA`; `estado` = `ACTIVA` | `RENOVADA` | `CANCELADA`.

Sin el header `x-api-key` correcto la respuesta es **401**. Los errores siguen el formato estándar
*Problem Details* (RFC 9457):

```json
{ "status": 409, "title": "Regla de negocio", "detail": "No se puede renovar una póliza cancelada" }
```

## Reglas de negocio

| Regla | Dónde se valida |
|---|---|
| Prima = canon mensual × meses de vigencia | `Poliza.nueva` y `Poliza.renovar` |
| Renovar: canon × (1 + IPC), prima recalculada, mismo periodo de vigencia, estado RENOVADA | `Poliza.renovar` |
| No se puede renovar una póliza cancelada | `Poliza.renovar` |
| Cancelar una póliza cancela todos sus riesgos | `Poliza.cancelar` |
| Una póliza individual solo puede tener 1 riesgo | `Poliza.agregarRiesgo` |
| Agregar riesgo exige que la póliza sea colectiva | `PolizaService.agregarRiesgo` |
| Solo se cancelan individualmente riesgos de pólizas colectivas | `Riesgo.cancelarIndividualmente` |
| Toda acción que cambia estado notifica al CORE | `PolizaModificadaEvent` → `CoreEventoListener` |

El IPC se configura en `application.properties` (`app.renovacion.ipc=0.0520`, es decir 5,20 %).

## Estructura del proyecto

```
src/main/java/com/faridfarfan/pruebadesarrolladortisenior
├── controller/        Capa web: PolizaController, RiesgoController, CoreMockController
├── service/           Casos de uso y transacciones: PolizaService, RiesgoService
├── repository/        Acceso a datos con Spring Data JPA
├── domain/            Entidades Poliza y Riesgo con sus reglas, y los enums
├── dto/               Records de entrada/salida del API (nunca se exponen entidades)
├── integration/core/  Integración con el CORE: puerto, adapter HTTP, evento y listener
├── exception/         Excepciones de negocio y manejador global de errores
└── config/            Seguridad (x-api-key), propiedades, Swagger y datos de ejemplo
```

## Decisiones de diseño

- **Modelo de dominio rico.** Las reglas viven en las entidades (`Poliza` es la raíz del agregado y
  sus riesgos solo cambian a través de ella). Los servicios orquestan: buscan, invocan al dominio
  y publican eventos. Así las reglas se prueban sin Spring y no se duplican.
- **Integración con el CORE desacoplada.** `CoreEdicionClient` es un puerto (interfaz) y
  `CoreEdicionRestClient` su adapter HTTP. Si el CORE real en WebLogic usa otro protocolo, solo cambia el adapter.
- **Notificación después del commit.** Los servicios publican `PolizaModificadaEvent` y
  `CoreEventoListener` lo envía con `@TransactionalEventListener(AFTER_COMMIT)`: nunca se informa al
  CORE un cambio que luego se revirtió. Si el CORE falla, la operación local no se pierde y queda el error en el log.
  En producción se completaría con el patrón *outbox* y reintentos (o una cola) para garantizar la entrega.
- **Dinero con `BigDecimal`**, escala 2 y redondeo `HALF_UP`; nunca `double`.
- **Bloqueo optimista** (`@Version`): si dos peticiones modifican la misma póliza a la vez, la segunda recibe 409.
- **Seguridad** con Spring Security y un filtro propio para `x-api-key`, comparando en tiempo constante.
  La clave se puede cambiar con la variable de entorno `API_KEY`.
- **Errores uniformes** con `ProblemDetail`: 400 validación, 401 sin API key, 404 no existe, 409 regla de negocio.
- **`open-in-view` deshabilitado** y DTOs construidos dentro de la transacción del servicio.
- **Configuración validada al arrancar** con `@ConfigurationProperties` (`AppProperties`).

## Supuestos

- Renovar una póliza ya `RENOVADA` está permitido (se renueva otra vez por el mismo periodo).
- La renovación se ejecuta bajo demanda con `POST /polizas/{id}/renovar`; la renovación automática
  (un proceso programado que renueve las que vencen) queda fuera del alcance de este módulo.
- `POST /riesgos/{id}/cancelar` aplica a pólizas colectivas; en una individual el riesgo es la póliza
  misma, así que se cancela la póliza.
- El servicio de edición se invoca por HTTP contra `/core-mock/evento` del mismo API
  (`app.core.url`), con el cuerpo `{"evento": "ACTUALIZACION", "polizaId": <id>}`.
