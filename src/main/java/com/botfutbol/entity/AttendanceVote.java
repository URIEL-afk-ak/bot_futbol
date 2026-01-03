package com.botfutbol.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad que representa la votación de asistencia de un usuario a un evento.
 * Almacena si el usuario votó "sí" o "no" para asistir al evento.
 */
@Entity
@Table(
    name = "attendance_votes",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_event_user", 
        columnNames = {"event_id", "user_id"}
    )
)
public class AttendanceVote {
    
    @Id
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private GameEvent event;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private boolean attending; // true = sí asistirá, false = no asistirá
    
    @Column(name = "voted_at", nullable = false)
    private LocalDateTime votedAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public AttendanceVote() {
        this.id = UUID.randomUUID().toString();
        this.votedAt = LocalDateTime.now();
    }
    
    public AttendanceVote(GameEvent event, User user, boolean attending) {
        this();
        this.event = event;
        this.user = user;
        this.attending = attending;
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
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public boolean isAttending() {
        return attending;
    }
    
    public void setAttending(boolean attending) {
        this.attending = attending;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getVotedAt() {
        return votedAt;
    }
    
    public void setVotedAt(LocalDateTime votedAt) {
        this.votedAt = votedAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "AttendanceVote{" +
                "id='" + id + '\'' +
                ", event=" + (event != null ? event.getId() : null) +
                ", user=" + (user != null ? user.getId() : null) +
                ", attending=" + attending +
                ", votedAt=" + votedAt +
                '}';
    }
}



