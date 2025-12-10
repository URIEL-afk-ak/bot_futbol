package com.botfutbol.dto;

/**
 * DTO para mostrar estadísticas de un jugador.
 */
public class PlayerStatsDTO {
    
    private String name;
    private int goalsScored;
    private int gamesPlayed;
    private double average;
    
    public PlayerStatsDTO() {
    }
    
    public PlayerStatsDTO(String name, int goalsScored, int gamesPlayed) {
        this.name = name;
        this.goalsScored = goalsScored;
        this.gamesPlayed = gamesPlayed;
        this.average = gamesPlayed > 0 ? (double) goalsScored / gamesPlayed : 0;
    }
    
    // Getters y Setters
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getGoalsScored() {
        return goalsScored;
    }
    
    public void setGoalsScored(int goalsScored) {
        this.goalsScored = goalsScored;
    }
    
    public int getGamesPlayed() {
        return gamesPlayed;
    }
    
    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }
    
    public double getAverage() {
        return average;
    }
    
    public void setAverage(double average) {
        this.average = average;
    }
}
