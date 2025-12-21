package com.botfutbol.dto;

/**
 * DTO para información del partido activo.
 * Usado para reconocimiento de voz y registro de goles.
 */
public class ActiveMatchDTO {
    
    private String matchId;
    private String teamAId;
    private String teamBId;
    private String teamAName;
    private String teamBName;
    private int teamAGoals;
    private int teamBGoals;
    private boolean hasActiveMatch;
    
    public ActiveMatchDTO() {
        this.hasActiveMatch = false;
    }
    
    public ActiveMatchDTO(String matchId, String teamAId, String teamBId, 
                         String teamAName, String teamBName, 
                         int teamAGoals, int teamBGoals) {
        this.matchId = matchId;
        this.teamAId = teamAId;
        this.teamBId = teamBId;
        this.teamAName = teamAName;
        this.teamBName = teamBName;
        this.teamAGoals = teamAGoals;
        this.teamBGoals = teamBGoals;
        this.hasActiveMatch = true;
    }
    
    // Getters y Setters
    
    public String getMatchId() {
        return matchId;
    }
    
    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }
    
    public String getTeamAId() {
        return teamAId;
    }
    
    public void setTeamAId(String teamAId) {
        this.teamAId = teamAId;
    }
    
    public String getTeamBId() {
        return teamBId;
    }
    
    public void setTeamBId(String teamBId) {
        this.teamBId = teamBId;
    }
    
    public String getTeamAName() {
        return teamAName;
    }
    
    public void setTeamAName(String teamAName) {
        this.teamAName = teamAName;
    }
    
    public String getTeamBName() {
        return teamBName;
    }
    
    public void setTeamBName(String teamBName) {
        this.teamBName = teamBName;
    }
    
    public int getTeamAGoals() {
        return teamAGoals;
    }
    
    public void setTeamAGoals(int teamAGoals) {
        this.teamAGoals = teamAGoals;
    }
    
    public int getTeamBGoals() {
        return teamBGoals;
    }
    
    public void setTeamBGoals(int teamBGoals) {
        this.teamBGoals = teamBGoals;
    }
    
    public boolean isHasActiveMatch() {
        return hasActiveMatch;
    }
    
    public void setHasActiveMatch(boolean hasActiveMatch) {
        this.hasActiveMatch = hasActiveMatch;
    }
}

