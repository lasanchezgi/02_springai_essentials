package com.eazybytes.springai.controller;

import com.openai.models.ChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Prompt stuffing: se incluye en el prompt de sistema la información de contexto que el modelo
 * debe usar para responder (políticas, FAQ, etc.), cargada desde {@code systemPromptTemplate.st}.
 * <p>
 * Es simple pero consume tokens en cada llamada y está limitado por la ventana de contexto;
 * para volúmenes grandes de información la alternativa es RAG.
 */
@RestController
@RequestMapping("/api")
public class PromptStuffingController {

    private final ChatClient chatClient;

    public PromptStuffingController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Value("classpath:/promptTemplates/systemPromptTemplate.st")
    Resource systemPromptTemplate;

    /**
     * {@code GET /api/prompt-stuffing?message=...}
     * <p>
     * {@code .options(...)} sobrescribe solo para esta llamada el modelo ({@code gpt-5.4-nano})
     * y la temperatura (0.7) definidos por defecto en {@code ChatClientConfig}.
     */
    @GetMapping("/prompt-stuffing")
    public String promptStuffing(@RequestParam("message") String message) {
        return chatClient
                .prompt()
                .options(OpenAiChatOptions.builder().model(ChatModel.GPT_5_4_NANO.asString())
                        .temperature(0.7))
                .system(systemPromptTemplate)
                .user(message)
                .call().content();
    }

}