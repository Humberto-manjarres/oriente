---
name: webflux-development
description: Patrones de implementación reactiva con Spring WebFlux, Reactor, MongoDB reactivo, MapStruct y Lombok para el proyecto hexagonal `oriente` (com.biblioteca.oriente — domain/model/<contexto>/gateway, domain/model/exception, domain/usecase/<contexto>, infraestructure/entry_points/reactive_web/<contexto>, infraestructure/driven_adapters/reactive_repository/<contexto>_mongo, application). Úsalo al crear modelos, gateways, casos de uso, entities y repositorios Mongo, adapters, transformers MapStruct, controllers @RestController con DTOs, configuración de beans (UsecaseConfig, TransformerConfig) y application.yml, manejo de errores con @RestControllerAdvice + ErrorResponse o pruebas con StepVerifier/WebTestClient.
---

# WebFlux Development

Patrones de referencia por capa, tomados de cómo está organizado el proyecto hoy. La implementación de referencia es el contexto `escritor` (crear y consultar escritor). Este skill no autoriza crear contextos, paquetes ni clases sin aprobación del usuario.

Paquete base: `com.biblioteca.oriente` (raíz: `src/main/java/com/biblioteca/oriente`).

---

## 0. Mapa de paquetes por contexto

Cada contexto funcional (`<contexto>` en minúscula y singular: `escritor`, `libro`, `categoria`) se replica así:

```
com/biblioteca/oriente
├── OrienteApplication.java
├── application                                         ← configuración de beans (sin subpaquete config)
│   ├── UsecaseConfig.java                              ← un @Bean por caso de uso (compartido)
│   └── TransformerConfig.java                          ← un @Bean por transformer MapStruct (compartido)
├── domain
│   ├── model
│   │   ├── <contexto>
│   │   │   ├── <Entidad>.java                          ← modelo de dominio (Lombok)
│   │   │   └── gateway/<Entidad>Gateway.java           ← puerto de salida del contexto
│   │   └── exception/<Nombre>Exception.java            ← excepciones de dominio (compartidas)
│   └── usecase
│       └── <contexto>/<Entidad>UseCase.java            ← un caso de uso por contexto, un método por operación
└── infraestructure                                     ← así, con "e" (no "infrastructure")
    ├── entry_points/reactive_web
    │   ├── exception                                   ← transversal a todos los contextos
    │   │   ├── GlobalExceptionHandler.java             ← @RestControllerAdvice
    │   │   └── ErrorResponse.java                      ← record de respuesta de error
    │   └── <contexto>
    │       ├── <Entidad>Controller.java                ← @RestController
    │       ├── <Entidad>DTO.java                       ← DTO de entrada y salida
    │       └── <Entidad>Transformer.java               ← MapStruct DTO ↔ dominio
    └── driven_adapters/reactive_repository
        └── <contexto>_mongo
            ├── <Entidad>Entity.java                    ← @Document
            ├── <Entidad>Repository.java                ← ReactiveMongoRepository
            ├── <Entidad>TransformerRepository.java     ← MapStruct entity ↔ dominio
            └── <Entidad>Adapter.java                   ← implementa el gateway
```

Reglas:

- **No hay Router ni Handler**: los endpoints son controllers anotados (`@RestController`). No crear `RouterFunction`, `*Router` ni `*Handler` salvo que el usuario lo apruebe.
- Respeta los nombres existentes tal cual: `infraestructure`, `entry_points`, `reactive_web`, `driven_adapters`, `reactive_repository`, sufijo `_mongo` en el paquete del adapter, `UsecaseConfig` (con `c` minúscula).
- Lo específico de un contexto va en su subpaquete; lo transversal (errores HTTP, configuración, excepciones de dominio) va en su paquete compartido.
- Otra tecnología de driven adapter (WebClient, Kafka) sería un hermano de `reactive_repository` dentro de `driven_adapters` y requiere aprobación.

### Dependencias entre capas

| Desde | Puede depender de | Nunca de |
|---|---|---|
| `domain.model` | Lombok, Reactor (`Mono`/`Flux` en gateways) | Spring, Mongo, Jackson, MapStruct, `infraestructure` |
| `domain.usecase` | `domain.model` (entidades, gateways, excepciones), Lombok, Reactor | Spring, `infraestructure` |
| `entry_points` | `domain.usecase`, `domain.model` (para mapear y para excepciones) | `driven_adapters`, repositorios |
| `driven_adapters` | `domain.model` (gateway, entidad) | `domain.usecase`, `entry_points` |
| `application` | todas (solo para registrar beans) | — |

