package com.botfutbol.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad que representa un gol anotado durante un partido.
 * Registra quién anotó, a qué equipo pertenece y cuándo fue el gol.
 */
@Entity
@Table(name = "goals")
public class Goal {
    
    @Id
    private String id;
    
    @Column(name = "player_id", nullable = false)
    private String playerId;
    
    @Column(name = "player_name", nullable = false, length = 100)
    private String playerName;
    
    @Column(name = "team_id", nullable = false)
    private String teamId;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "match_id", nullable = false)
    private String matchId; // Identificador del partido
    
    public Goal() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
    }
    
    public Goal(String playerId, String playerName, String teamId, String matchId) {
        this();
        this.playerId = playerId;
        this.playerName = playerName;
        this.teamId = teamId;
        this.matchId = matchId;
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
    
    public String getTeamId() {
        return teamId;
    }
    
    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getMatchId() {
        return matchId;
    }
    
    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }
    
    @Override
    public String toString() {
        return "Goal{" +
                "id='" + id + '\'' +
                ", playerName='" + playerName + '\'' +
                ", teamId='" + teamId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
