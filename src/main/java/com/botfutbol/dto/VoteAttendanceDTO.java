package com.botfutbol.dto;

/**
 * DTO para votar asistencia a un evento.
 */
public class VoteAttendanceDTO {
    private String eventId;
    private boolean attending; // true = sí asistirá, false = no asistirá
    
    public VoteAttendanceDTO() {
    }
    
    public VoteAttendanceDTO(String eventId, boolean attending) {
        this.eventId = eventId;
        this.attending = attending;
    }
    
    public String getEventId() {
        return eventId;
    }
    
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
    
    public boolean isAttending() {
        return attending;
    }
    
    public void setAttending(boolean attending) {
        this.attending = attending;
    }
}

