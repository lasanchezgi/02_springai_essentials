package com.eazybytes.springai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación.
 * <p>
 * {@code @SpringBootApplication} activa la autoconfiguración (incluida la de Spring AI/OpenAI)
 * y el escaneo de componentes del paquete {@code com.eazybytes.springai} y sus subpaquetes.
 * Por eso todos los controllers, configuraciones y advisors deben vivir bajo este paquete.
 */
@SpringBootApplication
public class SpringAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiApplication.class, args);
    }

}