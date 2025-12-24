package com.botfutbol.dto;

/**
 * DTO para crear un nuevo grupo.
 */
public class CreateGroupDTO {
    private String name;
    private String description;
    
    public CreateGroupDTO() {
    }
    
    public CreateGroupDTO(String name, String description) {
        this.name = name;
        this.description = description;
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
}