---

## 1. Modelo del dominio — `domain/model/<contexto>`

- Clase con Lombok, igual que `Escritor` y `Libro`: `@Data @Builder @AllArgsConstructor @NoArgsConstructor`.
- Lombok es la única librería permitida en el dominio. Sin Spring, Mongo (`@Document`, `@Id`), Jackson ni MapStruct.
- Solo los campos aprobados; no inventar campos, longitudes ni reglas.
- El `id` es `String` y lo genera Mongo al crear (llega `null`).
- Comentarios cortos por campo cuando el significado no es obvio (como en `Escritor`).

```java
package com.biblioteca.oriente.domain.model.escritor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Escritor {
    private String id;
    private String identificacion; // Identificador único del escritor
    private String nombre;
    private String nacionalidad;
}
```

## 2. Gateway del contexto — `domain/model/<contexto>/gateway`

- Interfaz `<Entidad>Gateway` con tipos del dominio y retorno `Mono`/`Flux`.
- Métodos nombrados `<accion><Entidad>` en español: `crearEscritor`, `consultarEscritor`.
- Sin tipos técnicos en la firma (`EscritorEntity`, `ObjectId`, `ResponseEntity`, `Pageable`).
- **Solo las operaciones que usa un caso de uso aprobado.**

```java
package com.biblioteca.oriente.domain.model.escritor.gateway;

import com.biblioteca.oriente.domain.model.escritor.Escritor;
import reactor.core.publisher.Mono;

public interface EscritorGateway {
    Mono<Escritor> crearEscritor(Escritor escritor);
    Mono<Escritor> consultarEscritor(String identificacion);
}
```

## 3. Excepciones — `domain/model/exception`

- Excepciones de dominio compartidas por todos los contextos, que extienden `RuntimeException`.
- Una excepción por situación de negocio, reutilizable entre contextos mediante parámetros (p. ej. `recurso` + `identificador`), no una por contexto.
- Existentes:
  - `CampoObligatorioException(recurso, List<String> campos)` → 400 (`"<Recurso>: los siguientes campos son obligatorios: a, b"`).
  - `RecursoNoEncontradoException(recurso, identificador)` → 404 (`"<Recurso> no encontrado con identificador: <id>"`).
  - `RecursoDuplicadoException(recurso, identificador)` → 409 (`"<Recurso> ya existe con identificador: <id>"`).

```java
package com.biblioteca.oriente.domain.model.exception;

public class RecursoNoEncontradoException extends RuntimeException {

    private final String recurso;
    private final String identificador;

    public RecursoNoEncontradoException(String recurso, String identificador) {
        super(recurso + " no encontrado con identificador: " + identificador);
        this.recurso = recurso;
        this.identificador = identificador;
    }

    public String getRecurso() { return recurso; }
    public String getIdentificador() { return identificador; }
}
```

Toda excepción nueva debe registrarse en `GlobalExceptionHandler` (sección 7.1).

## 4. Casos de uso — `domain/usecase/<contexto>`

- **Una clase por contexto**, `<Entidad>UseCase`, con **un método por operación** (`crearEscritor`, `obtenerEscritorPorId`). Una operación nueva del contexto es un método nuevo en esa clase, no una clase nueva.
- Sin `@Service`: se registra como `@Bean` en `UsecaseConfig`.
- Inyección por constructor con `@RequiredArgsConstructor` y campo `private final`.
- Depende de la interfaz del gateway, nunca del adapter.
- Aquí viven las reglas de negocio: recurso inexistente → `RecursoNoEncontradoException`, clave de negocio repetida → `RecursoDuplicadoException`.
- Campos obligatorios: validarlos primero, de forma síncrona (`null`, vacío o solo espacios = faltante), y si falta alguno devolver `Mono.error(new CampoObligatorioException(RECURSO, faltantes))` con **todos** los faltantes, sin llamar al gateway. Escritor: `identificacion` y `nombre` obligatorios; `nacionalidad` opcional. Bean Validation (`@NotBlank`/`@Valid`) no está en el proyecto: requiere aprobación.
- Unicidad: antes de crear, consultar por la clave de negocio con el gateway; si existe, error 409 sin llamar a `crear`. Si no existe, crear dentro de `Mono.defer(...)` para que el guardado solo se ejecute cuando la consulta llega vacía. El índice único de Mongo (sección 6.1) cubre las peticiones simultáneas.
- Usa la forma perezosa `Mono.error(() -> new ...)` en `switchIfEmpty`.

