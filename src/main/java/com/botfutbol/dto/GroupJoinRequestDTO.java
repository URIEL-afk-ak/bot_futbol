package com.botfutbol.dto;

import java.time.LocalDateTime;

/**
 * DTO para transferir información de solicitudes de ingreso a grupos.
 */
public class GroupJoinRequestDTO {
    
    private String id;
    private String groupId;
    private String groupName;
    private String groupPhotoUrl;
    private Long userId;
    private String userName;
    private String userEmail;
    private String userPhotoUrl;
    private String status; // PENDING, APPROVED, REJECTED
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
    private Long respondedByUserId;
    private String respondedByUserName;
    private String message;
    private String rejectionReason;
    
    // Constructor vacío
    public GroupJoinRequestDTO() {
    }
    
    // Constructor completo
    public GroupJoinRequestDTO(String id, String groupId, String groupName, String groupPhotoUrl,
                               Long userId, String userName, String userEmail, String userPhotoUrl,
                               String status, LocalDateTime createdAt, LocalDateTime respondedAt,
                               Long respondedByUserId, String respondedByUserName,
                               String message, String rejectionReason) {
        this.id = id;
        this.groupId = groupId;
        this.groupName = groupName;
        this.groupPhotoUrl = groupPhotoUrl;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPhotoUrl = userPhotoUrl;
        this.status = status;
        this.createdAt = createdAt;
        this.respondedAt = respondedAt;
        this.respondedByUserId = respondedByUserId;
        this.respondedByUserName = respondedByUserName;
        this.message = message;
        this.rejectionReason = rejectionReason;
    }
    
    // Getters y Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getGroupId() {
        return groupId;
    }
    
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    
    public String getGroupName() {
        return groupName;
    }
    
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    
    public String getGroupPhotoUrl() {
        return groupPhotoUrl;
    }
    
    public void setGroupPhotoUrl(String groupPhotoUrl) {
        this.groupPhotoUrl = groupPhotoUrl;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getUserEmail() {
        return userEmail;
    }
    
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    
    public String getUserPhotoUrl() {
        return userPhotoUrl;
    }
    
    public void setUserPhotoUrl(String userPhotoUrl) {
        this.userPhotoUrl = userPhotoUrl;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
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
    
    public Long getRespondedByUserId() {
        return respondedByUserId;
    }
    
    public void setRespondedByUserId(Long respondedByUserId) {
        this.respondedByUserId = respondedByUserId;
    }
    
    public String getRespondedByUserName() {
        return respondedByUserName;
    }
    
    public void setRespondedByUserName(String respondedByUserName) {
        this.respondedByUserName = respondedByUserName;
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
}

