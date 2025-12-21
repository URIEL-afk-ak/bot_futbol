package com.botfutbol.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Propiedades de configuración de la aplicación Bot Futbol.
 * Centraliza la configuración en lugar de usar valores hardcodeados.
 */
@Configuration
@ConfigurationProperties(prefix = "bot")
public class BotProperties {
    
    /**
     * Costo por defecto por jugador por partido.
     */
    private double defaultCostPerPlayer = 1500.0;
    
    /**
     * Número máximo de jugadores por equipo.
     */
    private int teamMaxPlayers = 11;
    
    /**
     * Número mínimo de jugadores por equipo.
     */
    private int teamMinPlayers = 5;
    
    /**
     * Habilitar modo de prueba.
     */
    private boolean testMode = false;
    
    // Getters y Setters
    
    public double getDefaultCostPerPlayer() {
        return defaultCostPerPlayer;
    }
    
    public void setDefaultCostPerPlayer(double defaultCostPerPlayer) {
        this.defaultCostPerPlayer = defaultCostPerPlayer;
    }
    
    public int getTeamMaxPlayers() {
        return teamMaxPlayers;
    }
    
    public void setTeamMaxPlayers(int teamMaxPlayers) {
        this.teamMaxPlayers = teamMaxPlayers;
    }
    
    public int getTeamMinPlayers() {
        return teamMinPlayers;
    }
    
    public void setTeamMinPlayers(int teamMinPlayers) {
        this.teamMinPlayers = teamMinPlayers;
    }
    
    public boolean isTestMode() {
        return testMode;
    }
    
    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }
}

