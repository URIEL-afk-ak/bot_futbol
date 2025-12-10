package com.botfutbol.dto;

/**
 * DTO para mostrar deudas de jugadores.
 */
public class PlayerDebtDTO {
    
    private String name;
    private double debt;
    private double paid;
    
    public PlayerDebtDTO() {
    }
    
    public PlayerDebtDTO(String name, double debt, double paid) {
        this.name = name;
        this.debt = debt;
        this.paid = paid;
    }
    
    // Getters y Setters
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public double getDebt() {
        return debt;
    }
    
    public void setDebt(double debt) {
        this.debt = debt;
    }
    
    public double getPaid() {
        return paid;
    }
    
    public void setPaid(double paid) {
        this.paid = paid;
    }
}
