package com.botfutbol.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que almacena los tokens FCM de los dispositivos de los usuarios.
 */
@Entity
@Table(
    name = "device_tokens",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_user_token",
        columnNames = {"user_id", "token"}
    )
)
public class DeviceToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, length = 500)
    private String token; // Token FCM del dispositivo
    
    @Column(name = "device_type", length = 20)
    private String deviceType; // "ANDROID" o "IOS"
    
    @Column(name = "device_name", length = 100)
    private String deviceName; // Nombre del dispositivo (opcional)
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true; // Si el token está activo
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
    
    public DeviceToken() {
        this.createdAt = LocalDateTime.now();
        this.lastUsedAt = LocalDateTime.now();
        this.isActive = true;
    }
    
    public DeviceToken(User user, String token, String deviceType) {
        this();
        this.user = user;
        this.token = token;
        this.deviceType = deviceType;
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
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getDeviceType() {
        return deviceType;
    }
    
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
    
    public String getDeviceName() {
        return deviceName;
    }
    
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }
    
    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }
}

