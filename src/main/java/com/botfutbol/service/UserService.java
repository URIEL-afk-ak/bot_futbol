package com.botfutbol.service;

import com.botfutbol.entity.User;
import com.botfutbol.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User findByEmail(String email) {
        logger.debug("Buscando usuario por email: {}", email);
        return userRepository.findByEmail(email);
    }
    
    public User findByUsername(String username) {
        logger.debug("Buscando usuario por username: {}", username);
        return userRepository.findByUsername(username);
    }

    public User findById(Long id) {
        logger.debug("Buscando usuario por ID: {}", id);
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            logger.warn("Usuario no encontrado con ID: {}", id);
        }
        return user;
    }

    public void save(User user) {
        logger.info("Guardando usuario: {}", user.getEmail());
        // Si la contraseña no está hasheada (no empieza con $2a$), la hasheamos
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            logger.debug("Contraseña hasheada para usuario: {}", user.getEmail());
        }
        userRepository.save(user);
        logger.info("Usuario guardado exitosamente con ID: {}", user.getId());
    }

    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public User updateProfile(Long userId, String nombre, String apellido, String email, String username, String password, byte[] profileImage) {
        logger.info("Actualizando perfil de usuario: {}", userId);
        User user = findById(userId);
        if (user == null) {
            logger.warn("Intento de actualizar perfil de usuario inexistente: {}", userId);
            throw new IllegalArgumentException("Usuario no encontrado");
        }
        
        // Verificar si el email ya está en uso por otro usuario
        if (email != null && !email.equals(user.getEmail())) {
            User existingUser = findByEmail(email);
            if (existingUser != null && !existingUser.getId().equals(userId)) {
                throw new IllegalArgumentException("El email ya está en uso");
            }
            user.setEmail(email.trim().toLowerCase());
        }
        
        // Verificar si el username ya está en uso por otro usuario
        if (username != null && !username.equals(user.getUsername())) {
            User existingUser = findByUsername(username);
            if (existingUser != null && !existingUser.getId().equals(userId)) {
                throw new IllegalArgumentException("El nombre de usuario ya está en uso");
            }
            user.setUsername(username.trim().toLowerCase());
        }
        
        if (nombre != null && !nombre.trim().isEmpty()) {
            user.setNombre(nombre.trim());
        }
        
        if (apellido != null && !apellido.trim().isEmpty()) {
            user.setApellido(apellido.trim());
        }
        
        if (password != null && !password.trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        }
        
        // Guardar imagen de perfil si se proporciona
        if (profileImage != null && profileImage.length > 0) {
            try {
                // Convertir bytes a Base64 para almacenar en la base de datos
                String base64Image = "data:image/jpeg;base64," + java.util.Base64.getEncoder().encodeToString(profileImage);
                user.setProfileImageUrl(base64Image);
                logger.info("Imagen de perfil actualizada para usuario: {}", userId);
            } catch (Exception e) {
                logger.error("Error al procesar imagen de perfil: {}", e.getMessage());
                throw new IllegalArgumentException("Error al procesar la imagen");
            }
        }
        
        return userRepository.save(user);
    }
}
