package com.botfutbol.repository;

import com.botfutbol.entity.DeviceToken;
import com.botfutbol.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para administrar tokens de dispositivos FCM.
 */
@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    
    // Busca todos los tokens activos de un usuario
    List<DeviceToken> findByUserAndIsActiveTrue(User user);
    
    // Busca un token específico de un usuario
    Optional<DeviceToken> findByUserAndToken(User user, String token);
    
    // Busca todos los tokens de un usuario (activos e inactivos)
    List<DeviceToken> findByUser(User user);
    
    // Elimina tokens inactivos antiguos (más de 30 días)
    void deleteByIsActiveFalseAndLastUsedAtBefore(java.time.LocalDateTime date);
}

