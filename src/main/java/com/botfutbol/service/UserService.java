package com.botfutbol.service;

import com.botfutbol.entity.User;
import com.botfutbol.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public void save(User user) {
        // Si la contraseña no está hasheada (no empieza con $2a$), la hasheamos
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userRepository.save(user);
    }

    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public User updateProfile(Long userId, String nombre, String apellido, String email, String password) {
        User user = findById(userId);
        if (user == null) {
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
        
        if (nombre != null && !nombre.trim().isEmpty()) {
            user.setNombre(nombre.trim());
        }
        
        if (apellido != null && !apellido.trim().isEmpty()) {
            user.setApellido(apellido.trim());
        }
        
        if (password != null && !password.trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        }
        
        return userRepository.save(user);
    }
}
