package com.botfutbol.dto;

import java.time.LocalDateTime;

/**
 * DTO para transferir información de puntuación inicial de jugadores en grupos.
 */
public class GroupPlayerInitialRatingDTO {
    private String id;
    private String groupId;
    private String groupName;
    private Long playerUserId;
    private String playerName;
    private double initialRating;
    private Long assignedByUserId;
    private String assignedByName;
    private String comment;
    private LocalDateTime assignedAt;
    private LocalDateTime updatedAt;
    
    public GroupPlayerInitialRatingDTO() {
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
    
    public Long getPlayerUserId() {
        return playerUserId;
    }
    
    public void setPlayerUserId(Long playerUserId) {
        this.playerUserId = playerUserId;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    
    public double getInitialRating() {
        return initialRating;
    }
    
    public void setInitialRating(double initialRating) {
        this.initialRating = initialRating;
    }
    
    public Long getAssignedByUserId() {
        return assignedByUserId;
    }
    
    public void setAssignedByUserId(Long assignedByUserId) {
        this.assignedByUserId = assignedByUserId;
    }
    
    public String getAssignedByName() {
        return assignedByName;
    }
    
    public void setAssignedByName(String assignedByName) {
        this.assignedByName = assignedByName;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }
    
    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