```java
package com.biblioteca.oriente.domain.usecase.escritor;

import com.biblioteca.oriente.domain.model.escritor.Escritor;
import com.biblioteca.oriente.domain.model.escritor.gateway.EscritorGateway;
import com.biblioteca.oriente.domain.model.exception.CampoObligatorioException;
import com.biblioteca.oriente.domain.model.exception.RecursoDuplicadoException;
import com.biblioteca.oriente.domain.model.exception.RecursoNoEncontradoException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class EscritorUseCase {

    private static final String RECURSO = "Escritor";

    private final EscritorGateway escritorGateway;

    public Mono<Escritor> crearEscritor(Escritor escritor) {
        List<String> faltantes = camposObligatoriosFaltantes(escritor);
        if (!faltantes.isEmpty()) {
            return Mono.error(new CampoObligatorioException(RECURSO, faltantes));
        }
        return escritorGateway.consultarEscritor(escritor.getIdentificacion())
                .flatMap(existente -> Mono.<Escritor>error(
                        new RecursoDuplicadoException(RECURSO, escritor.getIdentificacion())))
                .switchIfEmpty(Mono.defer(() -> escritorGateway.crearEscritor(escritor)));
    }

    public Mono<Escritor> obtenerEscritorPorId(String identificacion) {
        return escritorGateway.consultarEscritor(identificacion)
                .switchIfEmpty(Mono.error(() -> new RecursoNoEncontradoException(RECURSO, identificacion)));
    }

    private static List<String> camposObligatoriosFaltantes(Escritor escritor) {
        List<String> faltantes = new ArrayList<>();
        if (estaVacio(escritor.getIdentificacion())) {
            faltantes.add("identificacion");
        }
        if (estaVacio(escritor.getNombre())) {
            faltantes.add("nombre");
        }
        return faltantes;
    }

    private static boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
```

## 5. Configuración — `application` y `application.yml`

### `UsecaseConfig`

- Un método `@Bean` por caso de uso; recibe la interfaz del gateway y Spring inyecta el adapter (`@Repository`).

```java
@Configuration
public class UsecaseConfig {

    @Bean
    public EscritorUseCase escritorUseCase(EscritorGateway escritorGateway) {
        return new EscritorUseCase(escritorGateway);
    }
}
```

### `TransformerConfig`

- Los transformers MapStruct se declaran con `@Mapper` **sin** `componentModel`, y se registran aquí como beans con `Mappers.getMapper(...)`.
- Un `@Bean` por transformer: el REST (`<Entidad>Transformer`) y el de repositorio (`<Entidad>TransformerRepository`).

```java
@Configuration
public class TransformerConfig {

    @Bean
    public EscritorTransformer escritorTransformer() {
        return Mappers.getMapper(EscritorTransformer.class);
    }

    @Bean
    public EscritorTransformerRepository escritorTransformerRepository() {
        return Mappers.getMapper(EscritorTransformerRepository.class);
    }
}
```

Si al crear un contexto olvidas registrar su transformer aquí, el arranque falla con *"No qualifying bean of type ...Transformer"*.

### `src/main/resources/application.yml`

- La configuración vive en `application.yml` (puerto 8080, nombre `oriente`, base de datos `oriente_db`). No agregar propiedades nuevas a `application.properties`.
- Nunca credenciales en el archivo; si hacen falta, por variable de entorno (`${MONGODB_URI:...}`).
- Perfiles adicionales (`application-<perfil>.yml`) requieren aprobación.

Estado actual:

```yaml
server:
  port: 8080
spring:
  application:
    name: "oriente"
  mongodb:
    uri: ${MONGODB_URI:mongodb://localhost:27017/oriente_db}
    database: oriente_db
```

