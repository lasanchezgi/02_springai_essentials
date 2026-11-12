# Spring AI – Essentials

Proyecto de aprendizaje (curso de Spring AI) que muestra los fundamentos para integrar un LLM de OpenAI en una aplicación Spring Boot usando `ChatClient`.

- **Stack:** Java 21 · Spring Boot 4.1.0 · Spring AI 2.0.0 · Maven
- **Modelo por defecto:** `gpt-5.4-mini`
- **Paquete base:** `com.eazybytes.springai`

---

## 1. Cómo ejecutarlo

```bash
export OPENAI_API_KEY="sk-..."     # una sola línea, sin espacios ni saltos de línea
mvn spring-boot:run                # desde la MISMA terminal donde hiciste el export
```

La app queda en `http://localhost:8080`. Los tests (`mvn test`) **no** necesitan la clave: usan una falsa definida en `src/test/resources/application.properties`.

---

## 2. Estructura del proyecto

```
src/main
├── java/com/eazybytes/springai
│   ├── SpringAiApplication.java        # Punto de entrada (@SpringBootApplication)
│   ├── config/ChatClientConfig.java    # Bean ChatClient con valores por defecto
│   ├── advisors/TokenUsageAuditAdvisor.java  # Advisor propio: audita tokens
│   ├── controller/                     # Un controller por concepto del curso
│   │   ├── HomeController.java         # GET / → lista de endpoints disponibles
│   │   ├── ChatController.java
│   │   ├── StreamController.java
│   │   ├── PromptTemplateController.java
│   │   ├── PromptStuffingController.java
│   │   └── StructuredOutPutController.java
│   └── model/CountryCities.java        # Record usado para salida estructurada
└── resources
    ├── application.properties
    └── promptTemplates/
        ├── systemPromptTemplate.st
        └── userPromptTemplate.st
```

---

## 3. Conceptos clave y dónde verlos

### 3.1 `ChatClient` y su configuración por defecto (`ChatClientConfig`)

`ChatClient` es la API fluida de Spring AI para hablar con un LLM, independiente del proveedor. Spring Boot autoconfigura un `ChatClient.Builder`; aquí lo usamos para crear **un bean con valores por defecto**:

| Método | Qué hace |
|---|---|
| `defaultOptions(...)` | Modelo y parámetros (aquí `gpt-5.4-mini`, `temperature 0.8`) |
| `defaultAdvisors(...)` | Interceptores aplicados a todas las llamadas |
| `defaultSystem(...)` | Rol/comportamiento del asistente (aquí: asistente de RR. HH.) |
| `defaultUser(...)` | Mensaje de usuario por defecto |

**Idea esencial:** los `default*` son un valor de partida. Cada llamada puede **sobrescribirlos**.

- `ChatController` usa `.system(...)` propio (helpdesk de TI) → reemplaza el de RR. HH.
- Todos los controllers usan `.user(message)` → reemplaza el `defaultUser`.
- `PromptStuffingController` usa `.options(...)` → reemplaza modelo y temperatura (`gpt-5.4-nano`, `0.7`).

### 3.2 Temperatura

Controla la aleatoriedad: bajo (~0–0.3) = respuestas deterministas y consistentes; alto (~0.8–1) = más creativas. Para tareas factuales o de extracción, usa valores bajos.

### 3.3 Prompt de sistema vs. de usuario

- **System:** define quién es el asistente, su alcance y reglas. Es la herramienta principal para **limitar su comportamiento** (p. ej. "si te piden algo fuera de RR. HH., di que no puedes ayudar").
- **User:** la petición concreta de cada llamada.

### 3.4 Advisors (`TokenUsageAuditAdvisor`, `SimpleLoggerAdvisor`)

Un advisor es un **interceptor** (patrón *chain of responsibility*) que envuelve la llamada al modelo: puede inspeccionar/modificar la petición antes y la respuesta después.

