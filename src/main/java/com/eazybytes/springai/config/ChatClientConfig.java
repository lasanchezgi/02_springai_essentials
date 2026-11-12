package com.eazybytes.springai.config;

import com.eazybytes.springai.advisors.TokenUsageAuditAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración del {@link ChatClient} compartido por la mayoría de controllers.
 * <p>
 * Los valores {@code default*} son un punto de partida: cada llamada puede sobrescribirlos
 * (por ejemplo {@code .system(...)}, {@code .user(...)} u {@code .options(...)}).
 */
@Configuration
public class ChatClientConfig {

    /**
     * Crea el bean {@link ChatClient} a partir del {@link ChatClient.Builder} autoconfigurado por Spring AI.
     * <ul>
     *   <li>Opciones: modelo {@code gpt-5.4-mini} y temperatura 0.8 (más creativa).</li>
     *   <li>Advisors: logger de peticiones/respuestas y auditoría de tokens.</li>
     *   <li>System por defecto: asistente de RR. HH. con alcance limitado.</li>
     * </ul>
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
        var options = OpenAiChatOptions.builder().model("gpt-5.4-mini").temperature(0.8)
                .streamUsage(true);
        return chatClientBuilder
                .defaultOptions(options)
                .defaultAdvisors(List.of(new SimpleLoggerAdvisor(),
                        new TokenUsageAuditAdvisor()))
                .defaultSystem("""
                        You are an internal HR assistant. Your role is to help\s
                        employees with questions related to HR policies, such as\s
                        leave policies, working hours, benefits, and code of conduct.
                        If a user asks for help with anything outside of these topics,\s
                        kindly inform them that you can only assist with queries related to\s
                        HR policies.
                        """)
                .defaultUser("How can you help me ?")
                .build();
    }
}
