package com.botfutbol.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para actualizar un grupo existente.
 */
public class UpdateGroupDTO {
    private String name;
    private String description;
    private String type; // "FUTBOL", "BASKET", "VOLEY", etc.
    private String photoUrl; // URL de la foto del grupo
    
    @JsonProperty("isPrivate")
    private Boolean isPrivate; // Si el grupo es privado o público
    
    public UpdateGroupDTO() {
    }
    
    public UpdateGroupDTO(String name, String description, String type, String photoUrl, Boolean isPrivate) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.photoUrl = photoUrl;
        this.isPrivate = isPrivate;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getPhotoUrl() {
        return photoUrl;
    }
    
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
    
    public Boolean getIsPrivate() {
        return isPrivate;
    }
    
    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }
}

