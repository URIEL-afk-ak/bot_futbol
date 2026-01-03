package com.botfutbol.dto;

import java.time.LocalDateTime;

/**
 * DTO para notificaciones.
 */
public class NotificationDTO {
    
    private Long id;
    private String title;
    private String message;
    private String type;
    private boolean isRead;
    private LocalDateTime createdAt;
    private String relatedGroupId; // Cambiado a String para UUIDs
    private String relatedEventId; // Cambiado a String para UUIDs
    private String actionUrl;
    
    public NotificationDTO() {}
    
    public NotificationDTO(Long id, String title, String message, String type, boolean isRead, 
                         LocalDateTime createdAt, String relatedGroupId, String relatedEventId, String actionUrl) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.isRead = isRead;
        this.createdAt = createdAt;
        this.relatedGroupId = relatedGroupId;
        this.relatedEventId = relatedEventId;
        this.actionUrl = actionUrl;
    }
    
    // Getters y Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public boolean isRead() {
        return isRead;
    }
    
    public void setRead(boolean read) {
        isRead = read;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getRelatedGroupId() {
        return relatedGroupId;
    }
    
    public void setRelatedGroupId(String relatedGroupId) {
        this.relatedGroupId = relatedGroupId;
    }
    
    public String getRelatedEventId() {
        return relatedEventId;
    }
    
    public void setRelatedEventId(String relatedEventId) {
        this.relatedEventId = relatedEventId;
    }
    
    public String getActionUrl() {
        return actionUrl;
    }
    
    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }
}

