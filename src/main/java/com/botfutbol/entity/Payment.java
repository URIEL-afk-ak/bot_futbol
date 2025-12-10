package com.botfutbol.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad que representa un pago realizado por un jugador.
 * Registra el monto, la fecha y a qué jugador corresponde.
 */
@Entity
@Table(name = "payments")
public class Payment {
    
    @Id
    private String id;
    
    @Column(name = "player_id", nullable = false)
    private String playerId;
    
    @Column(name = "player_name", nullable = false, length = 100)
    private String playerName;
    
    @Column(nullable = false)
    private double amount;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(length = 200)
    private String concept; // Ej: "Pago partido 15/12", "Adelanto"
    
    public Payment() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
    }
    
    public Payment(String playerId, String playerName, double amount) {
        this();
        this.playerId = playerId;
        this.playerName = playerName;
        this.amount = amount;
    }
    
    public Payment(String playerId, String playerName, double amount, String concept) {
        this(playerId, playerName, amount);
        this.concept = concept;
    }
    
    // Getters y Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
    
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
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getConcept() {
        return concept;
    }
    
    public void setConcept(String concept) {
        this.concept = concept;
    }
    
    @Override
    public String toString() {
        return "Payment{" +
                "id='" + id + '\'' +
                ", playerName='" + playerName + '\'' +
                ", amount=" + amount +
                ", timestamp=" + timestamp +
                ", concept='" + concept + '\'' +
                '}';
    }
}