> **Spring Boot 4 (el proyecto usa 4.2.0):** la conexión se configura con `spring.mongodb.*`. No usar `spring.data.mongodb.uri`: Boot 4 la ignora y Mongo escribe en la base `test` por defecto (ya ocurrió en este proyecto). Para confirmar la base usada, arrancar con `--logging.level.org.mongodb.driver.protocol.command=DEBUG` y buscar `started on database "oriente_db"`.

## 6. Driven adapters — `infraestructure/driven_adapters/reactive_repository/<contexto>_mongo`

Cuatro clases por contexto: `Entity`, `Repository`, `TransformerRepository` y `Adapter`. Ninguna sale de este paquete salvo a través del gateway.

### 6.1 Entity

- `@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder(toBuilder = true) @Document(collection = "<contexto en plural>")`.
- Colección en plural en español: `escritores`, `libros`, `categorias`.
- Las referencias a otros contextos se guardan como id (`escritorId`, `categoriaId`), no como objetos embebidos.
- Claves de negocio únicas: `@Indexed(unique = true)`. El índice se crea al arrancar porque `application.yml` tiene `spring.data.mongodb.auto-index-creation: true`. Si la colección ya tiene valores repetidos, la creación del índice falla y la aplicación no arranca: limpiar los datos primero.

```java
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Document(collection = "escritores")
public class EscritorEntity {
    private String id;
    @Indexed(unique = true)
    private String identificacion;
    private String nombre;
    private String nacionalidad;
}
```

### 6.2 Repository

- `ReactiveMongoRepository<<Entidad>Entity, String>` con consultas derivadas solo si un gateway las necesita.

```java
public interface EscritorRepository extends ReactiveMongoRepository<EscritorEntity, String> {
    Mono<EscritorEntity> findByIdentificacion(String identificacion);
}
```

### 6.3 TransformerRepository (MapStruct)

- `@Mapper` sin `componentModel`; métodos `toEntity(dominio)` y `toDomain(entity)`.
- Registrar el bean en `TransformerConfig`.
- Solo conversión de forma, sin lógica de negocio. Si los campos difieren entre dominio y entity, declarar `@Mapping` explícitos.

```java
@Mapper
public interface EscritorTransformerRepository {
    EscritorEntity toEntity(Escritor escritor);
    Escritor toDomain(EscritorEntity escritorEntity);
}
```

### 6.4 Adapter

- `@Repository @RequiredArgsConstructor`, implementa `<Entidad>Gateway`.
- Campos: `repository` y `mapper` (el `TransformerRepository`).
- Para crear usa `repository.insert(...)`, no `save`: `save` hace upsert y sobrescribe en silencio si llega un id existente.
- Traduce `DuplicateKeyException` (violación del índice único) a `RecursoDuplicadoException` con `onErrorMap`; los demás errores se propagan tal cual.
- Logs con SLF4J (`@Slf4j`), nunca `System.out` / `System.err`.

```java
@Slf4j
@Repository
@RequiredArgsConstructor
public class EscritorAdapter implements EscritorGateway {

    private final EscritorRepository repository;
    private final EscritorTransformerRepository mapper;

    @Override
    public Mono<Escritor> crearEscritor(Escritor escritor) {
        return Mono.fromSupplier(() -> mapper.toEntity(escritor))
                .flatMap(repository::insert)
                .doOnNext(guardado -> log.debug("Escritor guardado: id={} identificacion={}",
                        guardado.getId(), guardado.getIdentificacion()))
                .onErrorMap(DuplicateKeyException.class,
                        error -> new RecursoDuplicadoException("Escritor", escritor.getIdentificacion()))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Escritor> consultarEscritor(String identificacion) {
        return repository.findByIdentificacion(identificacion)
                .map(mapper::toDomain);
    }
}
```

### 6.5 Clientes REST (WebClient)

- Paquete propio hermano de `reactive_repository` (requiere aprobación).
- El `WebClient` se construye como bean en `application` (base URL y timeouts desde `application.yml`).
- En el adapter: `retrieve()`, `onStatus(...)` para mapear errores, `.timeout(...)`, `retryWhen(Retry.backoff(...))` solo para errores transitorios (5xx, timeout), nunca para 4xx.

## 7. Entry points — `infraestructure/entry_points/reactive_web/<contexto>`

