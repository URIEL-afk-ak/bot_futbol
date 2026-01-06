package com.botfutbol.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad que representa una solicitud de ingreso a un grupo privado.
 * Permite que los administradores aprueben o rechacen solicitudes.
 */
@Entity
@Table(name = "group_join_requests")
public class GroupJoinRequest {
    
    @Id
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.PENDING;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;
    
    @ManyToOne
    @JoinColumn(name = "responded_by_user_id")
    private User respondedBy;
    
    @Column(length = 500)
    private String message; // Mensaje opcional del solicitante
    
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason; // Razón del rechazo (opcional)
    
    public enum RequestStatus {
        PENDING,   // Pendiente de aprobación
        APPROVED,  // Aprobada
        REJECTED   // Rechazada
    }
    
    public GroupJoinRequest() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.status = RequestStatus.PENDING;
    }
    
    public GroupJoinRequest(Group group, User user, String message) {
        this();
        this.group = group;
        this.user = user;
        this.message = message;
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
    
    public RequestStatus getStatus() {
        return status;
    }
    
    public void setStatus(RequestStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }
    
    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }
    
    public User getRespondedBy() {
        return respondedBy;
    }
    
    public void setRespondedBy(User respondedBy) {
        this.respondedBy = respondedBy;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getRejectionReason() {
        return rejectionReason;
    }
    
    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
    
    @Override
    public String toString() {
        return "GroupJoinRequest{" +
                "id='" + id + '\'' +
                ", group=" + (group != null ? group.getId() : null) +
                ", user=" + (user != null ? user.getId() : null) +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}

