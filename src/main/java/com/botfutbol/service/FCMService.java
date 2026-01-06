package com.botfutbol.service;

import com.botfutbol.entity.DeviceToken;
import com.botfutbol.entity.User;
import com.botfutbol.repository.DeviceTokenRepository;
import com.google.firebase.messaging.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio para enviar notificaciones push mediante Firebase Cloud Messaging.
 */
@Service
public class FCMService {
    
    private static final Logger logger = LoggerFactory.getLogger(FCMService.class);
    
    @Autowired
    private DeviceTokenRepository deviceTokenRepository;
    
    /**
     * Registra un token FCM para un usuario.
     */
    @Transactional
    public void registerToken(User user, String token, String deviceType, String deviceName) {
        logger.info("Registrando token FCM para usuario {}", user.getId());
        
        // Verificar si el token ya existe para este usuario
        Optional<DeviceToken> existingToken = deviceTokenRepository.findByUserAndToken(user, token);
        
        if (existingToken.isPresent()) {
            // Actualizar token existente
            DeviceToken dt = existingToken.get();
            dt.setActive(true);
            dt.setLastUsedAt(LocalDateTime.now());
            dt.setDeviceType(deviceType);
            dt.setDeviceName(deviceName);
            deviceTokenRepository.save(dt);
            logger.info("Token FCM actualizado para usuario {}", user.getId());
        } else {
            // Crear nuevo token
            DeviceToken newToken = new DeviceToken(user, token, deviceType);
            newToken.setDeviceName(deviceName);
            deviceTokenRepository.save(newToken);
            logger.info("Nuevo token FCM registrado para usuario {}", user.getId());
        }
    }
    
    /**
     * Desactiva un token FCM.
     */
    @Transactional
    public void unregisterToken(User user, String token) {
        logger.info("Desactivando token FCM para usuario {}", user.getId());
        
        Optional<DeviceToken> deviceToken = deviceTokenRepository.findByUserAndToken(user, token);
        deviceToken.ifPresent(dt -> {
            dt.setActive(false);
            deviceTokenRepository.save(dt);
        });
    }
    
    /**
     * Envía una notificación push a un usuario.
     */
    public void sendNotificationToUser(User user, String title, String body, String type, String groupId, String eventId) {
        sendNotificationToUser(user, title, body, type, groupId, eventId, null);
    }
    
    /**
     * Envía una notificación push a un usuario con datos adicionales personalizados.
     */
    public void sendNotificationToUser(User user, String title, String body, String type, String groupId, String eventId, Map<String, String> additionalData) {
        List<DeviceToken> tokens = deviceTokenRepository.findByUserAndIsActiveTrue(user);
        
        if (tokens.isEmpty()) {
            logger.debug("No hay tokens FCM activos para usuario {}", user.getId());
            return;
        }
        
        for (DeviceToken deviceToken : tokens) {
            try {
                sendPushNotification(deviceToken.getToken(), title, body, type, groupId, eventId, additionalData);
                
                // Actualizar lastUsedAt
                deviceToken.setLastUsedAt(LocalDateTime.now());
                deviceTokenRepository.save(deviceToken);
            } catch (Exception e) {
                logger.warn("Error al enviar notificación push a token {}: {}", 
                    deviceToken.getId(), e.getMessage());
                
                // Si el token es inválido, desactivarlo
                if (e.getMessage() != null && 
                    (e.getMessage().contains("invalid") || e.getMessage().contains("not-registered"))) {
                    deviceToken.setActive(false);
                    deviceTokenRepository.save(deviceToken);
                }
            }
        }
    }
    
    /**
     * Envía una notificación push a múltiples usuarios.
     */
    public void sendNotificationToUsers(List<User> users, String title, String body, String type, String groupId, String eventId) {
        for (User user : users) {
            sendNotificationToUser(user, title, body, type, groupId, eventId);
        }
    }
    
    /**
     * Envía la notificación push mediante Firebase.
     */
    private void sendPushNotification(String token, String title, String body, String type, String groupId, String eventId) {
        sendPushNotification(token, title, body, type, groupId, eventId, null);
    }
    
    /**
     * Envía la notificación push mediante Firebase con datos adicionales.
     */
    private void sendPushNotification(String token, String title, String body, String type, String groupId, String eventId, Map<String, String> additionalData) {
        try {
            // Construir el mensaje
            Message.Builder messageBuilder = Message.builder()
                .setToken(token)
                .setNotification(
                    com.google.firebase.messaging.Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build()
                );
            
            // Agregar datos personalizados
            if (type != null) {
                messageBuilder.putData("type", type);
            }
            if (groupId != null) {
                messageBuilder.putData("groupId", groupId);
            }
            if (eventId != null) {
                messageBuilder.putData("eventId", eventId);
            }
            if (additionalData != null) {
                for (Map.Entry<String, String> entry : additionalData.entrySet()) {
                    messageBuilder.putData(entry.getKey(), entry.getValue());
                }
            }
            
            // Configuración específica para Android
            messageBuilder.setAndroidConfig(
                AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .setNotification(
                        AndroidNotification.builder()
                            .setSound("default")
                            .setColor("#4CAF50")
                            .build()
                    )
                    .build()
            );
            
            // Configuración específica para iOS
            messageBuilder.setApnsConfig(
                ApnsConfig.builder()
                    .setAps(
                        Aps.builder()
                            .setSound("default")
                            .build()
                    )
                    .build()
            );
            
            // Enviar mensaje
            String response = FirebaseMessaging.getInstance().send(messageBuilder.build());
            logger.debug("Notificación push enviada exitosamente: {}", response);
            
        } catch (FirebaseMessagingException e) {
            logger.error("Error al enviar notificación push: {}", e.getMessage());
            throw new RuntimeException("Error al enviar notificación push", e);
        }
    }
    
    /**
     * Limpia tokens inactivos antiguos (más de 30 días).
     */
    @Transactional
    public void cleanupOldTokens() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        deviceTokenRepository.deleteByIsActiveFalseAndLastUsedAtBefore(thirtyDaysAgo);
        logger.info("Tokens FCM antiguos eliminados");
    }
}

