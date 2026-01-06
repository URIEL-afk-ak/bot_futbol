package com.botfutbol.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

/**
 * Configuración de zona horaria para la aplicación.
 * Establece la zona horaria por defecto para todos los timestamps.
 */
@Configuration
public class TimeZoneConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(TimeZoneConfig.class);
    
    @Value("${spring.jackson.time-zone:America/Argentina/Buenos_Aires}")
    private String timeZone;
    
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
        logger.info("✅ Zona horaria configurada: {} (GMT{})", 
            timeZone, 
            TimeZone.getDefault().getDisplayName());
        logger.info("Hora actual del sistema: {}", java.time.LocalDateTime.now());
    }
}

