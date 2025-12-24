package com.botfutbol.dto;

import java.time.LocalDateTime;

/**
 * DTO para transferir información de eventos de juego.
 */
public class GameEventDTO {
    private String id;
    private String groupId;
    private String groupName;
    private LocalDateTime date;
    private String location;
    private Double costPerPlayer;
    private Integer maxPlayers;
    private boolean active;
    private LocalDateTime votingDeadline;
    private boolean teamsFormed;
    private Long createdByUserId;
    private String createdByName;
    private LocalDateTime createdAt;
    private int confirmedCount; // Cantidad de usuarios que confirmaron asistencia
    private int totalVotes; // Total de votos (sí + no)
    
    public GameEventDTO() {
    }
    
    public GameEventDTO(String id, String groupId, String groupName, LocalDateTime date, 
                       String location, Double costPerPlayer, Integer maxPlayers, 
                       boolean active, LocalDateTime votingDeadline, boolean teamsFormed,
                       Long createdByUserId, String createdByName, LocalDateTime createdAt,
                       int confirmedCount, int totalVotes) {
        this.id = id;
        this.groupId = groupId;
        this.groupName = groupName;
        this.date = date;
        this.location = location;
        this.costPerPlayer = costPerPlayer;
        this.maxPlayers = maxPlayers;
        this.active = active;
        this.votingDeadline = votingDeadline;
        this.teamsFormed = teamsFormed;
        this.createdByUserId = createdByUserId;
        this.createdByName = createdByName;
        this.createdAt = createdAt;
        this.confirmedCount = confirmedCount;
        this.totalVotes = totalVotes;
    }
    
    // Getters y Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getGroupId() {
        return groupId;
    }
    
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    
    public String getGroupName() {
        return groupName;
    }
    
    public void setGroupName(String groupName) {
        this.groupName = groupName;
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
    
    public Long getCreatedByUserId() {
        return createdByUserId;
    }
    
    public void setCreatedByUserId(Long createdByUserId) {
        this.createdByUserId = createdByUserId;
    }
    
    public String getCreatedByName() {
        return createdByName;
    }
    
    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public int getConfirmedCount() {
        return confirmedCount;
    }
    
    public void setConfirmedCount(int confirmedCount) {
        this.confirmedCount = confirmedCount;
    }
    
    public int getTotalVotes() {
        return totalVotes;
    }
    
    public void setTotalVotes(int totalVotes) {
        this.totalVotes = totalVotes;
    }
}

