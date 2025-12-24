package com.botfutbol.dto;

import java.time.LocalDateTime;

/**
 * DTO para transferir información de grupos.
 */
public class GroupDTO {
    private String id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private Long createdByUserId;
    private String createdByName;
    private boolean isActive;
    private int memberCount;
    
    public GroupDTO() {
    }
    
    public GroupDTO(String id, String name, String description, LocalDateTime createdAt, 
                   Long createdByUserId, String createdByName, boolean isActive, int memberCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.createdByUserId = createdByUserId;
        this.createdByName = createdByName;
        this.isActive = isActive;
        this.memberCount = memberCount;
    }
    
    // Getters y Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public int getMemberCount() {
        return memberCount;
    }
    
    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }
}

