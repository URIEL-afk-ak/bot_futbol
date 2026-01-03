package com.botfutbol.dto;

import java.time.LocalDateTime;

/**
 * DTO para transferir información de calificaciones de jugadores.
 */
public class PlayerRatingDTO {
    private String id;
    private String eventId;
    private String eventName;
    private Long playerUserId;
    private String playerName;
    private String playerUsername;
    private Long ratedByUserId;
    private String ratedByName;
    private double rating;
    private String comment;
    private LocalDateTime ratedAt;
    
    public PlayerRatingDTO() {
    }
    
    public PlayerRatingDTO(String id, String eventId, String eventName, Long playerUserId, 
                          String playerName, String playerUsername, Long ratedByUserId, 
                          String ratedByName, double rating, String comment, LocalDateTime ratedAt) {
        this.id = id;
        this.eventId = eventId;
        this.eventName = eventName;
        this.playerUserId = playerUserId;
        this.playerName = playerName;
        this.playerUsername = playerUsername;
        this.ratedByUserId = ratedByUserId;
        this.ratedByName = ratedByName;
        this.rating = rating;
        this.comment = comment;
        this.ratedAt = ratedAt;
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
    
    public String getEventName() {
        return eventName;
    }
    
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
    
    public Long getPlayerUserId() {
        return playerUserId;
    }
    
    public void setPlayerUserId(Long playerUserId) {
        this.playerUserId = playerUserId;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    
    public String getPlayerUsername() {
        return playerUsername;
    }
    
    public void setPlayerUsername(String playerUsername) {
        this.playerUsername = playerUsername;
    }
    
    public Long getRatedByUserId() {
        return ratedByUserId;
    }
    
    public void setRatedByUserId(Long ratedByUserId) {
        this.ratedByUserId = ratedByUserId;
    }
    
    public String getRatedByName() {
        return ratedByName;
    }
    
    public void setRatedByName(String ratedByName) {
        this.ratedByName = ratedByName;
    }
    
    public double getRating() {
        return rating;
    }
    
    public void setRating(double rating) {
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
}



