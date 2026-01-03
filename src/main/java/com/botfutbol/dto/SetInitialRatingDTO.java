package com.botfutbol.dto;

/**
 * DTO para asignar o actualizar la puntuación inicial de un jugador.
 */
public class SetInitialRatingDTO {
    private Long playerUserId;
    private double initialRating;
    private String comment;
    
    public SetInitialRatingDTO() {
    }
    
    public SetInitialRatingDTO(Long playerUserId, double initialRating, String comment) {
        this.playerUserId = playerUserId;
        this.initialRating = initialRating;
        this.comment = comment;
    }
    
    // Getters y Setters
    
    public Long getPlayerUserId() {
        return playerUserId;
    }
    
    public void setPlayerUserId(Long playerUserId) {
        this.playerUserId = playerUserId;
    }
    
    public double getInitialRating() {
        return initialRating;
    }
    
    public void setInitialRating(double initialRating) {
        this.initialRating = initialRating;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
}

