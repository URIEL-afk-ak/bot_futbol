package com.botfutbol.dto;

/**
 * DTO para calificar a un jugador.
 */
public class RatePlayerDTO {
    private String eventId;
    private Long playerUserId;
    private int rating; // 1-10
    private String comment; // Opcional
    
    public RatePlayerDTO() {
    }
    
    public RatePlayerDTO(String eventId, Long playerUserId, int rating, String comment) {
        this.eventId = eventId;
        this.playerUserId = playerUserId;
        this.rating = rating;
        this.comment = comment;
    }
    
    // Getters y Setters
    
    public String getEventId() {
        return eventId;
    }
    
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
    
    public Long getPlayerUserId() {
        return playerUserId;
    }
    
    public void setPlayerUserId(Long playerUserId) {
        this.playerUserId = playerUserId;
    }
    
    public int getRating() {
        return rating;
    }
    
    public void setRating(int rating) {
        this.rating = rating;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
}

