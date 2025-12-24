package com.botfutbol.service;

import com.botfutbol.dto.AttendanceVoteDTO;
import com.botfutbol.dto.GameEventDTO;
import com.botfutbol.entity.AttendanceVote;
import com.botfutbol.entity.GameEvent;
import com.botfutbol.entity.Group;
import com.botfutbol.entity.PlayerRating;
import com.botfutbol.entity.User;
import com.botfutbol.repository.AttendanceVoteRepository;
import com.botfutbol.repository.GameEventRepository;
import com.botfutbol.repository.GroupRepository;
import com.botfutbol.repository.PlayerRatingRepository;
import com.botfutbol.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final UserRepository userRepository;
    private final GroupService groupService;
    private final PlayerRatingRepository playerRatingRepository;
    
    public GameEventService(GameEventRepository gameEventRepository,
                           AttendanceVoteRepository attendanceVoteRepository,
                           GroupRepository groupRepository,
                           UserRepository userRepository,
                           GroupService groupService,
                           PlayerRatingRepository playerRatingRepository) {
        this.gameEventRepository = gameEventRepository;
        this.attendanceVoteRepository = attendanceVoteRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.groupService = groupService;
        this.playerRatingRepository = playerRatingRepository;
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
        
        // Marcar evento como inactivo (completado)
        event.setActive(false);
        event = gameEventRepository.save(event);
        
        logger.info("Evento {} registrado exitosamente", eventId);
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
        
        if (vote != null) {
            // Actualizar votación existente
            vote.setAttending(attending);
            vote = attendanceVoteRepository.save(vote);
            logger.info("Votación actualizada para usuario {} en evento {}", userId, eventId);
        } else {
            // Crear nueva votación
            vote = new AttendanceVote(event, user, attending);
            vote = attendanceVoteRepository.save(vote);
            logger.info("Nueva votación creada para usuario {} en evento {}", userId, eventId);
        }
        
        return convertVoteToDTO(vote);
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
     * Obtiene un evento por ID.
     */
    @Transactional(readOnly = true)
    public GameEventDTO getEventById(String eventId) {
        GameEvent event = gameEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
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
     * Obtiene la entidad GameEvent por ID (para uso interno).
     */
    @Transactional(readOnly = true)
    public GameEvent getEventEntity(String eventId) {
        return gameEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
    }
    
    /**
     * Califica a un jugador después de un evento (1-10).
     */
    public PlayerRating ratePlayer(String eventId, Long playerUserId, Long ratedByUserId, int rating, String comment) {
        logger.info("Usuario {} calificando a jugador {} con nota {} en evento {}", 
                   ratedByUserId, playerUserId, rating, eventId);
        
        GameEvent event = gameEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        
        // Verificar que el evento está registrado (completado)
        if (event.isActive()) {
            throw new IllegalStateException("Solo se pueden calificar jugadores en eventos registrados/completados");
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
        if (rating < 1 || rating > 10) {
            throw new IllegalArgumentException("La calificación debe estar entre 1 y 10");
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
                vote.getVotedAt(),
                vote.getUpdatedAt()
        );
    }
}