Tres clases por contexto: `Controller`, `DTO` y `Transformer`. **Endpoints anotados con `@RestController`**, no Router/Handler.

### 7.1 Controller

- `@RestController @RequiredArgsConstructor @RequestMapping(<Entidad>Controller.<CONTEXTO>_PATH)`, con el path como constante pública (`ESCRITOR_PATH = "/escritor"`, en singular).
- Campos: `transformer` y el `<Entidad>UseCase`.
- Rutas REST, **sin verbos en la URL** (`/crear`, `/listar`…): un segmento fijo junto a `/{variable}` hace la ruta ambigua (`GET /escritor/crear` se interpretaba como el escritor "crear").
  - Crear: `@PostMapping` sobre la raíz (`POST /escritor`) → **201 Created** + header `Location` (construido con `UriComponentsBuilder` y codificado) + body con el recurso.
  - Consultar: `@GetMapping("/{identificacion}")` → 200. La variable se llama como el dato que recibe (la clave de negocio, no el `id` de Mongo).
- El flujo es: DTO → `transformer.toEntity` → caso de uso → `transformer::toDTO`.
- Sin lógica de negocio, sin acceso a repositorios ni a `driven_adapters`, sin `System.out`.

```java
@RestController
@RequiredArgsConstructor
@RequestMapping(EscritorController.ESCRITOR_PATH)
public class EscritorController {

    public static final String ESCRITOR_PATH = "/escritor";

    private final EscritorTransformer transformer;
    private final EscritorUseCase escritorUseCase;

    @PostMapping
    public Mono<ResponseEntity<EscritorDTO>> crear(@RequestBody EscritorDTO escritorDTO) {
        return escritorUseCase.crearEscritor(transformer.toEntity(escritorDTO))
                .map(transformer::toDTO)
                .map(creado -> ResponseEntity.created(ubicacion(creado.getIdentificacion())).body(creado));
    }

    @GetMapping(path = "/{identificacion}")
    public Mono<EscritorDTO> consultarPorIdentificacion(@PathVariable String identificacion) {
        return escritorUseCase.obtenerEscritorPorId(identificacion)
                .map(transformer::toDTO);
    }

    private static URI ubicacion(String identificacion) {
        return UriComponentsBuilder.fromPath(ESCRITOR_PATH)
                .pathSegment("{identificacion}")
                .buildAndExpand(identificacion)
                .encode()
                .toUri();
    }
}
```

### 7.2 DTO

- Clase `<Entidad>DTO` con `@Data @Builder @AllArgsConstructor @NoArgsConstructor`, usada para entrada y salida.
- Separada del modelo de dominio aunque hoy tenga los mismos campos.

```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EscritorDTO {
    private String id;
    private String identificacion;
    private String nombre;
    private String nacionalidad;
}
```

### 7.3 Transformer (MapStruct)

- `@Mapper` sin `componentModel`, registrado en `TransformerConfig`.
- Métodos `toEntity(DTO)` → dominio y `toDTO(dominio)` → DTO (en esta capa "entity" significa la entidad de dominio).

```java
@Mapper
public interface EscritorTransformer {
    Escritor toEntity(EscritorDTO escritorDTO);
    EscritorDTO toDTO(Escritor escritor);
}
```

### 7.4 Manejo de errores — `entry_points/reactive_web/exception`

Ya existe un único manejador global compartido. **No crear otro por contexto**; agregar un `@ExceptionHandler` nuevo cuando se apruebe una excepción.

- `GlobalExceptionHandler` con `@RestControllerAdvice` y `@Slf4j`; cada método recibe la excepción y el `ServerWebExchange`, registra el error en el log y delega en el helper privado `responder(status, mensaje, exchange)`, que arma el `Mono<ResponseEntity<ErrorResponse>>`.
- Cuerpo: `ErrorResponse(status, error, message, path, timestamp)` (record).
- Log: `warn` para errores del cliente (4xx), `error` con stack trace para el 500. El stack trace va solo al log, nunca a la respuesta.

```java
@ExceptionHandler(RecursoNoEncontradoException.class)
public Mono<ResponseEntity<ErrorResponse>> handleNotFound(
        RecursoNoEncontradoException ex, ServerWebExchange exchange) {

    log.warn("Recurso no encontrado: {}", ex.getMessage());
    return responder(HttpStatus.NOT_FOUND, ex.getMessage(), exchange);
}
```

