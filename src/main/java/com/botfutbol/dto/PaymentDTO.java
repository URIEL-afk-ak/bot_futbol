package com.botfutbol.dto;

import jakarta.validation.constraints.*;

/**
 * DTO para registrar un pago.
 * Transporta información desde el controller al service.
 */
public class PaymentDTO {
    
    @NotBlank(message = "El nombre del jugador es obligatorio")
    private String playerName;
    
    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a 0")
    private Double amount;
    
    @Size(max = 500, message = "El concepto no puede exceder 500 caracteres")
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
    
    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
    
    public String getConcept() {
        return concept;
    }
    
    public void setConcept(String concept) {
        this.concept = concept;
    }
}
