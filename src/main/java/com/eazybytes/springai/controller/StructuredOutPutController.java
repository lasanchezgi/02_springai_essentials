package com.eazybytes.springai.controller;

import com.eazybytes.springai.model.CountryCities;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Salida estructurada: convierte la respuesta de texto del modelo en objetos Java.
 * <p>
 * Spring AI añade al prompt las instrucciones de formato (p. ej. un esquema JSON) y después
 * parsea la respuesta con un convertidor. Este controller construye su propio cliente desde
 * {@link ChatClient.Builder} (un builder nuevo por inyección), por lo que no hereda el system
 * prompt ni los advisors de {@code ChatClientConfig}.
 */
@RestController
@RequestMapping("/api")
public class StructuredOutPutController {

    private final ChatClient chatClient;

    public StructuredOutPutController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    /** {@code GET /api/chat-bean}: la respuesta se convierte a un único {@link CountryCities}. */
    @GetMapping("/chat-bean")
    public ResponseEntity<CountryCities> chatBean(@RequestParam("message") String message) {
        CountryCities countryCities = chatClient
                .prompt()
                .user(message)
                .call().entity(CountryCities.class);
        return ResponseEntity.ok(countryCities);
    }

    /** {@code GET /api/chat-list}: {@link ListOutputConverter} devuelve una lista de textos. */
    @GetMapping("/chat-list")
    public ResponseEntity<List<String>> chatList(@RequestParam("message") String message) {
        List<String> countryCities = chatClient
                .prompt()
                .user(message)
                .call().entity(new ListOutputConverter());
        return ResponseEntity.ok(countryCities);
    }

    /** {@code GET /api/chat-map}: {@link MapOutputConverter} devuelve un mapa clave/valor. */
    @GetMapping("/chat-map")
    public ResponseEntity<Map<String,Object>> chatMap(@RequestParam("message") String message) {
        Map<String, Object> countryCities = chatClient
                .prompt()
                .user(message)
                .call().entity(new MapOutputConverter());
        return ResponseEntity.ok(countryCities);
    }

    /**
     * {@code GET /api/chat-bean-list}: para tipos genéricos se usa {@link ParameterizedTypeReference},
     * que conserva el tipo {@code List<CountryCities>} que Java borraría en tiempo de ejecución.
     */
    @GetMapping("/chat-bean-list")
    public ResponseEntity<List<CountryCities>> chatBeanList(@RequestParam("message") String message) {
        List<CountryCities> countryCities = chatClient
                .prompt()
                .user(message)
                .call().entity(new ParameterizedTypeReference<List<CountryCities>>() {
                });
        return ResponseEntity.ok(countryCities);
    }

}