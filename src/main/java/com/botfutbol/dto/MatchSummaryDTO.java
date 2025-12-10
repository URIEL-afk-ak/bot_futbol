package com.botfutbol.dto;

import java.util.List;

/**
 * DTO para el resumen del partido que se compartirá en WhatsApp
 */
public class MatchSummaryDTO {
    private String matchDate;
    private List<TeamSummaryDTO> teams;
    private PaymentStatusDTO paymentStatus;
    
    public MatchSummaryDTO() {}
    
    public MatchSummaryDTO(String matchDate, List<TeamSummaryDTO> teams, PaymentStatusDTO paymentStatus) {
        this.matchDate = matchDate;
        this.teams = teams;
        this.paymentStatus = paymentStatus;
    }
    
    // Getters y Setters
    public String getMatchDate() {
        return matchDate;
    }
    
    public void setMatchDate(String matchDate) {
        this.matchDate = matchDate;
    }
    
    public List<TeamSummaryDTO> getTeams() {
        return teams;
    }
    
    public void setTeams(List<TeamSummaryDTO> teams) {
        this.teams = teams;
    }
    
    public PaymentStatusDTO getPaymentStatus() {
        return paymentStatus;
    }
    
    public void setPaymentStatus(PaymentStatusDTO paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    /**
     * DTO para información de un equipo en el resumen
     */
    public static class TeamSummaryDTO {
        private String name;
        private List<String> players;
        
        public TeamSummaryDTO() {}
        
        public TeamSummaryDTO(String name, List<String> players) {
            this.name = name;
            this.players = players;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public List<String> getPlayers() {
            return players;
        }
        
        public void setPlayers(List<String> players) {
            this.players = players;
        }
    }
    
    /**
     * DTO para el estado de pagos
     */
    public static class PaymentStatusDTO {
        private List<String> paid;
        private List<String> pending;
        
        public PaymentStatusDTO() {}
        
        public PaymentStatusDTO(List<String> paid, List<String> pending) {
            this.paid = paid;
            this.pending = pending;
        }
        
        public List<String> getPaid() {
            return paid;
        }
        
        public void setPaid(List<String> paid) {
            this.paid = paid;
        }
        
        public List<String> getPending() {
            return pending;
        }
        
        public void setPending(List<String> pending) {
            this.pending = pending;
        }
    }
}
