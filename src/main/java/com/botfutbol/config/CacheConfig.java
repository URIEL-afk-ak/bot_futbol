package com.botfutbol.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuración de caché para optimizar el rendimiento.
 * Usa Caffeine como implementación de caché en memoria.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configura el CacheManager con Caffeine.
     * TTL (Time To Live): 5 minutos para datos que cambian frecuentemente
     * TTL: 30 minutos para datos más estables
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // Configuración de caché para jugadores (5 minutos)
        cacheManager.registerCustomCache("players", Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats()
                .build());
        
        // Configuración de caché para top scorers (10 minutos)
        cacheManager.registerCustomCache("topScorers", Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats()
                .build());
        
        // Configuración de caché para estadísticas (15 minutos)
        cacheManager.registerCustomCache("stats", Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .recordStats()
                .build());
        
        // Configuración de caché para partidos activos (1 minuto - datos muy dinámicos)
        cacheManager.registerCustomCache("activeMatch", Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .recordStats()
                .build());
        
        return cacheManager;
    }
}

