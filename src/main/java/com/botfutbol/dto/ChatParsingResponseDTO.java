package com.botfutbol.dto;

import java.util.List;

/**
 * DTO para la respuesta del procesamiento de chat
 */
public class ChatParsingResponseDTO {
    private int playersConfirmed;
    private int paymentsRegistered;
    private int attendanceMarked; // Nuevo: cantidad de asistencias marcadas masivamente
    private List<String> confirmedPlayers;
    private List<String> paidPlayers;
    private List<String> attendanceMarkedPlayers; // Nuevo: jugadores con asistencia marcada
    private List<String> unrecognizedMessages;
    private List<String> newPlayersAdded;
    
    public ChatParsingResponseDTO() {}
    
    // Getters y Setters
    public int getPlayersConfirmed() {
        return playersConfirmed;
    }
    
    public void setPlayersConfirmed(int playersConfirmed) {
        this.playersConfirmed = playersConfirmed;
    }
    
    public int getPaymentsRegistered() {
        return paymentsRegistered;
    }
    
    public void setPaymentsRegistered(int paymentsRegistered) {
        this.paymentsRegistered = paymentsRegistered;
    }
    
    public int getAttendanceMarked() {
        return attendanceMarked;
    }
    
    public void setAttendanceMarked(int attendanceMarked) {
        this.attendanceMarked = attendanceMarked;
    }
    
    public List<String> getConfirmedPlayers() {
        return confirmedPlayers;
    }
    
    public void setConfirmedPlayers(List<String> confirmedPlayers) {
        this.confirmedPlayers = confirmedPlayers;
    }
    
    public List<String> getPaidPlayers() {
        return paidPlayers;
    }
    
    public void setPaidPlayers(List<String> paidPlayers) {
        this.paidPlayers = paidPlayers;
    }
    
    public List<String> getAttendanceMarkedPlayers() {
        return attendanceMarkedPlayers;
    }
    
    public void setAttendanceMarkedPlayers(List<String> attendanceMarkedPlayers) {
        this.attendanceMarkedPlayers = attendanceMarkedPlayers;
    }
    
    public List<String> getUnrecognizedMessages() {
        return unrecognizedMessages;
    }
    
    public void setUnrecognizedMessages(List<String> unrecognizedMessages) {
        this.unrecognizedMessages = unrecognizedMessages;
    }
    
    public List<String> getNewPlayersAdded() {
        return newPlayersAdded;
    }
    
    public void setNewPlayersAdded(List<String> newPlayersAdded) {
        this.newPlayersAdded = newPlayersAdded;
    }
}
