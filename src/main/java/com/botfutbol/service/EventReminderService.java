package com.botfutbol.service;

import com.botfutbol.entity.GameEvent;
import com.botfutbol.entity.GroupMember;
import com.botfutbol.repository.GameEventRepository;
import com.botfutbol.repository.GroupMemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para enviar recordatorios automáticos de eventos.
 */
@Service
public class EventReminderService {
    
    private static final Logger logger = LoggerFactory.getLogger(EventReminderService.class);
    
    private final GameEventRepository gameEventRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final NotificationService notificationService;
    private final GroupMessageService groupMessageService;
    
    public EventReminderService(GameEventRepository gameEventRepository,
                               GroupMemberRepository groupMemberRepository,
                               NotificationService notificationService,
                               GroupMessageService groupMessageService) {
        this.gameEventRepository = gameEventRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.notificationService = notificationService;
        this.groupMessageService = groupMessageService;
    }
    
    /**
     * Verifica eventos que ocurren hoy y envía recordatorios.
     * Se ejecuta solo a las 9:00 AM para evitar duplicados.
     */
    @Scheduled(cron = "0 0 9 * * ?") // Solo a las 9:00 AM
    @Transactional
    public void sendDailyReminders() {
        logger.info("🔔 Ejecutando verificación de recordatorios diarios a las 9:00 AM");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = now.withHour(23).withMinute(59).withSecond(59);
        
        // Buscar eventos activos que ocurren hoy
        List<GameEvent> eventsToday = gameEventRepository.findAll().stream()
                .filter(event -> event.isActive())
                .filter(event -> {
                    LocalDateTime eventDate = event.getDate();
                    return eventDate.isAfter(startOfDay) && eventDate.isBefore(endOfDay);
                })
                .collect(java.util.stream.Collectors.toList());
        
        logger.info("📅 Encontrados {} eventos para hoy", eventsToday.size());
        
        for (GameEvent event : eventsToday) {
            try {
                sendEventReminder(event);
            } catch (Exception e) {
                logger.error("❌ Error al enviar recordatorio para evento {}: {}", event.getId(), e.getMessage());
            }
        }
        
        logger.info("✅ Verificación de recordatorios completada. Eventos procesados: {}", eventsToday.size());
    }
    
    /**
     * Envía recordatorios para un evento específico.
     */
    private void sendEventReminder(GameEvent event) {
        String eventDateStr = event.getDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String locationStr = event.getLocation() != null && !event.getLocation().trim().isEmpty() 
                ? " en " + event.getLocation() : "";
        
        // Enviar mensaje al chat del grupo
        String chatMessage = String.format("⏰ Recordatorio: Partido hoy a las %s%s\n¡No olvides confirmar tu asistencia!",
                event.getDate().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
                locationStr);
        
        try {
            groupMessageService.createSystemMessage(event.getGroup().getId(), chatMessage);
        } catch (Exception e) {
            logger.warn("Error al enviar mensaje de recordatorio al chat: {}", e.getMessage());
        }
        
        // Enviar notificaciones a todos los miembros del grupo
        List<GroupMember> members = groupMemberRepository.findByGroup(event.getGroup());
        for (GroupMember member : members) {
            try {
                notificationService.createGameEventNotification(
                    member.getUser(),
                    "Recordatorio: Partido hoy",
                    "El partido en \"" + event.getGroup().getName() + "\" es hoy a las " + 
                    event.getDate().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) + locationStr,
                    NotificationService.TYPE_GAME_REMINDER,
                    event.getId(),
                    event.getGroup().getId()
                );
            } catch (Exception e) {
                logger.warn("Error al enviar notificación a usuario {}: {}", member.getUser().getId(), e.getMessage());
            }
        }
        
        logger.info("Recordatorios enviados para evento {} del grupo {}", event.getId(), event.getGroup().getName());
    }
}

