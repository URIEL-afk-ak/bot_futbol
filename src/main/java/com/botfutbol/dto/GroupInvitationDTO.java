package com.botfutbol.dto;

import java.time.LocalDateTime;

/**
 * DTO para transferir información de invitaciones a grupos.
 */
public class GroupInvitationDTO {
    private String id;
    private String groupId;
    private String groupName;
    private String invitationCode;
    private String invitationLink; // URL completa del enlace
    private Long createdByUserId;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Integer maxUses;
    private int currentUses;
    private boolean isActive;
    
    public GroupInvitationDTO() {
    }
    
    public GroupInvitationDTO(String id, String groupId, String groupName, String invitationCode,
                             String invitationLink, Long createdByUserId, LocalDateTime createdAt,
                             LocalDateTime expiresAt, Integer maxUses, int currentUses, boolean isActive) {
        this.id = id;
        this.groupId = groupId;
        this.groupName = groupName;
        this.invitationCode = invitationCode;
        this.invitationLink = invitationLink;
        this.createdByUserId = createdByUserId;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.maxUses = maxUses;
        this.currentUses = currentUses;
        this.isActive = isActive;
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
    
    public String getInvitationCode() {
        return invitationCode;
    }
    
    public void setInvitationCode(String invitationCode) {
        this.invitationCode = invitationCode;
    }
    
    public String getInvitationLink() {
        return invitationLink;
    }
    
    public void setInvitationLink(String invitationLink) {
        this.invitationLink = invitationLink;
    }
    
    public Long getCreatedByUserId() {
        return createdByUserId;
    }
    
    public void setCreatedByUserId(Long createdByUserId) {
        this.createdByUserId = createdByUserId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public Integer getMaxUses() {
        return maxUses;
    }
    
    public void setMaxUses(Integer maxUses) {
        this.maxUses = maxUses;
    }
    
    public int getCurrentUses() {
        return currentUses;
    }
    
    public void setCurrentUses(int currentUses) {
        this.currentUses = currentUses;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
}



