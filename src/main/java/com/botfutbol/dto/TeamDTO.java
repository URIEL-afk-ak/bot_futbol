package com.botfutbol.dto;

import java.util.List;

/**
 * DTO para devolver información de un equipo.
 * Usado para enviar respuesta al usuario.
 */
public class TeamDTO {
    
    private String id;
    private String name;
    private List<PlayerResponseDTO> players;
    private int totalSkillLevel;
    private int goals;
    
    public TeamDTO() {
    }
    
    public TeamDTO(String id, String name, List<PlayerResponseDTO> players, int totalSkillLevel) {
        this.id = id;
        this.name = name;
        this.players = players;
        this.totalSkillLevel = totalSkillLevel;
    }
    
    // Getters y Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public List<PlayerResponseDTO> getPlayers() {
        return players;
    }
    
    public void setPlayers(List<PlayerResponseDTO> players) {
        this.players = players;
    }
    
    public int getTotalSkillLevel() {
        return totalSkillLevel;
    }
    
    public void setTotalSkillLevel(int totalSkillLevel) {
        this.totalSkillLevel = totalSkillLevel;
    }
    
    public int getGoals() {
        return goals;
    }
    
    public void setGoals(int goals) {
        this.goals = goals;
    }
    
    public List<String> getPlayerNames() {
        return players.stream()
                .map(PlayerResponseDTO::getName)
                .collect(java.util.stream.Collectors.toList());
    }
}
