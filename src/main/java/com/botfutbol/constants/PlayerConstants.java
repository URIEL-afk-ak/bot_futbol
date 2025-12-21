package com.botfutbol.constants;

/**
 * Constantes relacionadas con jugadores.
 * Centraliza valores mágicos para facilitar mantenimiento.
 */
public class PlayerConstants {
    
    // Niveles de habilidad
    public static final int MIN_SKILL_LEVEL = 1;
    public static final int MAX_SKILL_LEVEL = 10;
    public static final int DEFAULT_SKILL_LEVEL = 5;
    
    // Posiciones
    public static final String POSITION_GOALKEEPER = "POR";
    public static final String POSITION_DEFENDER = "DEF";
    public static final String POSITION_MIDFIELDER = "MED";
    public static final String POSITION_FORWARD = "DEL";
    public static final String DEFAULT_POSITION = POSITION_MIDFIELDER;
    
    // Valores por defecto
    public static final double DEFAULT_TOTAL_DEBT = 0.0;
    public static final double DEFAULT_TOTAL_PAID = 0.0;
    public static final int DEFAULT_GAMES_PLAYED = 0;
    public static final int DEFAULT_GOALS_SCORED = 0;
    public static final boolean DEFAULT_ATTENDED = false;
    public static final boolean DEFAULT_ACTIVO = true;
    
    // Longitudes máximas
    public static final int MAX_NAME_LENGTH = 100;
    public static final int MAX_POSITION_LENGTH = 10;
    
    // Mensajes de error
    public static final String ERROR_SKILL_LEVEL_RANGE = 
            "El nivel de habilidad debe estar entre " + MIN_SKILL_LEVEL + " y " + MAX_SKILL_LEVEL;
    public static final String ERROR_PLAYER_NOT_FOUND = "Jugador no encontrado";
    
    private PlayerConstants() {
        // Prevenir instanciación
    }
}