- `SimpleLoggerAdvisor` (de Spring AI): registra petición y respuesta. Solo se ve con nivel `DEBUG` (configurado en `application.properties`).
- `TokenUsageAuditAdvisor` (propio): implementa `CallAdvisor`, llama a `callAdvisorChain.nextCall(...)` y luego registra los tokens usados (`Usage`). `getOrder()` define su posición en la cadena (menor número = más cerca de la petición original).

> ✅ `TokenUsageAuditAdvisor` implementa `CallAdvisor` **y** `StreamAdvisor`, así que audita tanto `.call()` como `.stream()`. En streaming los tokens solo se conocen al final: se agregan los fragmentos con `ChatClientMessageAggregator` y se registran al completarse el flujo. Además hay que activar `streamUsage(true)` en las opciones (ver `ChatClientConfig`), o OpenAI no envía el consumo en streaming.

### 3.5 Streaming (`StreamController`)

`.stream().content()` devuelve un `Flux<String>` (Project Reactor) que emite la respuesta **por fragmentos** a medida que el modelo la genera, mejorando la experiencia percibida en respuestas largas. Se usa `.call()` cuando se necesita la respuesta completa de una vez.

### 3.6 Plantillas de prompt (`PromptTemplateController`)

Los prompts largos o reutilizables se sacan a archivos `.st` (sintaxis StringTemplate) en `resources/promptTemplates/`. Los marcadores `{nombre}` se rellenan con `.param("nombre", valor)`:

```java
.user(spec -> spec.text(userPromptTemplate)
        .param("customerName", customerName)
        .param("customerMessage", customerMessage))
```

Ventajas: separar texto de código, versionarlo y reutilizarlo. **Los nombres de los marcadores deben coincidir exactamente con los `param`**.

### 3.7 Prompt stuffing (`PromptStuffingController`)

Técnica de **incluir directamente en el prompt** la información de contexto que el modelo necesita (políticas, FAQ, documentación) para que responda basándose en ella y no en lo que "recuerda". Es la forma más simple de dar conocimiento propio al modelo.

- ✅ Simple, sin infraestructura extra.
- ❌ Consume tokens en cada llamada y está limitada por el tamaño de la ventana de contexto. Cuando el contexto crece, la alternativa es RAG (recuperar solo los fragmentos relevantes).

> 📝 `systemPromptTemplate.st` contiene ahora un texto genérico. Para ver la técnica de verdad, pega ahí información real (p. ej. una política de vacaciones) y pregúntale sobre ella.

### 3.8 Salida estructurada (`StructuredOutPutController`)

Convierte la respuesta del LLM (texto) en objetos Java. Spring AI añade al prompt instrucciones de formato y luego parsea la respuesta:

| Endpoint | Conversión | Tipo resultante |
|---|---|---|
| `/api/chat-bean` | `.entity(CountryCities.class)` | `CountryCities` |
| `/api/chat-list` | `ListOutputConverter` | `List<String>` |
| `/api/chat-map` | `MapOutputConverter` | `Map<String,Object>` |
| `/api/chat-bean-list` | `ParameterizedTypeReference<List<CountryCities>>` | `List<CountryCities>` |

Este controller recibe `ChatClient.Builder` y construye **su propio** cliente: el builder es nuevo en cada inyección, por lo que **no hereda** el system prompt de RR. HH. ni los advisors del bean de `ChatClientConfig`; solo el `SimpleLoggerAdvisor` que se añade aquí.

---

## 4. Endpoints

| Endpoint | Ejemplo | Concepto |
|---|---|---|
| `GET /` | — | Lista los endpoints disponibles |
| `GET /api/chat` | `?message=hola` | Chat básico + system prompt propio |
| `GET /api/stream` | `?message=cuéntame un chiste` | Streaming |
| `GET /api/email` | `?customerName=Ana&customerMessage=Mi pedido llegó roto` | Plantilla de prompt |
| `GET /api/prompt-stuffing` | `?message=hola` | Prompt stuffing + opciones por llamada |
| `GET /api/chat-bean` | `?message=Ciudades de Colombia` | Salida → objeto |
| `GET /api/chat-list` | `?message=5 frutas` | Salida → lista |
| `GET /api/chat-map` | `?message=Datos de Francia` | Salida → mapa |
| `GET /api/chat-bean-list` | `?message=Ciudades de Colombia y Perú` | Salida → lista de objetos |

