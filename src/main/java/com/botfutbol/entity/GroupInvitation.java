package com.botfutbol.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad que representa una invitación a un grupo mediante enlace.
 * Permite que usuarios se unan a grupos usando un código de invitación único.
 */
@Entity
@Table(name = "group_invitations")
public class GroupInvitation {
    
    @Id
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;
    
    @Column(name = "invitation_code", nullable = false, unique = true, length = 50)
    private String invitationCode; // Código único para el enlace
    
    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt; // Opcional: fecha de expiración
    
    @Column(name = "max_uses")
    private Integer maxUses; // Opcional: máximo número de usos
    
    @Column(name = "current_uses", nullable = false)
    private int currentUses = 0;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
    
    public GroupInvitation() {
        this.id = UUID.randomUUID().toString();
        this.invitationCode = generateInvitationCode();
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
        this.currentUses = 0;
    }
    
    public GroupInvitation(Group group, Long createdByUserId) {
        this();
        this.group = group;
        this.createdByUserId = createdByUserId;
    }
    
    public GroupInvitation(Group group, Long createdByUserId, LocalDateTime expiresAt, Integer maxUses) {
        this(group, createdByUserId);
        this.expiresAt = expiresAt;
        this.maxUses = maxUses;
    }
    
    /**
     * Genera un código de invitación único.
     */
    private String generateInvitationCode() {
        // Genera un código alfanumérico de 8 caracteres
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            code.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return code.toString();
    }
    
    /**
     * Verifica si la invitación es válida (activa, no expirada, no excedió usos).
     */
    public boolean isValid() {
        if (!isActive) {
            return false;
        }
        if (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) {
            return false;
        }
        if (maxUses != null && currentUses >= maxUses) {
            return false;
        }
        return true;
    }
    
    /**
     * Incrementa el contador de usos.
     */
    public void incrementUses() {
        this.currentUses++;
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
    
    public String getInvitationCode() {
        return invitationCode;
    }
    
    public void setInvitationCode(String invitationCode) {
        this.invitationCode = invitationCode;
    }
    
    public Long getCreatedByUserId() {
        return createdByUserId;
    }
    
    public void setCreatedByUserId(Long createdByUserId) {
        this.createdByUserId = createdByUserId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public Integer getMaxUses() {
        return maxUses;
    }
    
    public void setMaxUses(Integer maxUses) {
        this.maxUses = maxUses;
    }
    
    public int getCurrentUses() {
        return currentUses;
    }
    
    public void setCurrentUses(int currentUses) {
        this.currentUses = currentUses;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
}



