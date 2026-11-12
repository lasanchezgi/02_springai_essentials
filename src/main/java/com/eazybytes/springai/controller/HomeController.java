package com.eazybytes.springai.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Raíz de la aplicación: lista los endpoints disponibles con un ejemplo de uso,
 * para no ver la Whitelabel Error Page al abrir {@code http://localhost:8080/}.
 */
@RestController
public class HomeController {

    /** {@code GET /}: devuelve el catálogo de endpoints. */
    @GetMapping("/")
    public Map<String, Object> home() {
        return Map.of(
                "app", "Spring AI - Essentials",
                "endpoints", List.of(
                        endpoint("/api/chat", "?message=hola", "Chat básico con system prompt propio (helpdesk de TI)"),
                        endpoint("/api/stream", "?message=cuéntame un chiste", "Respuesta en streaming"),
                        endpoint("/api/email", "?customerName=Ana&customerMessage=Mi pedido llegó roto", "Plantilla de prompt"),
                        endpoint("/api/prompt-stuffing", "?message=hola", "Prompt stuffing + opciones por llamada"),
                        endpoint("/api/chat-bean", "?message=Ciudades de Colombia", "Salida estructurada: objeto"),
                        endpoint("/api/chat-list", "?message=5 frutas", "Salida estructurada: lista"),
                        endpoint("/api/chat-map", "?message=Datos de Francia", "Salida estructurada: mapa"),
                        endpoint("/api/chat-bean-list", "?message=Ciudades de Colombia y Perú", "Salida estructurada: lista de objetos")));
    }

    private Map<String, String> endpoint(String path, String example, String description) {
        return Map.of("method", "GET", "path", path, "example", path + example, "description", description);
    }
}
