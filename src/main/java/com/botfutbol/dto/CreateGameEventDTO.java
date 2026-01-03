package com.botfutbol.dto;

import java.time.LocalDateTime;

/**
 * DTO para crear un nuevo evento de juego.
 */
public class CreateGameEventDTO {
    private String groupId;
    private LocalDateTime date;
    private String location;
    private Double costPerPlayer;
    private Integer maxPlayers;
    private LocalDateTime votingDeadline;
    
    public CreateGameEventDTO() {
    }
    
    public CreateGameEventDTO(String groupId, LocalDateTime date, String location, 
                             Double costPerPlayer, Integer maxPlayers, LocalDateTime votingDeadline) {
        this.groupId = groupId;
        this.date = date;
        this.location = location;
        this.costPerPlayer = costPerPlayer;
        this.maxPlayers = maxPlayers;
        this.votingDeadline = votingDeadline;
    }
    
    // Getters y Setters
    
    public String getGroupId() {
        return groupId;
    }
    
    public void setGroupId(String groupId) {
        this.groupId = groupId;
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
    
    public LocalDateTime getVotingDeadline() {
        return votingDeadline;
    }
    
    public void setVotingDeadline(LocalDateTime votingDeadline) {
        this.votingDeadline = votingDeadline;
    }
}



