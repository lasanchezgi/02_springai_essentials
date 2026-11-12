package com.eazybytes.springai.advisors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

/**
 * Advisor propio que registra en el log los tokens consumidos en cada llamada al modelo.
 * <p>
 * Un advisor es un interceptor en una cadena: recibe la petición, la pasa al siguiente eslabón con
 * {@code nextCall(...)} y puede inspeccionar la respuesta al volver.
 * <p>
 * Implementa {@link CallAdvisor} (para {@code .call()}) y {@link StreamAdvisor} (para {@code .stream()}).
 * En streaming la respuesta llega en fragmentos; los tokens solo se conocen al final, por eso se
 * agregan con {@link ChatClientMessageAggregator} y se registran al completarse el flujo.
 * Requiere {@code streamUsage(true)} en las opciones, o OpenAI no envía el consumo al hacer streaming.
 */
public class TokenUsageAuditAdvisor implements CallAdvisor, StreamAdvisor {

    private static final Logger logger = LoggerFactory.getLogger(TokenUsageAuditAdvisor.class);

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);
        logUsage(chatClientResponse);
        return chatClientResponse;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        Flux<ChatClientResponse> responses = streamAdvisorChain.nextStream(chatClientRequest);
        return new ChatClientMessageAggregator().aggregateChatClientResponse(responses, this::logUsage);
    }

    private void logUsage(ChatClientResponse chatClientResponse) {
        ChatResponse chatResponse = chatClientResponse.chatResponse();
        if (chatResponse != null && chatResponse.getMetadata() != null) {
            Usage usage = chatResponse.getMetadata().getUsage();

            if (usage != null) {
                logger.info("Token usage details : {}", usage);
            }
        }
    }

    @Override
    public String getName() {
        return "TokenUsageAuditAdvisor";
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
