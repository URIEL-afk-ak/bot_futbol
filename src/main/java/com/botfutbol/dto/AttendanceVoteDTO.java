package com.botfutbol.dto;

import java.time.LocalDateTime;

/**
 * DTO para transferir información de votaciones de asistencia.
 */
public class AttendanceVoteDTO {
    private String id;
    private String eventId;
    private Long userId;
    private String userName;
    private boolean attending;
    private LocalDateTime votedAt;
    private LocalDateTime updatedAt;
    
    public AttendanceVoteDTO() {
    }
    
    public AttendanceVoteDTO(String id, String eventId, Long userId, String userName, 
                             boolean attending, LocalDateTime votedAt, LocalDateTime updatedAt) {
        this.id = id;
        this.eventId = eventId;
        this.userId = userId;
        this.userName = userName;
        this.attending = attending;
        this.votedAt = votedAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters y Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getEventId() {
        return eventId;
    }
    
    public void setEventId(String eventId) {
        this.eventId = eventId;
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
    
    public boolean isAttending() {
        return attending;
    }
    
    public void setAttending(boolean attending) {
        this.attending = attending;
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
}

