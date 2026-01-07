package com.botfutbol.dto;

import java.time.LocalDateTime;

/**
 * DTO para transferir información de miembros de grupos.
 */
public class GroupMemberDTO {
    private String id;
    private String groupId;
    private String groupName;
    private Long userId;
    private String userName;
    private String userEmail;
    private String profileImageUrl; // URL de la foto de perfil del usuario
    private String role; // ADMIN o MEMBER
    private LocalDateTime joinedAt;
    
    public GroupMemberDTO() {
    }
    
    public GroupMemberDTO(String id, String groupId, String groupName, Long userId, 
                         String userName, String userEmail, String profileImageUrl, 
                         String role, LocalDateTime joinedAt) {
        this.id = id;
        this.groupId = groupId;
        this.groupName = groupName;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.profileImageUrl = profileImageUrl;
        this.role = role;
        this.joinedAt = joinedAt;
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
    
    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }
    
    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}



