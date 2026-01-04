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
    
    @Column(name = "is_private", nullable = false, columnDefinition = "boolean default false")
    private boolean isPrivate = false; // false = público, true = privado
    
    @Column(name = "type", length = 50, nullable = true)
    private String type; // "FUTBOL", "BASKET", "VOLEY", etc.
    
    @Column(name = "photo_url", columnDefinition = "TEXT", nullable = true)
    private String photoUrl; // URL de la foto del grupo (puede ser base64 o URL externa)
    
    public Group() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
        this.isPrivate = false;
    }
    
    public Group(String name, String description, User createdBy, boolean isPrivate, String type) {
        this();
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.isPrivate = isPrivate;
        this.type = type;
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
    
    public boolean isPrivate() {
        return isPrivate;
    }
    
    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getPhotoUrl() {
        return photoUrl;
    }
    
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
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
                ", isPrivate=" + isPrivate +
                ", type='" + type + '\'' +
                '}';
    }
}



