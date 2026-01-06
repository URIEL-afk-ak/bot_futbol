package com.botfutbol.service;

import com.botfutbol.dto.AttendanceVoteDTO;
import com.botfutbol.dto.GameEventDTO;
import com.botfutbol.entity.AttendanceVote;
import com.botfutbol.entity.GameEvent;
import com.botfutbol.entity.Group;
import com.botfutbol.entity.GroupMember;
import com.botfutbol.entity.PlayerRating;
import com.botfutbol.entity.User;
import com.botfutbol.repository.AttendanceVoteRepository;
import com.botfutbol.repository.EventTeamRepository;
import com.botfutbol.repository.GameEventRepository;
import com.botfutbol.repository.GroupMemberRepository;
import com.botfutbol.repository.GroupRepository;
import com.botfutbol.repository.PlayerRatingRepository;
import com.botfutbol.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para administrar eventos de juego y votaciones de asistencia.
 */
@Service
@Transactional
public class GameEventService {
    
    private static final Logger logger = LoggerFactory.getLogger(GameEventService.class);
    
    private final GameEventRepository gameEventRepository;
    private final AttendanceVoteRepository attendanceVoteRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final GroupService groupService;
    private final PlayerRatingRepository playerRatingRepository;
    private final NotificationService notificationService;
    private final EventTeamRepository eventTeamRepository;
    private final com.botfutbol.service.GroupMessageService groupMessageService;
    
    public GameEventService(GameEventRepository gameEventRepository,
                           AttendanceVoteRepository attendanceVoteRepository,
                           GroupRepository groupRepository,
                           GroupMemberRepository groupMemberRepository,
                           UserRepository userRepository,
                           GroupService groupService,
                           PlayerRatingRepository playerRatingRepository,
                           NotificationService notificationService,
                           EventTeamRepository eventTeamRepository,
                           com.botfutbol.service.GroupMessageService groupMessageService) {
        this.gameEventRepository = gameEventRepository;
        this.attendanceVoteRepository = attendanceVoteRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
        this.groupService = groupService;
        this.playerRatingRepository = playerRatingRepository;
        this.notificationService = notificationService;
        this.eventTeamRepository = eventTeamRepository;
        this.groupMessageService = groupMessageService;
    }
    
