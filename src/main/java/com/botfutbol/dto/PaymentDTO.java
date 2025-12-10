package com.botfutbol.dto;

/**
 * DTO para registrar un pago.
 * Transporta información desde el controller al service.
 */
public class PaymentDTO {
    
    private String playerName;
    private double amount;
    private String concept;
    
    public PaymentDTO() {
    }
    
    public PaymentDTO(String playerName, double amount) {
        this.playerName = playerName;
        this.amount = amount;
    }
    
    public PaymentDTO(String playerName, double amount, String concept) {
        this.playerName = playerName;
        this.amount = amount;
        this.concept = concept;
    }
    
    // Getters y Setters
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public String getConcept() {
        return concept;
    }
    
    public void setConcept(String concept) {
        this.concept = concept;
    }
}
