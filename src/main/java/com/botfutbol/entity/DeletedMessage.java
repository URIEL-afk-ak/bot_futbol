package com.botfutbol.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad para rastrear mensajes eliminados por usuarios específicos (soft delete por usuario).
 */
@Entity
@Table(name = "deleted_messages", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"message_id", "user_id"})
})
public class DeletedMessage {
    
    @Id
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "message_id", nullable = false)
    private GroupMessage message;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "deleted_at", nullable = false)
    private LocalDateTime deletedAt;
    
    public DeletedMessage() {
        this.id = UUID.randomUUID().toString();
        this.deletedAt = LocalDateTime.now();
    }
    
    public DeletedMessage(GroupMessage message, User user) {
        this();
        this.message = message;
        this.user = user;
    }
    
    // Getters y Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public GroupMessage getMessage() {
        return message;
    }
    
    public void setMessage(GroupMessage message) {
        this.message = message;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
    
    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}








