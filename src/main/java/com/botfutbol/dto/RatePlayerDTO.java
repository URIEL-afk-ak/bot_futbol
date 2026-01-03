package com.botfutbol.dto;

/**
 * DTO para calificar a un jugador.
 * La calificación puede ser decimal (0.0 a 10.0).
 */
public class RatePlayerDTO {
    private String eventId;
    private Long playerUserId;
    private double rating; // 0-10 (puede ser decimal)
    private String comment; // Opcional
    
    public RatePlayerDTO() {
    }
    
    public RatePlayerDTO(String eventId, Long playerUserId, double rating, String comment) {
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
}



