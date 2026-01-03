package com.botfutbol.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad que representa la puntuación inicial de un jugador en un grupo.
 * Esta puntuación es asignada por el administrador del grupo y se usa como base
 * para calcular el promedio junto con las calificaciones de los partidos.
 */
@Entity
@Table(
    name = "group_player_initial_ratings",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_group_player_initial_rating", 
        columnNames = {"group_id", "player_user_id"}
    )
)
public class GroupPlayerInitialRating {
    
    @Id
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;
    
    @ManyToOne
    @JoinColumn(name = "player_user_id", nullable = false)
    private User player; // Usuario/jugador
    
    @Column(nullable = false)
    private double initialRating; // Puntuación inicial (puede ser decimal)
    
    @ManyToOne
    @JoinColumn(name = "assigned_by_user_id", nullable = false)
    private User assignedBy; // Usuario que asignó la puntuación (admin)
    
    @Column(length = 500)
    private String comment; // Comentario opcional
    
    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public GroupPlayerInitialRating() {
        this.id = UUID.randomUUID().toString();
        this.assignedAt = LocalDateTime.now();
    }
    
    public GroupPlayerInitialRating(Group group, User player, User assignedBy, double initialRating) {
        this();
        this.group = group;
        this.player = player;
        this.assignedBy = assignedBy;
        if (initialRating < 0 || initialRating > 10) {
            throw new IllegalArgumentException("La puntuación inicial debe estar entre 0 y 10");
        }
        this.initialRating = initialRating;
    }
    
    public GroupPlayerInitialRating(Group group, User player, User assignedBy, double initialRating, String comment) {
        this(group, player, assignedBy, initialRating);
        this.comment = comment;
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
    
    public User getPlayer() {
        return player;
    }
    
    public void setPlayer(User player) {
        this.player = player;
    }
    
    public double getInitialRating() {
        return initialRating;
    }
    
    public void setInitialRating(double initialRating) {
        if (initialRating < 0 || initialRating > 10) {
            throw new IllegalArgumentException("La puntuación inicial debe estar entre 0 y 10");
        }
        this.initialRating = initialRating;
        this.updatedAt = LocalDateTime.now();
    }
    
    public User getAssignedBy() {
        return assignedBy;
    }
    
    public void setAssignedBy(User assignedBy) {
        this.assignedBy = assignedBy;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
        this.updatedAt = LocalDateTime.now();
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

