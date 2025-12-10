package com.botfutbol;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de la aplicación Bot de Fútbol con Spring Boot.
 * Configura y ejecuta la aplicación.
 */
@SpringBootApplication
public class BotFutbolApplication {
    
    /**
     * Método main para ejecutar la aplicación Spring Boot.
     */
    public static void main(String[] args) {
        SpringApplication.run(BotFutbolApplication.class, args);
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║     🤖 BOT DE FÚTBOL INICIADO 🤖      ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println();
        System.out.println("La aplicación está ejecutándose en:");
        System.out.println("http://localhost:8080");
        System.out.println();
        System.out.println("Consola H2 disponible en:");
        System.out.println("http://localhost:8080/h2-console");
    }
}
