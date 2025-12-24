package com.botfutbol.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad que representa un grupo de fútbol.
 * Los grupos permiten que múltiples usuarios se unan y organicen partidos juntos.
 */
@Entity
@Table(name = "groups")
public class Group {
    
    @Id
    private String id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @ManyToOne
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
    
    public Group() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
    }
    
    public Group(String name, String description, User createdBy) {
        this();
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
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
    
    public User getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    @Override
    public String toString() {
        return "Group{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", createdBy=" + (createdBy != null ? createdBy.getId() : null) +
                ", isActive=" + isActive +
                '}';
    }
}

