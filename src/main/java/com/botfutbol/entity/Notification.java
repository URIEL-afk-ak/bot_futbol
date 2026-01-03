package com.botfutbol.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa una notificación para un usuario.
 */
@Entity
@Table(name = "notifications")
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, length = 100)
    private String title; // Título de la notificación
    
    @Column(nullable = false, length = 500)
    private String message; // Mensaje de la notificación
    
    @Column(nullable = false, length = 50)
    private String type; // Tipo: GROUP_INVITATION, GAME_REMINDER, GAME_UPDATE, etc.
    
    @Column(nullable = false)
    private boolean isRead = false; // Si la notificación fue leída
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    // Referencias opcionales a otras entidades
    @Column(name = "related_group_id", length = 50)
    private String relatedGroupId; // ID del grupo relacionado (si aplica) - UUID como String
    
    @Column(name = "related_event_id", length = 50)
    private String relatedEventId; // ID del evento relacionado (si aplica) - UUID como String
    
    // URL o acción relacionada
    @Column(name = "action_url", length = 255)
    private String actionUrl; // URL para navegar cuando se hace clic
    
    public Notification() {
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }
    
    public Notification(User user, String title, String message, String type) {
        this();
        this.user = user;
        this.title = title;
        this.message = message;
        this.type = type;
    }
    
    // Getters y Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
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

