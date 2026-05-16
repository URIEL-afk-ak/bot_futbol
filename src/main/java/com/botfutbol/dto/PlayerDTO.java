package com.botfutbol.dto;

import com.botfutbol.constants.PlayerConstants;
import jakarta.validation.constraints.*;

public class PlayerDTO {

    @NotBlank(message = "El nombre del jugador es obligatorio")
    @Size(max = PlayerConstants.MAX_NAME_LENGTH, message = "El nombre no puede exceder " + PlayerConstants.MAX_NAME_LENGTH + " caracteres")
    private String name;

    @Min(value = PlayerConstants.MIN_SKILL_LEVEL, message = PlayerConstants.ERROR_SKILL_LEVEL_RANGE)
    @Max(value = PlayerConstants.MAX_SKILL_LEVEL, message = PlayerConstants.ERROR_SKILL_LEVEL_RANGE)
    private Double skillLevel;

    @Size(max = PlayerConstants.MAX_POSITION_LENGTH, message = "La posición no puede exceder " + PlayerConstants.MAX_POSITION_LENGTH + " caracteres")
    private String position; // POR, DEF, MED, DEL

    // Atributos extendidos
    private Integer edadMin;
    private Integer edadMax;

    @Min(value = 1, message = "Nivel técnico mínimo: 1")
    @Max(value = 10, message = "Nivel técnico máximo: 10")
    private Integer nivelTecnico;

    @Min(value = 1, message = "Estado físico mínimo: 1")
    @Max(value = 5, message = "Estado físico máximo: 5")
    private Integer estadoFisico;

    private String intensidad; // BAJA, MEDIA, ALTA

    private String piernaHabil; // DERECHA, IZQUIERDA, AMBAS

    @Size(max = 500)
    private String notas;

    @Size(max = 500)
    private String observaciones;

    public PlayerDTO() {}

    public PlayerDTO(String name) { this.name = name; }

    public PlayerDTO(String name, Double skillLevel) {
        this.name = name;
        this.skillLevel = skillLevel;
    }

    public PlayerDTO(String name, Double skillLevel, String position) {
        this.name = name;
        this.skillLevel = skillLevel;
        this.position = position;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getSkillLevel() { return skillLevel; }
    public void setSkillLevel(Double skillLevel) { this.skillLevel = skillLevel; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public Integer getEdadMin() { return edadMin; }
    public void setEdadMin(Integer edadMin) { this.edadMin = edadMin; }

    public Integer getEdadMax() { return edadMax; }
    public void setEdadMax(Integer edadMax) { this.edadMax = edadMax; }

    public Integer getNivelTecnico() { return nivelTecnico; }
    public void setNivelTecnico(Integer nivelTecnico) { this.nivelTecnico = nivelTecnico; }

    public Integer getEstadoFisico() { return estadoFisico; }
    public void setEstadoFisico(Integer estadoFisico) { this.estadoFisico = estadoFisico; }

    public String getIntensidad() { return intensidad; }
    public void setIntensidad(String intensidad) { this.intensidad = intensidad; }

    public String getPiernaHabil() { return piernaHabil; }
    public void setPiernaHabil(String piernaHabil) { this.piernaHabil = piernaHabil; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
