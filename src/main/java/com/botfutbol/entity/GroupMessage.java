package com.botfutbol.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad que representa un mensaje en el chat de un grupo.
 */
@Entity
@Table(name = "group_messages")
public class GroupMessage {
    
    @Id
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, length = 2000)
    private String message;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "is_system_message", nullable = false)
    private boolean isSystemMessage = false; // Para mensajes del sistema (ej: "Usuario se unió")
    
    @Column(name = "is_pinned")
    private Boolean isPinned = false; // Mensaje fijado
    
    @Column(name = "pinned_at")
    private LocalDateTime pinnedAt; // Fecha en que se fijó el mensaje
    
    @Column(name = "pinned_until")
    private LocalDateTime pinnedUntil; // Fecha hasta la cual está fijado (null = indefinido)
    
    @Column(name = "is_highlighted")
    private Boolean isHighlighted = false; // Mensaje destacado
    
    @Column(name = "is_deleted")
    private Boolean isDeleted = false; // Mensaje eliminado para todos
    
    @Column(name = "edited_at")
    private LocalDateTime editedAt; // Fecha de última edición
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt; // Fecha de eliminación
    
    @Column(name = "audio_url")
    private String audioUrl; // URL del archivo de audio
    
    @Column(name = "audio_duration")
    private Integer audioDuration; // Duración del audio en segundos
    
    public GroupMessage() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }
    
    public GroupMessage(Group group, User user, String message) {
        this();
        this.group = group;
        this.user = user;
        this.message = message;
    }
    
    public GroupMessage(Group group, User user, String message, boolean isSystemMessage) {
        this(group, user, message);
        this.isSystemMessage = isSystemMessage;
    }
    
    // Getters y Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Group getGroup() {
        return group;
    }
    
    public void setGroup(Group group) {
        this.group = group;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
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
        return isPinned != null ? isPinned : false;
    }
    
    public void setPinned(boolean isPinned) {
        this.isPinned = isPinned;
        if (isPinned && this.pinnedAt == null) {
            this.pinnedAt = LocalDateTime.now();
        } else if (!isPinned) {
            this.pinnedAt = null;
            this.pinnedUntil = null;
        }
    }
    
    public LocalDateTime getPinnedAt() {
        return pinnedAt;
    }
    
    public void setPinnedAt(LocalDateTime pinnedAt) {
        this.pinnedAt = pinnedAt;
    }
    
    public LocalDateTime getPinnedUntil() {
        return pinnedUntil;
    }
    
    public void setPinnedUntil(LocalDateTime pinnedUntil) {
        this.pinnedUntil = pinnedUntil;
    }
    
    public boolean isHighlighted() {
        return isHighlighted != null ? isHighlighted : false;
    }
    
    public void setHighlighted(boolean isHighlighted) {
        this.isHighlighted = isHighlighted;
    }
    
    public boolean isDeleted() {
        return isDeleted != null ? isDeleted : false;
    }
    
    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
        if (isDeleted && this.deletedAt == null) {
            this.deletedAt = LocalDateTime.now();
        }
    }
    
    public LocalDateTime getEditedAt() {
        return editedAt;
    }
    
    public void setEditedAt(LocalDateTime editedAt) {
        this.editedAt = editedAt;
    }
    
    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
    
    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
    
    public String getAudioUrl() {
        return audioUrl;
    }
    
    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }
    
    public Integer getAudioDuration() {
        return audioDuration;
    }
    
    public void setAudioDuration(Integer audioDuration) {
        this.audioDuration = audioDuration;
    }
}
