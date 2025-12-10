package com.botfutbol.dto;

import java.util.List;

/**
 * DTO para devolver estadísticas del sistema.
 * Usado para responder consultas de estadísticas.
 */
public class StatsDTO {
    
    private List<PlayerStatsDTO> topScorers;
    private List<PlayerDebtDTO> debtors;
    private int totalPlayers;
    private int totalGoals;
    private double totalDebt;
    
    public StatsDTO() {
    }
    
    // Getters y Setters
    
    public List<PlayerStatsDTO> getTopScorers() {
        return topScorers;
    }
    
    public void setTopScorers(List<PlayerStatsDTO> topScorers) {
        this.topScorers = topScorers;
    }
    
    public List<PlayerDebtDTO> getDebtors() {
        return debtors;
    }
    
    public void setDebtors(List<PlayerDebtDTO> debtors) {
        this.debtors = debtors;
    }
    
    public int getTotalPlayers() {
        return totalPlayers;
    }
    
    public void setTotalPlayers(int totalPlayers) {
        this.totalPlayers = totalPlayers;
    }
    
    public int getTotalGoals() {
        return totalGoals;
    }
    
    public void setTotalGoals(int totalGoals) {
        this.totalGoals = totalGoals;
    }
    
    public double getTotalDebt() {
        return totalDebt;
    }
    
    public void setTotalDebt(double totalDebt) {
        this.totalDebt = totalDebt;
    }
}
