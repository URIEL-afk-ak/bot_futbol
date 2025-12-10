package com.botfutbol.dto;

/**
 * DTO para crear un nuevo jugador.
 * Transporta datos desde el controller al service.
 */
public class PlayerDTO {
    
    private String name;
    private Integer skillLevel;
    
    public PlayerDTO() {
    }
    
    public PlayerDTO(String name) {
        this.name = name;
    }
    
    public PlayerDTO(String name, Integer skillLevel) {
        this.name = name;
        this.skillLevel = skillLevel;
    }
    
    // Getters y Setters
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getSkillLevel() {
        return skillLevel;
    }
    
    public void setSkillLevel(Integer skillLevel) {
        this.skillLevel = skillLevel;
    }
}