    /**
     * Crea un nuevo evento de juego (solo administradores).
     */
    public GameEventDTO createGameEvent(String groupId, LocalDateTime date, String location,
                                       Double costPerPlayer, Integer maxPlayers,
                                       LocalDateTime votingDeadline, Long userId) {
        logger.info("Creando evento de juego para grupo {} por usuario {}", groupId, userId);
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        
        // Verificar que el usuario es administrador del grupo
        if (!groupService.isUserAdminOfGroup(groupId, userId)) {
            throw new IllegalStateException("Solo los administradores pueden crear eventos");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        GameEvent event = new GameEvent(group, date, location, costPerPlayer, maxPlayers, votingDeadline, user);
        event = gameEventRepository.save(event);
        
        // Enviar mensaje automático al chat del grupo
        String eventDateStr = date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String locationStr = location != null && !location.trim().isEmpty() ? " en " + location : "";
        String message = String.format("🏆 Nuevo partido creado\n📅 Fecha: %s%s\n👥 Máximo de jugadores: %s\n💰 Costo por jugador: %s",
                eventDateStr,
                locationStr,
                maxPlayers != null ? maxPlayers.toString() : "Sin límite",
                costPerPlayer != null ? "$" + costPerPlayer : "Gratis");
        try {
            groupMessageService.createSystemMessage(groupId, message);
        } catch (Exception e) {
            logger.warn("Error al enviar mensaje al chat del grupo: {}", e.getMessage());
        }
        
        // Notificar a todos los miembros del grupo sobre el nuevo evento
        List<com.botfutbol.entity.GroupMember> members = groupMemberRepository.findByGroup(group);
        for (com.botfutbol.entity.GroupMember member : members) {
            notificationService.createGameEventNotification(
                member.getUser(),
                "Nuevo partido creado",
                "Se creó un nuevo partido en \"" + group.getName() + "\" para el " + eventDateStr + locationStr,
                NotificationService.TYPE_EVENT_CREATED,
                event.getId(),
                groupId
            );
        }
        
        logger.info("Evento creado exitosamente con ID: {}", event.getId());
        return convertToDTO(event);
    }
    
    /**
     * Registra/finaliza un evento (marca como completado).
     */
    public GameEventDTO registerEvent(String eventId, Long userId) {
        logger.info("Registrando evento {} por usuario {}", eventId, userId);
        
        GameEvent event = gameEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        
        // Verificar que el usuario es administrador del grupo
        if (!groupService.isUserAdminOfGroup(event.getGroup().getId(), userId)) {
            throw new IllegalStateException("Solo los administradores pueden registrar eventos");
        }
        
        // Verificar que el evento esté activo
        if (!event.isActive()) {
            throw new IllegalStateException("El evento ya está finalizado");
        }
        
        // Marcar evento como inactivo (completado)
        event.setActive(false);
        event.setRegisteredAt(java.time.LocalDateTime.now()); // Guardar cuándo se registró
        event = gameEventRepository.save(event);
        
        // Notificar a todos los participantes que asistieron para que califiquen
        List<AttendanceVote> confirmedVotes = attendanceVoteRepository.findByEventAndAttendingTrue(event);
        String eventDateStr = event.getDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        for (AttendanceVote vote : confirmedVotes) {
            notificationService.createGameEventNotification(
                vote.getUser(),
                "Evento finalizado - Califica a los jugadores",
                "El partido del " + eventDateStr + " en \"" + event.getGroup().getName() + "\" ha finalizado. ¡Califica a tus compañeros!",
                NotificationService.TYPE_GAME_UPDATE,
                event.getId(),
                event.getGroup().getId()
            );
        }
        
        logger.info("Evento {} registrado exitosamente. Notificaciones enviadas a {} participantes", 
                   eventId, confirmedVotes.size());
        return convertToDTO(event);
    }
    
    /**
     * Vota asistencia a un evento (sí o no).
     */
    public AttendanceVoteDTO voteAttendance(String eventId, Long userId, boolean attending) {
        logger.info("Usuario {} votando {} para evento {}", userId, attending ? "sí" : "no", eventId);
        
        GameEvent event = gameEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        
        // Verificar que el evento está activo
        if (!event.isActive()) {
            throw new IllegalStateException("El evento no está activo");
        }
        
        // Verificar que no haya pasado la fecha límite de votación
        if (event.getVotingDeadline() != null && LocalDateTime.now().isAfter(event.getVotingDeadline())) {
            throw new IllegalStateException("La fecha límite de votación ha pasado");
        }
        
        // Verificar que el usuario es miembro del grupo
        if (!groupService.isUserMemberOfGroup(event.getGroup().getId(), userId)) {
            throw new IllegalStateException("El usuario no es miembro del grupo");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        // Buscar si ya existe una votación
        AttendanceVote vote = attendanceVoteRepository.findByEventAndUser(event, user)
                .orElse(null);
        
        boolean wasConfirmed = false;
        if (vote != null) {
            // Guardar si estaba confirmado antes
            wasConfirmed = vote.isAttending() && 
                          vote.getAttendanceStatus() == AttendanceVote.AttendanceStatus.CONFIRMED;
            
            // Actualizar votación existente
            vote.setAttending(attending);
            
            if (attending) {
                // Si vota que sí, recalcular su estado (CONFIRMED o SUBSTITUTE)
                recalculateAttendanceStatus(event, vote);
            } else {
                // Si vota que no, limpiar estado y posición
                vote.setAttendanceStatus(null);
                vote.setPosition(null);
            }
            
            vote = attendanceVoteRepository.save(vote);
            logger.info("Votación actualizada para usuario {} en evento {}", userId, eventId);
        } else {
            // Crear nueva votación
            vote = new AttendanceVote(event, user, attending);
            
            if (attending) {
                // Asignar estado según capacidad disponible
                recalculateAttendanceStatus(event, vote);
            }
            
            vote = attendanceVoteRepository.save(vote);
            logger.info("Nueva votación creada para usuario {} en evento {}", userId, eventId);
        }
        
        // Si alguien confirmado se bajó, promover al primer suplente
        if (wasConfirmed && !attending) {
            promoteFirstSubstitute(event);
        }
        
        // Notificar a los miembros del grupo sobre cambios en la asistencia
        notifyAttendanceChange(event, user, vote, wasConfirmed);
        
        return convertVoteToDTO(vote);
    }
    
    /**
     * Notifica a los miembros del grupo sobre cambios en la asistencia de un evento.
     */
    private void notifyAttendanceChange(GameEvent event, User user, AttendanceVote vote, boolean wasConfirmed) {
        try {
            String userName = user.getNombre() + " " + user.getApellido();
            String eventDate = event.getDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            
            // Obtener todos los miembros del grupo
            List<GroupMember> members = groupMemberRepository.findByGroup(event.getGroup());
            
            String title = null;
            String message = null;
            
            if (vote.isAttending()) {
                // Usuario se anotó
                if (vote.getAttendanceStatus() == AttendanceVote.AttendanceStatus.CONFIRMED) {
                    title = "Nuevo confirmado";
                    message = userName + " confirmó asistencia al partido del " + eventDate;
                } else if (vote.getAttendanceStatus() == AttendanceVote.AttendanceStatus.SUBSTITUTE) {
                    title = "Nuevo suplente";
                    message = userName + " se agregó como suplente al partido del " + eventDate;
                }
            } else if (wasConfirmed) {
                // Usuario confirmado se bajó
                title = "Jugador se bajó";
                message = userName + " canceló su asistencia al partido del " + eventDate;
            }
            
            // Enviar notificación a todos los miembros excepto al usuario que hizo el cambio
            if (title != null && message != null) {
                for (GroupMember member : members) {
                    if (!member.getUser().getId().equals(user.getId())) {
                        notificationService.createGameEventNotification(
                            member.getUser(),
                            title,
                            message,
                            NotificationService.TYPE_EVENT_ATTENDANCE,
                            event.getId(),
                            event.getGroup().getId()
                        );
                    }
                }
                logger.debug("Notificaciones de cambio de asistencia enviadas para evento {}", event.getId());
            }
        } catch (Exception e) {
            logger.warn("Error al enviar notificaciones de cambio de asistencia: {}", e.getMessage());
        }
    }
    
    /**
     * Recalcula el estado de asistencia (CONFIRMED o SUBSTITUTE) según la capacidad del evento.
     */
    private void recalculateAttendanceStatus(GameEvent event, AttendanceVote newVote) {
        Integer maxPlayers = event.getMaxPlayers();
        
        // Si no hay límite de capacidad, todos son confirmados
        if (maxPlayers == null || maxPlayers <= 0) {
            newVote.setAttendanceStatus(AttendanceVote.AttendanceStatus.CONFIRMED);
            newVote.setPosition(null);
            return;
        }
        
        // Contar cuántos confirmados hay (excluyendo el voto actual si ya existe)
        List<AttendanceVote> attendingVotes = attendanceVoteRepository.findByEventAndAttendingTrue(event)
                .stream()
                .filter(v -> !v.getId().equals(newVote.getId()))
                .sorted(Comparator.comparing(AttendanceVote::getVotedAt))
                .collect(Collectors.toList());
        
        long confirmedCount = attendingVotes.stream()
                .filter(v -> v.getAttendanceStatus() == AttendanceVote.AttendanceStatus.CONFIRMED)
                .count();
        
        // Si hay cupo disponible, confirmar
        if (confirmedCount < maxPlayers) {
            newVote.setAttendanceStatus(AttendanceVote.AttendanceStatus.CONFIRMED);
            newVote.setPosition(null);
            logger.info("Usuario {} confirmado para evento {} ({}/{})", 
                       newVote.getUser().getId(), event.getId(), confirmedCount + 1, maxPlayers);
        } else {
            // Si no hay cupo, agregar como suplente
            long substituteCount = attendingVotes.stream()
                    .filter(v -> v.getAttendanceStatus() == AttendanceVote.AttendanceStatus.SUBSTITUTE)
                    .count();
            newVote.setAttendanceStatus(AttendanceVote.AttendanceStatus.SUBSTITUTE);
            newVote.setPosition((int)(substituteCount + 1));
            logger.info("Usuario {} agregado como suplente #{} para evento {}", 
                       newVote.getUser().getId(), substituteCount + 1, event.getId());
        }
    }
    
    /**
     * Promueve al primer suplente cuando alguien confirmado se baja.
     */
    private void promoteFirstSubstitute(GameEvent event) {
        // Buscar el primer suplente (por posición)
        List<AttendanceVote> substitutes = attendanceVoteRepository.findByEventAndAttendingTrue(event)
                .stream()
                .filter(v -> v.getAttendanceStatus() == AttendanceVote.AttendanceStatus.SUBSTITUTE)
                .sorted(Comparator.comparing(AttendanceVote::getPosition, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
        
        if (!substitutes.isEmpty()) {
            AttendanceVote firstSubstitute = substitutes.get(0);
            firstSubstitute.setAttendanceStatus(AttendanceVote.AttendanceStatus.CONFIRMED);
            firstSubstitute.setPosition(null);
            attendanceVoteRepository.save(firstSubstitute);
            
            logger.info("Suplente {} promovido a confirmado en evento {}", 
                       firstSubstitute.getUser().getId(), event.getId());
            
            // Reordenar las posiciones de los suplentes restantes
            for (int i = 1; i < substitutes.size(); i++) {
                AttendanceVote sub = substitutes.get(i);
                sub.setPosition(i);
                attendanceVoteRepository.save(sub);
            }
            
            // Notificar al usuario promovido
            notificationService.createGroupNotification(
                firstSubstitute.getUser(),
                "¡Cupo disponible!",
                "Se liberó un cupo en el evento. Ahora estás confirmado.",
                NotificationService.TYPE_EVENT_ATTENDANCE,
                event.getGroup().getId()
            );
        }
    }
    
    /**
     * Cancela la asistencia de un usuario (cambia su voto a "no").
     */
    public AttendanceVoteDTO cancelAttendance(String eventId, Long userId) {
        logger.info("Usuario {} cancelando asistencia al evento {}", userId, eventId);
        return voteAttendance(eventId, userId, false);
    }
    
    /**
     * Obtiene todos los eventos activos de un grupo.
     */
    @Transactional(readOnly = true)
    public List<GameEventDTO> getActiveEventsByGroup(String groupId) {
        logger.debug("Obteniendo eventos activos del grupo {}", groupId);
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        
        List<GameEvent> events = gameEventRepository.findByGroupAndActiveTrueOrderByDateAsc(group);
        return events.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene todos los eventos históricos (completados/inactivos) de un grupo.
     */
    @Transactional(readOnly = true)
    public List<GameEventDTO> getHistoricalEventsByGroup(String groupId) {
        logger.debug("Obteniendo eventos históricos del grupo {}", groupId);
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        
        List<GameEvent> events = gameEventRepository.findByGroupAndActiveFalseOrderByDateDesc(group);
        return events.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene un evento por ID.
     */
    @Transactional(readOnly = true)
    public GameEventDTO getEventById(String eventId) {
        GameEvent event = gameEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        return convertToDTO(event);
    }
    
    /**
     * Cancela un evento (solo administradores).
     */
    public GameEventDTO cancelEvent(String eventId, Long userId) {
        logger.info("Cancelando evento {} por usuario {}", eventId, userId);
        
        GameEvent event = gameEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        
        // Verificar que el usuario es administrador del grupo
        if (!groupService.isUserAdminOfGroup(event.getGroup().getId(), userId)) {
            throw new IllegalStateException("Solo los administradores pueden cancelar eventos");
        }
        
        // Verificar que el evento esté activo
        if (!event.isActive()) {
            throw new IllegalStateException("El evento ya está cancelado o completado");
        }
        
        // Marcar evento como inactivo (cancelado)
        event.setActive(false);
        event = gameEventRepository.save(event);
        
        // Notificar a todos los miembros que confirmaron asistencia
        List<AttendanceVote> confirmedVotes = attendanceVoteRepository.findByEventAndAttendingTrue(event);
        for (AttendanceVote vote : confirmedVotes) {
            notificationService.createGameEventNotification(
                vote.getUser(),
                "Evento cancelado",
                "El partido del " + event.getDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + 
                " en \"" + event.getGroup().getName() + "\" ha sido cancelado",
                NotificationService.TYPE_GAME_UPDATE,
                event.getId(),
                event.getGroup().getId()
            );
        }
        
        logger.info("Evento {} cancelado exitosamente", eventId);
        return convertToDTO(event);
    }
    
    /**
     * Actualiza un evento (solo administradores).
     */
    public GameEventDTO updateEvent(String eventId, LocalDateTime date, String location,
                                    Double costPerPlayer, Integer maxPlayers,
                                    LocalDateTime votingDeadline, Long userId) {
        logger.info("Actualizando evento {} por usuario {}", eventId, userId);
        
        GameEvent event = gameEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        
        // Verificar que el usuario es administrador del grupo
        if (!groupService.isUserAdminOfGroup(event.getGroup().getId(), userId)) {
            throw new IllegalStateException("Solo los administradores pueden actualizar eventos");
        }
        
        // Verificar que el evento esté activo
        if (!event.isActive()) {
            throw new IllegalStateException("No se puede actualizar un evento cancelado o completado");
        }
        
        // Actualizar campos
        if (date != null) {
            event.setDate(date);
        }
        if (location != null) {
            event.setLocation(location);
        }
        if (costPerPlayer != null) {
            event.setCostPerPlayer(costPerPlayer);
        }
        if (maxPlayers != null) {
            event.setMaxPlayers(maxPlayers);
        }
        if (votingDeadline != null) {
            event.setVotingDeadline(votingDeadline);
        }
        
        event = gameEventRepository.save(event);
        
        // Notificar a todos los miembros que confirmaron asistencia
        List<AttendanceVote> confirmedVotes = attendanceVoteRepository.findByEventAndAttendingTrue(event);
        String eventDateStr = event.getDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        for (AttendanceVote vote : confirmedVotes) {
            notificationService.createGameEventNotification(
                vote.getUser(),
                "Evento actualizado",
                "El partido en \"" + event.getGroup().getName() + "\" ha sido actualizado. Nueva fecha: " + eventDateStr,
                NotificationService.TYPE_GAME_UPDATE,
                event.getId(),
                event.getGroup().getId()
            );
        }
        
        logger.info("Evento {} actualizado exitosamente", eventId);
        return convertToDTO(event);
    }
    
    /**
     * Obtiene la lista de usuarios que confirmaron asistencia (votaron "sí").
     */
    @Transactional(readOnly = true)
    public List<AttendanceVoteDTO> getConfirmedAttendees(String eventId) {
        logger.debug("Obteniendo lista de confirmados para evento {}", eventId);
        
        GameEvent event = gameEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        
        List<AttendanceVote> votes = attendanceVoteRepository.findByEventAndAttendingTrue(event);
        return votes.stream()
                .map(this::convertVoteToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene la lista de usuarios que asistieron al evento, excluyendo al usuario actual
     * (para calificación).
     */
    @Transactional(readOnly = true)
    public List<AttendanceVoteDTO> getAttendeesForRating(String eventId, Long currentUserId) {
        logger.debug("Obteniendo lista de asistentes para calificar en evento {} (excluyendo usuario {})", 
                    eventId, currentUserId);
        
        GameEvent event = gameEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        
        List<AttendanceVote> votes = attendanceVoteRepository.findByEventAndAttendingTrue(event);
        return votes.stream()
                .filter(vote -> !vote.getUser().getId().equals(currentUserId)) // Excluir usuario actual
                .map(this::convertVoteToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene las calificaciones que un usuario ya hizo en un evento.
     */
    @Transactional(readOnly = true)
    public List<PlayerRating> getRatingsByUserForEvent(String eventId, Long userId) {
        logger.debug("Obteniendo calificaciones del usuario {} para evento {}", userId, eventId);
        
        GameEvent event = gameEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        return playerRatingRepository.findByEventAndRatedBy(event, user);
    }
    
    /**
     * Obtiene todas las votaciones de un evento.
     */
    @Transactional(readOnly = true)
    public List<AttendanceVoteDTO> getAllVotes(String eventId) {
        logger.debug("Obteniendo todas las votaciones para evento {}", eventId);
        
        GameEvent event = gameEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        
        List<AttendanceVote> votes = attendanceVoteRepository.findByEvent(event);
        return votes.stream()
                .map(this::convertVoteToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Marca que los equipos ya fueron formados para un evento.
     */
    public void markTeamsFormed(String eventId) {
        logger.info("Marcando equipos formados para evento {}", eventId);
        
        GameEvent event = gameEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        
        event.setTeamsFormed(true);
        gameEventRepository.save(event);
    }
    
    /**
     * Guarda los equipos formados para un evento.
     * Los equipos vienen con Player, pero se guardan con User.
     */
    public void saveEventTeams(String eventId, List<com.botfutbol.entity.Team> teams, List<User> confirmedUsers) {
        logger.info("Guardando {} equipos para evento {}", teams.size(), eventId);
        
        GameEvent event = gameEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        
        // Eliminar equipos anteriores si existen
        eventTeamRepository.deleteByEvent(event);
        
        // Guardar nuevos equipos
        for (com.botfutbol.entity.Team team : teams) {
            com.botfutbol.entity.EventTeam eventTeam = new com.botfutbol.entity.EventTeam(
                    event, team.getId(), team.getName());
            
            // Convertir Player a User
            List<User> teamUsers = new ArrayList<>();
            for (com.botfutbol.entity.Player player : team.getPlayers()) {
                if (player.getUser() != null) {
                    teamUsers.add(player.getUser());
                } else {
                    // Si el player no tiene user, buscar por nombre en confirmedUsers
                    String playerName = player.getName();
                    User matchingUser = confirmedUsers.stream()
                            .filter(u -> (u.getNombre() + " " + u.getApellido()).equals(playerName))
                            .findFirst()
                            .orElse(null);
                    if (matchingUser != null) {
                        teamUsers.add(matchingUser);
                    }
                }
            }
            eventTeam.setPlayers(teamUsers);
            
            // Calcular promedio de habilidad
            int avgSkill = team.getPlayers().isEmpty() ? 0 : 
                    (int) Math.round(team.getPlayers().stream()
                            .mapToDouble(com.botfutbol.entity.Player::getSkillLevel)
                            .sum() / team.getPlayers().size());
            eventTeam.setAverageSkill(avgSkill);
            
            eventTeamRepository.save(eventTeam);
        }
        
        // Marcar que los equipos fueron formados
        markTeamsFormed(eventId);
    }
    
    /**
     * Obtiene los equipos formados para un evento.
     */
    @Transactional(readOnly = true)
    public List<com.botfutbol.entity.EventTeam> getEventTeams(String eventId) {
        logger.debug("Obteniendo equipos formados para evento {}", eventId);
        
        GameEvent event = gameEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        
        return eventTeamRepository.findByEventOrderByTeamId(event);
    }
    
    /**
     * Obtiene la entidad GameEvent por ID (para uso interno).
     */
    @Transactional(readOnly = true)
    public GameEvent getEventEntity(String eventId) {
        return gameEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
    }
    
    /**
     * Verifica si se puede calificar jugadores en un evento.
     * Solo se puede calificar durante 2 horas después de que se REGISTRE el evento.
     */
    public boolean canRatePlayers(String eventId) {
        GameEvent event = gameEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        
        // Verificar que el evento está registrado (completado)
        if (event.isActive()) {
            return false;
        }
        
        // Si no tiene fecha de registro, no se puede calificar
        if (event.getRegisteredAt() == null) {
            return false;
        }
        
        // Calcular si han pasado menos de 2 horas desde que se REGISTRÓ el evento
        java.time.LocalDateTime registeredAt = event.getRegisteredAt();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        
        java.time.Duration duration = java.time.Duration.between(registeredAt, now);
        long hoursPassed = duration.toHours();
        
        // Permitir calificar solo durante 2 horas después de que se registró
        // Si han pasado menos de 2 horas, se puede calificar
        return hoursPassed >= 0 && hoursPassed < 2;
    }
    
    /**
     * Califica a un jugador después de un evento (1-10).
     * Solo se puede calificar durante 2 horas después de que termine el evento.
     */
    public PlayerRating ratePlayer(String eventId, Long playerUserId, Long ratedByUserId, double rating, String comment) {
        logger.info("Usuario {} calificando a jugador {} con nota {} en evento {}", 
                   ratedByUserId, playerUserId, rating, eventId);
        
        GameEvent event = gameEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        
        // Verificar que el evento está registrado (completado)
        if (event.isActive()) {
            throw new IllegalStateException("Solo se pueden calificar jugadores en eventos registrados/completados");
        }
        
        // Verificar que no hayan pasado más de 2 horas desde que se registró el evento
        if (!canRatePlayers(eventId)) {
            throw new IllegalStateException("El tiempo para calificar jugadores ha expirado. Solo puedes calificar durante 2 horas después de que se registre el evento.");
        }
        
        // Verificar que el usuario que califica asistió al evento
        User ratedBy = userRepository.findById(ratedByUserId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario que califica no encontrado"));
        
        AttendanceVote vote = attendanceVoteRepository.findByEventAndUser(event, ratedBy)
                .orElse(null);
        
        if (vote == null || !vote.isAttending()) {
            throw new IllegalStateException("Solo los participantes del evento pueden calificar");
        }
        
        User player = userRepository.findById(playerUserId)
                .orElseThrow(() -> new IllegalArgumentException("Jugador a calificar no encontrado"));
        
        // Verificar que el jugador también asistió
        AttendanceVote playerVote = attendanceVoteRepository.findByEventAndUser(event, player)
                .orElse(null);
        
        if (playerVote == null || !playerVote.isAttending()) {
            throw new IllegalStateException("El jugador no asistió al evento");
        }
        
        // No permitir auto-calificación
        if (playerUserId.equals(ratedByUserId)) {
            throw new IllegalStateException("No puedes calificarte a ti mismo");
        }
        
        // Validar rango de calificación
        if (rating < 0 || rating > 10) {
            throw new IllegalArgumentException("La calificación debe estar entre 0 y 10");
        }
        
        // Buscar si ya existe una calificación
        PlayerRating existingRating = playerRatingRepository
                .findByEventAndPlayerAndRatedBy(event, player, ratedBy)
                .orElse(null);
        
        if (existingRating != null) {
            // Actualizar calificación existente
            existingRating.setRating(rating);
            existingRating.setComment(comment);
            existingRating = playerRatingRepository.save(existingRating);
            logger.info("Calificación actualizada para jugador {} en evento {}", playerUserId, eventId);
        } else {
            // Crear nueva calificación
            PlayerRating newRating = new PlayerRating(event, player, ratedBy, rating, comment);
            existingRating = playerRatingRepository.save(newRating);
            logger.info("Nueva calificación creada para jugador {} en evento {}", playerUserId, eventId);
        }
        
        return existingRating;
    }
    
    /**
     * Obtiene todas las calificaciones de un evento.
     */
    @Transactional(readOnly = true)
    public List<PlayerRating> getEventRatings(String eventId) {
        logger.debug("Obteniendo calificaciones del evento {}", eventId);
        
        GameEvent event = gameEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        
        return playerRatingRepository.findByEvent(event);
    }
    
    /**
     * Obtiene el historial de calificaciones de un jugador.
     */
    @Transactional(readOnly = true)
    public List<PlayerRating> getPlayerRatingHistory(Long playerUserId) {
        logger.debug("Obteniendo historial de calificaciones del jugador {}", playerUserId);
        
        User player = userRepository.findById(playerUserId)
                .orElseThrow(() -> new IllegalArgumentException("Jugador no encontrado"));
        
        return playerRatingRepository.findByPlayer(player);
    }
    
    /**
     * Calcula el promedio de calificaciones de un jugador.
     */
    @Transactional(readOnly = true)
    public Double getPlayerAverageRating(Long playerUserId) {
        logger.debug("Calculando promedio de calificaciones del jugador {}", playerUserId);
        
        User player = userRepository.findById(playerUserId)
                .orElseThrow(() -> new IllegalArgumentException("Jugador no encontrado"));
        
        Double average = playerRatingRepository.calculateAverageRatingByPlayer(player);
        return average != null ? average : 0.0;
    }
    
    /**
     * Calcula el promedio de calificaciones de un jugador en un grupo específico.
     */
    @Transactional(readOnly = true)
    public Double getPlayerAverageRatingInGroup(Long playerUserId, String groupId) {
        logger.debug("Calculando promedio de calificaciones del jugador {} en grupo {}", playerUserId, groupId);
        
        User player = userRepository.findById(playerUserId)
                .orElseThrow(() -> new IllegalArgumentException("Jugador no encontrado"));
        
        com.botfutbol.entity.Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        
        Double average = playerRatingRepository.calculateAverageRatingByPlayerAndGroup(player, group);
        return average != null ? average : 0.0;
    }
    
    /**
     * Convierte una entidad GameEvent a DTO.
     */
    private GameEventDTO convertToDTO(GameEvent event) {
        String createdByName = event.getCreatedBy().getNombre() + " " + event.getCreatedBy().getApellido();
        int confirmedCount = (int) attendanceVoteRepository.countByEventAndAttendingTrue(event);
        int totalVotes = attendanceVoteRepository.findByEvent(event).size();
        
        return new GameEventDTO(
                event.getId(),
                event.getGroup().getId(),
                event.getGroup().getName(),
                event.getDate(),
                event.getLocation(),
                event.getCostPerPlayer(),
                event.getMaxPlayers(),
                event.isActive(),
                event.getVotingDeadline(),
                event.isTeamsFormed(),
                event.getCreatedBy().getId(),
                createdByName,
                event.getCreatedAt(),
                confirmedCount,
                totalVotes
        );
    }
    
    /**
     * Convierte una entidad AttendanceVote a DTO.
     */
    private AttendanceVoteDTO convertVoteToDTO(AttendanceVote vote) {
        String userName = vote.getUser().getNombre() + " " + vote.getUser().getApellido();
        
        return new AttendanceVoteDTO(
                vote.getId(),
                vote.getEvent().getId(),
                vote.getUser().getId(),
                userName,
                vote.isAttending(),
                vote.getAttendanceStatus() != null ? vote.getAttendanceStatus().name() : null,
                vote.getPosition(),
                vote.getVotedAt(),
                vote.getUpdatedAt()
        );
    }
}