Mapeo vigente (Spring elige el manejador más específico):

| Excepción | HTTP | `message` |
|---|---|---|
| `CampoObligatorioException` | 400 | el de la excepción (lista los campos faltantes) |
| `RecursoNoEncontradoException` | 404 | el de la excepción |
| `ServerWebInputException` (body ausente, JSON inválido, parámetro con tipo incorrecto) | 400 | `MENSAJE_SOLICITUD_INVALIDA` (genérico, sin detalle técnico) |
| Otras `ResponseStatusException` (405, 415, 406…) | se conserva su código | texto estándar del código (`"Method Not Allowed"`…) |
| `RecursoDuplicadoException` | 409 | el de la excepción |
| `Exception` (cualquier otra) | 500 | `MENSAJE_ERROR_INTERNO` (`"Ocurrió un error interno en el servidor"`) |

- Cualquier código HTTP nuevo se acuerda con el usuario. Nunca exponer stack traces ni mensajes técnicos.
- Probar con curl en Windows: enviar JSON con tildes desde archivo (`--data-binary @archivo.json`); en línea de comandos curl los envía mal codificados y la API responde 400 (correcto).

## 8. Operadores Reactor: guía rápida

| Situación | Usar | Evitar |
|---|---|---|
| Transformación síncrona | `map` | `flatMap` innecesario |
| Llamada que devuelve `Mono`/`Flux` | `flatMap` / `concatMap` (orden) | `map` que deja `Mono<Mono<T>>` |
| Valor ausente → error | `switchIfEmpty(Mono.error(() -> ...))` | `switchIfEmpty(Mono.error(new ...))` (crea la excepción siempre) |
| Valor por defecto costoso | `switchIfEmpty(Mono.defer(...))` | `defaultIfEmpty(calcular())` |
| Combinar independientes | `Mono.zip(a, b)` | `flatMap` anidados en serie |
| Mapeo que puede lanzar al inicio del flujo | `Mono.fromSupplier(...)` | llamarlo fuera del flujo |
| Efectos secundarios (log, métricas) | `doOnNext`, `doOnError` con SLF4J | `System.out`, lógica de negocio en `doOn*` |
| Traducir errores | `onErrorMap` | `onErrorResume` que oculta errores |
| Resultado vacío tras operación | `then()` / `thenReturn(x)` | `map(x -> null)` |
| Código bloqueante | `fromCallable(...).subscribeOn(Schedulers.boundedElastic())` | llamarlo directo en el event loop |

## 9. Pruebas

Solo cuando el usuario las pida o apruebe.

Ubicación: `src/test/java/com/biblioteca/oriente/...` replicando exactamente el paquete de la clase probada (p. ej. `infraestructure/entry_points/reactive_web/escritor/EscritorControllerTest.java`). Clase `<Clase>Test`.

### Caso de uso (Mockito + StepVerifier)

```java
@ExtendWith(MockitoExtension.class)
class EscritorUseCaseTest {

    @Mock EscritorGateway escritorGateway;
    @InjectMocks EscritorUseCase useCase;

    @Test
    void debeLanzarNoEncontradoCuandoNoExiste() {
        when(escritorGateway.consultarEscritor("123")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.obtenerEscritorPorId("123"))
                .expectError(RecursoNoEncontradoException.class)
                .verify();
    }
}
```

### Controller (WebTestClient)

- `WebTestClient.bindToController(controller).controllerAdvice(new GlobalExceptionHandler()).build()`, con el caso de uso mockeado y el transformer real (`Mappers.getMapper(EscritorTransformer.class)`).
- Verificar el body del camino feliz y los errores (404 con `ErrorResponse`, 500 genérico).

### Transformers MapStruct

- Unitaria sin Spring con `Mappers.getMapper(...)`; verificar ida y vuelta de todos los campos.
- En pruebas de adapter/controller usar el transformer real, no un mock.

### Adapter

- Unitaria con el repositorio mockeado y el transformer real.
- Integración con Mongo (Testcontainers) solo si el usuario la aprueba: añade dependencias.

### `OrienteApplicationTests`

- Es `@SpringBootTest` y levanta los repositorios Mongo: requiere Mongo disponible en `localhost:27017`.

