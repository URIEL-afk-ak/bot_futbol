package com.botfutbol.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad que representa un evento de juego dentro de un grupo.
 * Un evento es una propuesta de partido con fecha, lugar y costo.
 */
@Entity
@Table(name = "game_events")
public class GameEvent {
    
    @Id
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;
    
    @Column(nullable = false)
    private LocalDateTime date;
    
    @Column(length = 200)
    private String location;
    
    @Column(name = "cost_per_player")
    private Double costPerPlayer;
    
    @Column(name = "max_players")
    private Integer maxPlayers;
    
    @Column(nullable = false)
    private boolean active = true;
    
    @Column(name = "voting_deadline")
    private LocalDateTime votingDeadline; // Fecha límite para votar asistencia
    
    @Column(name = "teams_formed", nullable = false)
    private boolean teamsFormed = false;
    
    @ManyToOne
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    public GameEvent() {
        this.id = UUID.randomUUID().toString();
        this.active = true;
        this.teamsFormed = false;
        this.createdAt = LocalDateTime.now();
    }
    
    public GameEvent(Group group, LocalDateTime date, String location, 
                     Double costPerPlayer, Integer maxPlayers, 
                     LocalDateTime votingDeadline, User createdBy) {
        this();
        this.group = group;
        this.date = date;
        this.location = location;
        this.costPerPlayer = costPerPlayer;
        this.maxPlayers = maxPlayers;
        this.votingDeadline = votingDeadline;
        this.createdBy = createdBy;
    }
    
    // Getters y Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Group getGroup() {
        return group;
    }
    
    public void setGroup(Group group) {
        this.group = group;
    }
    
    public LocalDateTime getDate() {
        return date;
    }
    
    public void setDate(LocalDateTime date) {
        this.date = date;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public Double getCostPerPlayer() {
        return costPerPlayer;
    }
    
    public void setCostPerPlayer(Double costPerPlayer) {
        this.costPerPlayer = costPerPlayer;
    }
    
    public Integer getMaxPlayers() {
        return maxPlayers;
    }
    
    public void setMaxPlayers(Integer maxPlayers) {
        this.maxPlayers = maxPlayers;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public LocalDateTime getVotingDeadline() {
        return votingDeadline;
    }
    
    public void setVotingDeadline(LocalDateTime votingDeadline) {
        this.votingDeadline = votingDeadline;
    }
    
    public boolean isTeamsFormed() {
        return teamsFormed;
    }
    
    public void setTeamsFormed(boolean teamsFormed) {
        this.teamsFormed = teamsFormed;
    }
    
    public User getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "GameEvent{" +
                "id='" + id + '\'' +
                ", group=" + (group != null ? group.getId() : null) +
                ", date=" + date +
                ", location='" + location + '\'' +
                ", costPerPlayer=" + costPerPlayer +
                ", maxPlayers=" + maxPlayers +
                ", active=" + active +
                ", votingDeadline=" + votingDeadline +
                ", teamsFormed=" + teamsFormed +
                '}';
    }
}



