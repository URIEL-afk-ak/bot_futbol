package com.botfutbol.dto;

import java.time.LocalDateTime;

/**
 * DTO para mensajes de grupos.
 */
public class GroupMessageDTO {
    private String id;
    private String groupId;
    private Long userId;
    private String userName;
    private String message;
    private LocalDateTime createdAt;
    private boolean isSystemMessage;
    private boolean isPinned;
    private boolean isHighlighted;
    private boolean isDeleted;
    private LocalDateTime editedAt;
    private boolean isDeletedForMe; // Si el mensaje fue eliminado para el usuario actual
    
    public GroupMessageDTO() {
    }
    
    public GroupMessageDTO(String id, String groupId, Long userId, String userName, 
                          String message, LocalDateTime createdAt, boolean isSystemMessage,
                          boolean isPinned, boolean isHighlighted, boolean isDeleted, 
                          LocalDateTime editedAt, boolean isDeletedForMe) {
        this.id = id;
        this.groupId = groupId;
        this.userId = userId;
        this.userName = userName;
        this.message = message;
        this.createdAt = createdAt;
        this.isSystemMessage = isSystemMessage;
        this.isPinned = isPinned;
        this.isHighlighted = isHighlighted;
        this.isDeleted = isDeleted;
        this.editedAt = editedAt;
        this.isDeletedForMe = isDeletedForMe;
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
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public boolean isSystemMessage() {
        return isSystemMessage;
    }
    
    public void setSystemMessage(boolean isSystemMessage) {
        this.isSystemMessage = isSystemMessage;
    }
    
    public boolean isPinned() {
        return isPinned;
    }
    
    public void setPinned(boolean isPinned) {
        this.isPinned = isPinned;
    }
    
    public boolean isHighlighted() {
        return isHighlighted;
    }
    
    public void setHighlighted(boolean isHighlighted) {
        this.isHighlighted = isHighlighted;
    }
    
    public boolean isDeleted() {
        return isDeleted;
    }
    
    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
    
    public LocalDateTime getEditedAt() {
        return editedAt;
    }
    
    public void setEditedAt(LocalDateTime editedAt) {
        this.editedAt = editedAt;
    }
    
    public boolean isDeletedForMe() {
        return isDeletedForMe;
    }
    
    public void setDeletedForMe(boolean isDeletedForMe) {
        this.isDeletedForMe = isDeletedForMe;
    }
}

