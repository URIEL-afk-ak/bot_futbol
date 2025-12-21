package com.botfutbol.dto;

import com.botfutbol.constants.PlayerConstants;
import jakarta.validation.constraints.*;

/**
 * DTO para crear un nuevo jugador.
 * Transporta datos desde el controller al service.
 */
public class PlayerDTO {
    
    @NotBlank(message = "El nombre del jugador es obligatorio")
    @Size(max = PlayerConstants.MAX_NAME_LENGTH, message = "El nombre no puede exceder " + PlayerConstants.MAX_NAME_LENGTH + " caracteres")
    private String name;
    
    @Min(value = PlayerConstants.MIN_SKILL_LEVEL, message = PlayerConstants.ERROR_SKILL_LEVEL_RANGE)
    @Max(value = PlayerConstants.MAX_SKILL_LEVEL, message = PlayerConstants.ERROR_SKILL_LEVEL_RANGE)
    private Integer skillLevel;
    
    @Size(max = PlayerConstants.MAX_POSITION_LENGTH, message = "La posición no puede exceder " + PlayerConstants.MAX_POSITION_LENGTH + " caracteres")
    private String position; // Posición (POR, DEF, MED, DEL)

    public PlayerDTO() {
    }

    public PlayerDTO(String name) {
        this.name = name;
    }

    public PlayerDTO(String name, Integer skillLevel) {
        this.name = name;
        this.skillLevel = skillLevel;
    }

    public PlayerDTO(String name, Integer skillLevel, String position) {
        this.name = name;
        this.skillLevel = skillLevel;
        this.position = position;
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

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}
