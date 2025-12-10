package com.botfutbol.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad que representa un partido.
 * Contiene información sobre los equipos, fecha y estado del partido.
 */
@Entity
@Table(name = "matches")
public class Match {
    
    @Id
    private String id;
    
    @Column(nullable = false)
    private LocalDateTime date;
    
    @Transient
    private Team teamA;
    
    @Transient
    private Team teamB;
    
    @Column(nullable = false)
    private boolean active;
    
    @Column(name = "cost_per_player", nullable = false)
    private double costPerPlayer;
    
    // Guardamos los IDs de los equipos como Strings JSON
    @Column(name = "team_a_data", columnDefinition = "TEXT")
    private String teamAData;
    
    @Column(name = "team_b_data", columnDefinition = "TEXT")
    private String teamBData;
    
    public Match() {
        this.id = UUID.randomUUID().toString();
        this.date = LocalDateTime.now();
        this.active = true;
    }
    
    public Match(Team teamA, Team teamB, double costPerPlayer) {
        this();
        this.teamA = teamA;
        this.teamB = teamB;
        this.costPerPlayer = costPerPlayer;
    }
    
    // Getters y Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public LocalDateTime getDate() {
        return date;
    }
    
    public void setDate(LocalDateTime date) {
        this.date = date;
    }
    
    public Team getTeamA() {
        return teamA;
    }
    
    public void setTeamA(Team teamA) {
        this.teamA = teamA;
    }
    
    public Team getTeamB() {
        return teamB;
    }
    
    public void setTeamB(Team teamB) {
        this.teamB = teamB;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public double getCostPerPlayer() {
        return costPerPlayer;
    }
    
    public void setCostPerPlayer(double costPerPlayer) {
        this.costPerPlayer = costPerPlayer;
    }
    
    @Override
    public String toString() {
        return "Match{" +
                "id='" + id + '\'' +
                ", date=" + date +
                ", teamA=" + teamA.getName() +
                ", teamB=" + teamB.getName() +
                ", active=" + active +
                '}';
    }
}
