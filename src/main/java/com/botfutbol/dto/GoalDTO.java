package com.botfutbol.dto;

/**
 * DTO para registrar un gol.
 * Transporta información desde el controller al service.
 */
public class GoalDTO {
    
    private String playerName;
    private String teamId;
    
    public GoalDTO() {
    }
    
    public GoalDTO(String playerName, String teamId) {
        this.playerName = playerName;
        this.teamId = teamId;
    }
    
    // Getters y Setters
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    
    public String getTeamId() {
        return teamId;
    }
    
    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }
}
