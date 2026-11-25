# CLAUDE.md — oriente

Microservicio reactivo de una biblioteca (escritores, libros, categorías) con arquitectura hexagonal.

Este archivo es la fuente de verdad del proyecto y prevalece sobre los agentes (`.claude/agents`) y el skill `webflux-development`. Los patrones de código por capa están en `.claude/skills/webflux-development/SKILL.md`.

## Stack

- Java 17 (toolchain de Gradle)
- Spring Boot 4.2.0 / Spring Framework 7
- Spring WebFlux + Project Reactor
- Spring Data MongoDB Reactive (base de datos `oriente_db`)
- Lombok
- MapStruct 1.6.3
- Gradle (wrapper incluido)
- Pruebas: JUnit 5, Mockito, StepVerifier, WebTestClient

Grupo `com.biblioteca`, artefacto `oriente`, paquete base `com.biblioteca.oriente`.

## Estructura de paquetes

```
src/main/java/com/biblioteca/oriente
├── OrienteApplication.java
├── application
│   ├── UsecaseConfig.java                          @Bean por caso de uso
│   └── TransformerConfig.java                      @Bean por transformer (Mappers.getMapper)
├── domain
│   ├── model
│   │   ├── <contexto>
│   │   │   ├── <Entidad>.java                      Lombok @Data @Builder
│   │   │   └── gateway/<Entidad>Gateway.java       puerto de salida
│   │   └── exception/<Nombre>Exception.java        excepciones de dominio compartidas
│   └── usecase
│       └── <contexto>/<Entidad>UseCase.java        una clase por contexto, un método por operación
└── infraestructure
    ├── entry_points/reactive_web
    │   ├── exception
    │   │   ├── GlobalExceptionHandler.java         @RestControllerAdvice (compartido)
    │   │   └── ErrorResponse.java                  record de error
    │   └── <contexto>
    │       ├── <Entidad>Controller.java            @RestController
    │       ├── <Entidad>DTO.java
    │       └── <Entidad>Transformer.java           MapStruct DTO ↔ dominio
    └── driven_adapters/reactive_repository
        └── <contexto>_mongo
            ├── <Entidad>Entity.java                @Document
            ├── <Entidad>Repository.java            ReactiveMongoRepository
            ├── <Entidad>TransformerRepository.java MapStruct entity ↔ dominio
            └── <Entidad>Adapter.java               @Repository, implementa el gateway

src/main/resources/application.yml                  configuración (puerto 8080, Mongo)
```

Reglas de estructura:

- Respetar los nombres existentes tal cual: `infraestructure` (con "e"), `entry_points/reactive_web`, `driven_adapters/reactive_repository`, sufijo `_mongo`, `UsecaseConfig`.
- Endpoints con `@RestController`. **No usar Router/Handler** (`RouterFunction`) sin aprobación.
- Contextos en minúscula y singular: `escritor`, `libro`, `categoria`. Colecciones Mongo en plural: `escritores`, `libros`, `categorias`.
- Lo específico de un contexto va en su subpaquete; lo transversal (errores HTTP, configuración, excepciones de dominio) en su paquete compartido.
- No crear `application/config`, `port/in`, `port/out`, `domain/gateway` global ni otros paquetes sin aprobación.
- Otra tecnología de salida (WebClient, Kafka) va como hermana de `reactive_repository` dentro de `driven_adapters` y requiere aprobación.

## Contextos

| Contexto | Estado |
|---|---|
| `escritor` | Implementado: crear (`POST /escritor` → 201 + `Location`; 409 si la identificación ya existe) y consultar por identificación (`GET /escritor/{identificacion}`). `identificacion` y `nombre` son obligatorios (400 si faltan; `nacionalidad` es opcional). `identificacion` es única (validación en el caso de uso + índice único en Mongo). Implementación de referencia. |
| `libro` | Esqueleto: modelo `Libro` (`id`, `descripcion`) y `LibroEntity` (`codigoLibro`, `nombre`, `valorUnitario`, `escritorId`, `categoriaId`) no coinciden; `LibroAdapter` y `LibroRepository` vacíos. |
| `categoria` | Esqueleto: `Categoria`, `CategoriaAdapter`, `CategoriaEntity`, `CategoriaRepository` vacíos. |

No completar `libro` ni `categoria` sin un requerimiento funcional aprobado (campos, obligatoriedad, unicidad, rutas, códigos HTTP).

## Dependencias permitidas

```
entry_points    -> domain.usecase, domain.model (mapear, excepciones)
domain.usecase  -> domain.model (entidad, gateway, exception)
driven_adapters -> domain.model (gateway, entidad)
application     -> usecase, gateway, transformers (solo registrar beans)
```

Prohibido:

- `domain` → Spring, Mongo, Jackson, MapStruct, `infraestructure`, `application`. En el dominio solo se permiten Java estándar, Lombok y Reactor.
- `entry_points` → `driven_adapters` o repositorios.
- `driven_adapters` → `domain.usecase` o `entry_points`.
- Casos de uso con `@Service` / `@Component`: se registran en `UsecaseConfig`.

## Convenciones de código

