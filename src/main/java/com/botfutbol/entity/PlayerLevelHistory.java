package com.botfutbol.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "player_level_history")
public class PlayerLevelHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String playerName;
    private int previousLevel;
    private int newLevel;
    private LocalDateTime date;

    public PlayerLevelHistory() {}

    public PlayerLevelHistory(String playerName, int previousLevel, int newLevel, LocalDateTime date) {
        this.playerName = playerName;
        this.previousLevel = previousLevel;
        this.newLevel = newLevel;
        this.date = date;
    }

    // Getters y setters...
    public Long getId() { return id; }
    public String getPlayerName() { return playerName; }
    public int getPreviousLevel() { return previousLevel; }
    public int getNewLevel() { return newLevel; }
    public LocalDateTime getDate() { return date; }
    public void setId(Long id) { this.id = id; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public void setPreviousLevel(int previousLevel) { this.previousLevel = previousLevel; }
    public void setNewLevel(int newLevel) { this.newLevel = newLevel; }
    public void setDate(LocalDateTime date) { this.date = date; }
}