### Reglas

- Cubre camino feliz, vacío y error en cada flujo.
- Nunca `block()` en pruebas; usa `StepVerifier`.
- Tiempo/retries: `StepVerifier.withVirtualTime`.

## 10. Entorno y verificación

- El `JAVA_HOME` del sistema puede apuntar a JDK 8; el proyecto usa toolchain Java 17. Ejecutar siempre:

```bash
cd /c/Sura/oriente
JAVA_HOME="/c/Program Files/Java/jdk-17" ./gradlew compileJava --console=plain   # compilar (genera MapStruct)
JAVA_HOME="/c/Program Files/Java/jdk-17" ./gradlew test --console=plain          # pruebas
JAVA_HOME="/c/Program Files/Java/jdk-17" ./gradlew bootRun                       # levantar (puerto 8080)
```

- Tras mover o renombrar clases, usar `clean compileJava` para descartar implementaciones MapStruct generadas obsoletas.
- Prueba manual: requiere Mongo en `localhost:27017`. **Una petición exitosa escribe datos reales**: avisar al usuario e informar los ids creados. Detener la aplicación al terminar.

```bash
curl -i --location 'http://localhost:8080/escritor' \
--header 'Content-Type: application/json' \
--data '{"identificacion": "1001", "nombre": "Gabriel García Márquez", "nacionalidad": "Colombiana"}'
# 201 + Location: /escritor/1001; repetirlo da 409

curl --location 'http://localhost:8080/escritor/1001'
```

En Git Bash de Windows, si el JSON lleva tildes, envíalo desde un archivo UTF-8 (`--data-binary @escritor.json`); en línea de comandos llega mal codificado y la API responde 400.

## 11. Estado actual y deuda conocida

No corregir sin aprobación; señalarlo al usuario cuando la tarea toque esos archivos.

- `categoria` y `libro` están en esqueleto: `Categoria`, `CategoriaAdapter`, `CategoriaEntity`, `CategoriaRepository`, `LibroAdapter` y `LibroRepository` están vacíos; no tienen gateway, caso de uso, transformers ni entry point.
- `Libro` (dominio: `id`, `descripcion`) no coincide con `LibroEntity` (`codigoLibro`, `nombre`, `valorUnitario`, `escritorId`, `categoriaId`). Hay que alinear los campos antes de crear su transformer.
- `EscritorUseCase.obtenerEscritorPorId` conserva su nombre aunque busca por `identificacion` (renombrarlo implica cambiar sus pruebas).
- `build.gradle` define `lombokMapstructBindingVersion` pero no declara `lombok-mapstruct-binding`. Como el dominio y los DTOs usan Lombok, si MapStruct reporta *"Unknown property"* o genera mapeos vacíos, falta agregar `annotationProcessor "org.projectlombok:lombok-mapstruct-binding:${lombokMapstructBindingVersion}"` (y su `testAnnotationProcessor`).
- `application.properties` repite `spring.application.name` (la configuración real está en `application.yml`).

## 12. Checklist antes de entregar

- [ ] La tarea está aprobada y no se adelantó trabajo no pedido.
- [ ] Clases en los paquetes de la sección 0, con los nombres existentes (`infraestructure`, `entry_points/reactive_web`, `driven_adapters/reactive_repository/<contexto>_mongo`).
- [ ] Sin Router/Handler: endpoints en `<Entidad>Controller` con `@RestController`.
- [ ] `domain` no importa Spring, Mongo, Jackson ni MapStruct (solo Lombok y Reactor).
- [ ] Gateway solo con operaciones usadas por el caso de uso.
- [ ] Caso de uso registrado en `UsecaseConfig`; transformers registrados en `TransformerConfig`.
- [ ] Transformers MapStruct sin lógica de negocio.
- [ ] Excepciones nuevas en `domain/model/exception` y mapeadas en `GlobalExceptionHandler` con `ErrorResponse`.
- [ ] Ningún `block()`, `subscribe()`, `Thread.sleep()` ni `System.out` en código nuevo.
- [ ] Inyección por constructor (`@RequiredArgsConstructor` + `private final`).
- [ ] Compilación (y pruebas, si se pidieron) ejecutadas con JDK 17 y resultado reportado tal cual.
