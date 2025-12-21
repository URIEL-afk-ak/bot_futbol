package com.botfutbol.constants;

/**
 * Constantes relacionadas con partidos.
 */
public class MatchConstants {
    
    // Costos
    public static final double DEFAULT_COST_PER_PLAYER = 1500.0;
    
    // Equipos
    public static final int MAX_PLAYERS_PER_TEAM = 11;
    public static final int MIN_PLAYERS_PER_TEAM = 5;
    
    // Nombres de equipos
    public static final String TEAM_A_DEFAULT_NAME = "Equipo A";
    public static final String TEAM_B_DEFAULT_NAME = "Equipo B";
    
    // Mensajes
    public static final String ERROR_NO_ACTIVE_MATCH = "No hay un partido activo";
    public static final String ERROR_MATCH_NOT_FOUND = "Partido no encontrado";
    public static final String ERROR_MATCH_NOT_AUTHORIZED = "Partido no encontrado o no autorizado";
    
    private MatchConstants() {
        // Prevenir instanciación
    }
}