- Inyección por constructor con `@RequiredArgsConstructor` + `private final`; nunca `@Autowired` en campos.
- Gateways y casos de uso devuelven `Mono`/`Flux`; métodos en español `<accion><Entidad>` (`crearEscritor`, `consultarEscritor`).
- Gateways solo con operaciones que usa un caso de uso aprobado.
- Reglas de negocio en el caso de uso (p. ej. inexistente → `RecursoNoEncontradoException`); controllers y adapters sin lógica de negocio.
- Campos obligatorios: se validan al inicio del caso de uso (`null`, vacío o solo espacios = faltante), antes de llamar al gateway, y se reportan todos juntos con `CampoObligatorioException(recurso, campos)` → 400. No usar Bean Validation (`@NotBlank`/`@Valid`) sin aprobación: el proyecto no tiene `spring-boot-starter-validation`.
- MapStruct: `@Mapper` sin `componentModel`, métodos `toEntity`/`toDomain` (repositorio) y `toEntity`/`toDTO` (REST), registrados en `TransformerConfig`.
- Errores HTTP solo en `GlobalExceptionHandler` con `ErrorResponse(status, error, message, path, timestamp)`. Mapeo vigente: `CampoObligatorioException` → 400, `RecursoNoEncontradoException` → 404, `RecursoDuplicadoException` → 409, `ServerWebInputException` (body ausente, JSON inválido) → 400, otras `ResponseStatusException` (405, 415…) → su propio código, `Exception` → 500 con mensaje genérico. Los 4xx se registran con `log.warn` y el 500 con `log.error` y stack trace. Códigos nuevos requieren aprobación.
- Rutas REST sin verbos en la URL: `POST /<contexto>` (201 + `Location`), `GET /<contexto>/{clave}`. No usar segmentos como `/crear` junto a `/{variable}`: generan rutas ambiguas.
- Prohibido en código productivo: `block()`, `subscribe()`, `Thread.sleep()`, `Future.get()`, `CompletableFuture.join()`, `System.out` / `System.err` (usar SLF4J `@Slf4j` en infraestructura).
- Para crear documentos usar `insert`, no `save` (evita upserts silenciosos).
- Claves de negocio únicas: validar en el caso de uso (→ `RecursoDuplicadoException`) y declarar `@Indexed(unique = true)` en la entity; el adapter traduce `DuplicateKeyException` a la misma excepción. Requiere `spring.data.mongodb.auto-index-creation: true` (ya configurado).
- Nunca credenciales en `application.yml`; usar variables de entorno.
- Mongo se configura con `spring.mongodb.uri` / `spring.mongodb.database` (Spring Boot 4). `spring.data.mongodb.uri` se ignora y hace que se escriba en la base `test`.

## Flujo de trabajo por fases

Cada fase requiere aprobación explícita del usuario (`aprobado`, `continuar`, `siguiente paso`, `generar`, `proceed`). El usuario puede aprobar una fase o un rango (p. ej. "Fases 3 a 7"); se ejecuta exactamente ese alcance y se detiene al final.

| Fase | Contenido | Responsable |
|---|---|---|
| 1 | Análisis del requerimiento y preguntas abiertas | `hexagonal-architect` |
| 2 | Diseño: paquetes, clases, rutas, códigos HTTP | `hexagonal-architect` |
| 3 | Modelo de dominio y excepciones | `webflux-developer` |
| 4 | Gateway del contexto | `webflux-developer` |
| 5 | Caso de uso | `webflux-developer` |
| 6 | Driven adapter: Entity, Repository, TransformerRepository, Adapter | `webflux-developer` |
| 7 | Entry point: DTO, Transformer, Controller | `webflux-developer` |
| 8 | Configuración (`UsecaseConfig`, `TransformerConfig`, `application.yml`) y manejo de errores | `webflux-developer` |
| 9 | Pruebas unitarias | `webflux-developer` |
| 10 | Verificación: compilación, pruebas y prueba manual con curl | `webflux-developer` |

- Nunca asumir requerimientos funcionales: preguntar atributos, obligatoriedad, unicidad, generación del id, rutas y códigos HTTP.
- Si el usuario pide implementar sin pruebas, se omite la fase 9 pero siempre se compila.
- Si solo se pide análisis o diseño, no generar código.

## Comandos

El `JAVA_HOME` del sistema puede apuntar a JDK 8; usar siempre el JDK 17:

```bash
cd /c/Sura/oriente
JAVA_HOME="/c/Program Files/Java/jdk-17" ./gradlew compileJava --console=plain
JAVA_HOME="/c/Program Files/Java/jdk-17" ./gradlew test --console=plain
JAVA_HOME="/c/Program Files/Java/jdk-17" ./gradlew bootRun
```

- Tras mover o renombrar clases: `clean compileJava` (descarta código MapStruct obsoleto).
- `bootRun` y `OrienteApplicationTests` requieren Mongo en `localhost:27017`. Una petición exitosa escribe datos reales: avisar al usuario, informar los ids creados y detener la aplicación al terminar.

## Deuda conocida

No corregir sin aprobación; señalarla cuando la tarea toque esos archivos.

- `build.gradle` define `lombokMapstructBindingVersion` pero no declara `lombok-mapstruct-binding`.
- `application.properties` repite `spring.application.name` (la configuración real está en `application.yml`).

## Git

- Ramas: `feature/<id>`, `bugfix/<id>`, `lab/<descripcion>`, en minúscula y con `_`.
- Commits en inglés: `<type>(<scope>): <subject>` (`feat`, `fix`, `docs`, `refactor`, `test`, `chore`, `build`, `config`…).
- No hacer commit, push, merge ni PR sin aprobación explícita.