---

## 5. Configuración (`application.properties`)

```properties
spring.ai.openai.api-key=${OPENAI_API_KEY}              # nunca la clave en el archivo
spring.ai.openai.chat.options.model=gpt-5.4-mini
logging.level.org.springframework.ai.chat.client.advisor=DEBUG
```

`src/test/resources/application.properties` **reemplaza por completo** al principal durante los tests (mismo nombre en el classpath), por eso repite las propiedades necesarias con una clave falsa.

---

## 6. ⭐ Aprendizajes esenciales (para futuros proyectos)

### Spring AI
1. **Un bean `ChatClient` con defaults + sobrescritura por llamada** es el patrón base: comportamiento común centralizado, excepciones puntuales donde hagan falta.
2. **El system prompt es tu mecanismo de control**: define rol, alcance y qué hacer cuando la petición queda fuera de él.
3. **Los advisors son el lugar para lo transversal** (logging, auditoría de tokens, memoria, RAG, seguridad) sin ensuciar los controllers.
4. **`call()` vs `stream()`**: un advisor solo actúa en el modo cuya interfaz implemente (`CallAdvisor` / `StreamAdvisor`); para cubrir ambos hay que implementar las dos. En streaming, además, OpenAI solo envía los tokens si activas `streamUsage(true)`.
5. **Saca los prompts largos a plantillas `.st`**; son más fáciles de mantener y versionar.
6. **Salida estructurada**: pide al framework que convierta a `record`/`List`/`Map` en vez de parsear texto a mano. Los `record` son ideales como modelo.
7. **Vigila los tokens**: cada llamada cuesta. Auditarlos desde el principio evita sorpresas (y el prompt stuffing los aumenta).
8. **El modelo puede no existir en tu cuenta**: si OpenAI devuelve "model not found", cambia el nombre del modelo.

### Spring Boot / Java (errores reales que tuvimos al arreglar este proyecto)
1. **El `package` debe coincidir con la ruta de carpetas**, y los controllers/componentes deben estar **dentro del paquete de la clase `@SpringBootApplication`** (o de lo contrario Spring no los escanea y los endpoints no existen).
2. **Los tests van en `src/test/java/<mismo paquete>`**.
3. **La versión de Java del `pom.xml` debe coincidir con tu JDK** (`java.version`). Pedir Java 25 con un JDK 21 no compila.
4. **Spring Boot no evalúa SpEL (`#{...}`) en `application.properties`**; solo placeholders `${...}`. Para transformar un valor, hazlo en código o en origen.
5. **Los tests necesitan su propia configuración** cuando el contexto depende de secretos externos.

### Secretos y entorno
1. **Nunca pongas la API key en el código, en `application.properties` ni en un repositorio.** Usa variables de entorno.
2. **Una clave con un salto de línea (`\n`) al final** rompe el header HTTP: `Unexpected char 0x0a in Authorization value`. Revisa con `echo -n "$OPENAI_API_KEY" | wc -c`.
3. **Una variable de entorno solo existe en el proceso donde se definió** (y sus hijos). El botón ▶ de IntelliJ usa su propia configuración, no la de tu terminal. Tras cambiarla, **reinicia la app**.
4. **Si una clave se expone** (captura, chat, commit), revócala y genera otra.

### Método de depuración que funcionó
Leer el **mensaje exacto** del error (`0x0a at 155`) → deducir la causa (salto de línea) → comprobar que el entorno realmente ve lo que crees (`wc -c`, `od -c`) en vez de suponerlo.

---

## 7. Ideas para seguir practicando

- Añadir memoria de conversación (`ChatMemory`) para que el asistente recuerde mensajes previos.
- Pasar de prompt stuffing a RAG con un `VectorStore` y embeddings.
- Añadir *tool calling* para que el modelo invoque métodos Java.
- Manejo de errores con `@ControllerAdvice` para devolver respuestas limpias en vez del *Whitelabel Error Page*.
