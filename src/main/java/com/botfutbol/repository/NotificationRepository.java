package com.botfutbol.repository;

import com.botfutbol.entity.Notification;
import com.botfutbol.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * Obtiene todas las notificaciones de un usuario, ordenadas por fecha (más recientes primero)
     */
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    
    /**
     * Obtiene las notificaciones no leídas de un usuario
     */
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);
    
    /**
     * Cuenta las notificaciones no leídas de un usuario
     */
    long countByUserAndIsReadFalse(User user);
    
    /**
     * Marca todas las notificaciones de un usuario como leídas
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user = :user AND n.isRead = false")
    int markAllAsRead(@Param("user") User user);
    
    /**
     * Elimina notificaciones antiguas (más de X días)
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.user = :user AND n.createdAt < :beforeDate")
    int deleteOldNotifications(@Param("user") User user, @Param("beforeDate") java.time.LocalDateTime beforeDate);
}

