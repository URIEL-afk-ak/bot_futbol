package com.botfutbol.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad que representa una calificación de un jugador en un evento.
 * Almacena la nota numérica (0-10, puede ser decimal) que recibió un jugador en un evento específico.
 */
@Entity
@Table(
    name = "player_ratings",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_event_player_rated_by", 
        columnNames = {"event_id", "player_user_id", "rated_by_user_id"}
    )
)
public class PlayerRating {
    
    @Id
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private GameEvent event;
    
    @ManyToOne
    @JoinColumn(name = "player_user_id", nullable = false)
    private User player; // Usuario que fue calificado
    
    @ManyToOne
    @JoinColumn(name = "rated_by_user_id", nullable = false)
    private User ratedBy; // Usuario que calificó
    
    @Column(nullable = false)
    private double rating; // Calificación de 0 a 10 (puede ser decimal)
    
    @Column(length = 500)
    private String comment; // Comentario opcional
    
    @Column(name = "rated_at", nullable = false)
    private LocalDateTime ratedAt;
    
    public PlayerRating() {
        this.id = UUID.randomUUID().toString();
        this.ratedAt = LocalDateTime.now();
    }
    
    public PlayerRating(GameEvent event, User player, User ratedBy, double rating) {
        this();
        this.event = event;
        this.player = player;
        this.ratedBy = ratedBy;
        // Validar rango antes de asignar
        if (rating < 0 || rating > 10) {
            throw new IllegalArgumentException("La calificación debe estar entre 0 y 10");
        }
        this.rating = rating;
    }
    
    public PlayerRating(GameEvent event, User player, User ratedBy, double rating, String comment) {
        this(event, player, ratedBy, rating);
        this.comment = comment;
    }
    
    // Getters y Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public GameEvent getEvent() {
        return event;
    }
    
    public void setEvent(GameEvent event) {
        this.event = event;
    }
    
    public User getPlayer() {
        return player;
    }
    
    public void setPlayer(User player) {
        this.player = player;
    }
    
    public User getRatedBy() {
        return ratedBy;
    }
    
    public void setRatedBy(User ratedBy) {
        this.ratedBy = ratedBy;
    }
    
    public double getRating() {
        return rating;
    }
    
    public void setRating(double rating) {
        if (rating < 0 || rating > 10) {
            throw new IllegalArgumentException("La calificación debe estar entre 0 y 10");
        }
        this.rating = rating;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public LocalDateTime getRatedAt() {
        return ratedAt;
    }
    
    public void setRatedAt(LocalDateTime ratedAt) {
        this.ratedAt = ratedAt;
    }
    
    @Override
    public String toString() {
        return "PlayerRating{" +
                "id='" + id + '\'' +
                ", event=" + (event != null ? event.getId() : null) +
                ", player=" + (player != null ? player.getId() : null) +
                ", ratedBy=" + (ratedBy != null ? ratedBy.getId() : null) +
                ", rating=" + rating +
                ", ratedAt=" + ratedAt +
                '}';
    }
}

