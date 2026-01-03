package com.botfutbol.dto;

import jakarta.validation.constraints.*;

/**
 * DTO para usuarios (registro y actualización).
 */
public class UserDTO {
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;
    
    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede exceder 100 caracteres")
    private String apellido;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    @Size(max = 255, message = "El email no puede exceder 255 caracteres")
    private String email;
    
    // Username es opcional para permitir usuarios antiguos
    @Size(min = 3, message = "El nombre de usuario debe tener al menos 3 caracteres", groups = {})
    @Size(max = 50, message = "El nombre de usuario no puede exceder 50 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "El nombre de usuario solo puede contener letras, números y guiones bajos")
    private String username;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    @Size(max = 100, message = "La contraseña no puede exceder 100 caracteres")
    private String password;

    public UserDTO() {}

    public UserDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public UserDTO(String nombre, String apellido, String email, String username, String password) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.username = username;
        this.password = password;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
