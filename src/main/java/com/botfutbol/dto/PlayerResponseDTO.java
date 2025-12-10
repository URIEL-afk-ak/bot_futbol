package com.botfutbol.dto;

/**
 * DTO para enviar información completa de un jugador al frontend.
 */
public class PlayerResponseDTO {
    
    private String name;
    private int skillLevel;
    private int goalsScored;
    private int gamesPlayed;
    private long totalPaid;
    private long totalDebt;
    private boolean attended;
    
    public PlayerResponseDTO() {
    }
    
    public PlayerResponseDTO(String name, int skillLevel, int goalsScored, 
                            int gamesPlayed, long totalPaid, long totalDebt, boolean attended) {
        this.name = name;
        this.skillLevel = skillLevel;
        this.goalsScored = goalsScored;
        this.gamesPlayed = gamesPlayed;
        this.totalPaid = totalPaid;
        this.totalDebt = totalDebt;
        this.attended = attended;
    }
    
    // Getters y Setters
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getSkillLevel() {
        return skillLevel;
    }
    
    public void setSkillLevel(int skillLevel) {
        this.skillLevel = skillLevel;
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
    
    public long getTotalPaid() {
        return totalPaid;
    }
    
    public void setTotalPaid(long totalPaid) {
        this.totalPaid = totalPaid;
    }
    
    public long getTotalDebt() {
        return totalDebt;
    }
    
    public void setTotalDebt(long totalDebt) {
        this.totalDebt = totalDebt;
    }
    
    public boolean isAttended() {
        return attended;
    }
    
    public void setAttended(boolean attended) {
        this.attended = attended;
    }
}
