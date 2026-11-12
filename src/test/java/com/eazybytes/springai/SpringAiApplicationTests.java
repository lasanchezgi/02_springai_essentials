package com.eazybytes.springai;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SpringAiApplicationTests {

    /**
     * Comprueba que el contexto de Spring arranca. No llama a OpenAI: usa la clave falsa de
     * {@code src/test/resources/application.properties}.
     */
    @Test
    void contextLoads() {
    }

}
