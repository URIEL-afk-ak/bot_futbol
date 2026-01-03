package com.botfutbol.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad que representa un equipo formado para un evento.
 * Guarda los equipos formados para poder mostrarlos después.
 */
@Entity
@Table(name = "event_teams")
public class EventTeam {
    
    @Id
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private GameEvent event;
    
    @Column(name = "team_id", nullable = false, length = 10)
    private String teamId; // "A" o "B"
    
    @Column(name = "team_name", nullable = false, length = 50)
    private String teamName; // "Equipo A", "Equipo B"
    
    @ManyToMany
    @JoinTable(
        name = "event_team_players",
        joinColumns = @JoinColumn(name = "event_team_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> players = new ArrayList<>();
    
    @Column(name = "average_skill")
    private Integer averageSkill; // Promedio de habilidad del equipo
    
    public EventTeam() {
        this.id = UUID.randomUUID().toString();
    }
    
    public EventTeam(GameEvent event, String teamId, String teamName) {
        this();
        this.event = event;
        this.teamId = teamId;
        this.teamName = teamName;
    }
    
    // Getters y Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public GameEvent getEvent() {
        return event;
    }
    
    public void setEvent(GameEvent event) {
        this.event = event;
    }
    
    public String getTeamId() {
        return teamId;
    }
    
    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }
    
    public String getTeamName() {
        return teamName;
    }
    
    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }
    
    public List<User> getPlayers() {
        return players;
    }
    
    public void setPlayers(List<User> players) {
        this.players = players;
    }
    
    public Integer getAverageSkill() {
        return averageSkill;
    }
    
    public void setAverageSkill(Integer averageSkill) {
        this.averageSkill = averageSkill;
    }
}

