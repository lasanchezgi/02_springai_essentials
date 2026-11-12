package com.eazybytes.springai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * Respuestas en streaming: el texto llega por fragmentos a medida que el modelo lo genera.
 */
@RestController
@RequestMapping("/api")
public class StreamController {

    private final ChatClient chatClient;

    public StreamController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * {@code GET /api/stream?message=...}
     * <p>
     * {@code .stream().content()} devuelve un {@link Flux} (Project Reactor) con los fragmentos de texto.
     * Mejora la experiencia en respuestas largas. Nota: {@code TokenUsageAuditAdvisor} no se ejecuta aquí.
     */
    @GetMapping("/stream")
    public Flux<String> stream(@RequestParam("message") String message) {
        return chatClient.prompt().user(message).stream().content();
    }
}