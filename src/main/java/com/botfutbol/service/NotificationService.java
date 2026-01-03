package com.botfutbol.service;

import com.botfutbol.entity.Notification;
import com.botfutbol.entity.User;
import com.botfutbol.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para manejar notificaciones de usuarios.
 */
@Service
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    // Tipos de notificaciones
    public static final String TYPE_GROUP_INVITATION = "GROUP_INVITATION";
    public static final String TYPE_GROUP_JOINED = "GROUP_JOINED";
    public static final String TYPE_GAME_REMINDER = "GAME_REMINDER";
    public static final String TYPE_GAME_UPDATE = "GAME_UPDATE";
    public static final String TYPE_GAME_STARTING_SOON = "GAME_STARTING_SOON";
    public static final String TYPE_EVENT_CREATED = "EVENT_CREATED";
    public static final String TYPE_ATTENDANCE_REMINDER = "ATTENDANCE_REMINDER";
    
    /**
     * Crea una notificación para un usuario.
     */
    @Transactional
    public Notification createNotification(User user, String title, String message, String type) {
        logger.info("Creando notificación para usuario {}: {}", user.getId(), title);
        Notification notification = new Notification(user, title, message, type);
        return notificationRepository.save(notification);
    }
    
    /**
     * Crea una notificación relacionada con un grupo.
     */
    @Transactional
    public Notification createGroupNotification(User user, String title, String message, String type, String groupId) {
        Notification notification = createNotification(user, title, message, type);
        notification.setRelatedGroupId(groupId);
        notification.setActionUrl("/groups/" + groupId);
        return notificationRepository.save(notification);
    }
    
    /**
     * Crea una notificación relacionada con un evento de juego.
     */
    @Transactional
    public Notification createGameEventNotification(User user, String title, String message, String type, String eventId, String groupId) {
        Notification notification = createNotification(user, title, message, type);
        notification.setRelatedEventId(eventId);
        notification.setRelatedGroupId(groupId);
        notification.setActionUrl("/groups/" + groupId + "/events/" + eventId);
        return notificationRepository.save(notification);
    }
    
    /**
     * Obtiene todas las notificaciones de un usuario.
     */
    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    /**
     * Obtiene las notificaciones no leídas de un usuario.
     */
    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }
    
    /**
     * Cuenta las notificaciones no leídas de un usuario.
     */
    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }
    
    /**
     * Marca una notificación como leída.
     */
    @Transactional
    public void markAsRead(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("Notificación no encontrada"));
        
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("No tienes permiso para marcar esta notificación");
        }
        
        notification.setRead(true);
        notificationRepository.save(notification);
        logger.debug("Notificación {} marcada como leída", notificationId);
    }
    
    /**
     * Marca todas las notificaciones de un usuario como leídas.
     */
    @Transactional
    public int markAllAsRead(User user) {
        int count = notificationRepository.markAllAsRead(user);
        logger.info("Marcadas {} notificaciones como leídas para usuario {}", count, user.getId());
        return count;
    }
    
    /**
     * Elimina una notificación.
     */
    @Transactional
    public void deleteNotification(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("Notificación no encontrada"));
        
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("No tienes permiso para eliminar esta notificación");
        }
        
        notificationRepository.delete(notification);
        logger.debug("Notificación {} eliminada", notificationId);
    }
    
    /**
     * Elimina notificaciones antiguas (más de 30 días).
     */
    @Transactional
    public int cleanupOldNotifications(User user) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        int deleted = notificationRepository.deleteOldNotifications(user, thirtyDaysAgo);
        logger.info("Eliminadas {} notificaciones antiguas para usuario {}", deleted, user.getId());
        return deleted;
    }
}

