package com.botfutbol.service;

import com.botfutbol.dto.GroupPollDTO;
import com.botfutbol.entity.Group;
import com.botfutbol.entity.GroupPoll;
import com.botfutbol.entity.PollVote;
import com.botfutbol.entity.User;
import com.botfutbol.repository.GroupPollRepository;
import com.botfutbol.repository.GroupMemberRepository;
import com.botfutbol.repository.GroupRepository;
import com.botfutbol.repository.PollVoteRepository;
import com.botfutbol.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class GroupPollService {
    
    private static final Logger logger = LoggerFactory.getLogger(GroupPollService.class);
    
    private final GroupPollRepository groupPollRepository;
    private final PollVoteRepository pollVoteRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupMessageService groupMessageService;
    private final GameEventService gameEventService;
    
    public GroupPollService(GroupPollRepository groupPollRepository,
                            PollVoteRepository pollVoteRepository,
                            GroupRepository groupRepository,
                            UserRepository userRepository,
                            GroupMemberRepository groupMemberRepository,
                            GroupMessageService groupMessageService,
                            GameEventService gameEventService) {
        this.groupPollRepository = groupPollRepository;
        this.pollVoteRepository = pollVoteRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupMessageService = groupMessageService;
        this.gameEventService = gameEventService;
    }
    
    /**
     * Crea una nueva encuesta en un grupo.
     */
    public GroupPollDTO createPoll(String groupId, Long userId, String question, 
                                   List<String> options, boolean isMultipleChoice, 
                                   LocalDateTime expiresAt, String eventId) {
        logger.info("Usuario {} creando encuesta en grupo {}", userId, groupId);
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        // Verificar que el usuario es miembro del grupo
        if (!groupMemberRepository.findByGroupAndUser(group, user).isPresent()) {
            throw new IllegalStateException("Debes ser miembro del grupo para crear encuestas");
        }
        
        if (options == null || options.size() < 2) {
            throw new IllegalArgumentException("Una encuesta debe tener al menos 2 opciones");
        }
        
        GroupPoll poll = new GroupPoll(group, user, question, options);
        poll.setMultipleChoice(isMultipleChoice);
        poll.setExpiresAt(expiresAt);
        if (eventId != null && !eventId.trim().isEmpty()) {
            poll.setEventId(eventId);
        }
        poll = groupPollRepository.save(poll);
        
        // Enviar mensaje automático al chat con la encuesta
        StringBuilder pollMessage = new StringBuilder("📊 Nueva encuesta:\n");
        pollMessage.append(question).append("\n\n");
        for (int i = 0; i < options.size(); i++) {
            pollMessage.append(String.format("%d️⃣ %s\n", i + 1, options.get(i)));
        }
        try {
            groupMessageService.createSystemMessage(groupId, pollMessage.toString());
        } catch (Exception e) {
            logger.warn("Error al enviar mensaje de encuesta al chat: {}", e.getMessage());
        }
        
        logger.info("Encuesta creada exitosamente con ID: {}", poll.getId());
        return convertToDTO(poll, userId);
    }
    
    /**
     * Vota en una encuesta.
     */
    public GroupPollDTO votePoll(String pollId, Long userId, Integer optionIndex) {
        logger.info("Usuario {} votando en encuesta {} opción {}", userId, pollId, optionIndex);
        
        GroupPoll poll = groupPollRepository.findById(pollId)
                .orElseThrow(() -> new IllegalArgumentException("Encuesta no encontrada"));
        
        if (!poll.isActive() || poll.isExpired()) {
            throw new IllegalStateException("La encuesta no está activa o ha expirado");
        }
        
        if (optionIndex < 0 || optionIndex >= poll.getOptions().size()) {
            throw new IllegalArgumentException("Opción inválida");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        // Verificar que el usuario es miembro del grupo
        if (!groupMemberRepository.findByGroupAndUser(poll.getGroup(), user).isPresent()) {
            throw new IllegalStateException("Debes ser miembro del grupo para votar");
        }
        
        // Verificar si ya votó
        java.util.Optional<PollVote> existingVoteOpt = pollVoteRepository.findByPollAndUser(poll, user);
        
        if (existingVoteOpt.isPresent()) {
            // Si ya votó y no es múltiple opción, actualizar el voto
            if (!poll.isMultipleChoice()) {
                PollVote existingVote = existingVoteOpt.get();
                existingVote.setSelectedOptionIndex(optionIndex);
                pollVoteRepository.save(existingVote);
            }
            // Si es múltiple opción, permitir múltiples votos (crear nuevo voto)
            else {
                PollVote newVote = new PollVote(poll, user, optionIndex);
                pollVoteRepository.save(newVote);
            }
        } else {
            // Crear nuevo voto
            PollVote vote = new PollVote(poll, user, optionIndex);
            pollVoteRepository.save(vote);
        }
        
        // Si la encuesta está vinculada a un evento, registrar asistencia automáticamente
        if (poll.getEventId() != null && !poll.getEventId().trim().isEmpty()) {
            try {
                // Mapear opciones por índice:
                // Opción 0 (índice 0): "Sí, asistiré" → confirmar asistencia
                // Opción 1 (índice 1): "No, no puedo asistir" → desconfirmar asistencia
                // Opción 2 (índice 2): "Tal vez" → no hacer nada
                boolean shouldRegister = false;
                boolean attending = false;
                
                if (optionIndex == 0) {
                    // Opción 1: "Sí, asistiré" → Confirmar asistencia
                    attending = true;
                    shouldRegister = true;
                } else if (optionIndex == 1) {
                    // Opción 2: "No, no puedo asistir" → Desconfirmar asistencia
                    attending = false;
                    shouldRegister = true;
                }
                // Opción 3 (índice 2): "Tal vez" → no registrar asistencia (no hacer nada)
                
                if (shouldRegister) {
                    // Registrar o actualizar asistencia al evento
                    gameEventService.voteAttendance(poll.getEventId(), userId, attending);
                    logger.info("Asistencia {} registrada automáticamente para usuario {} en evento {} desde encuesta (opción índice {})", 
                               attending ? "confirmada" : "desconfirmada", userId, poll.getEventId(), optionIndex);
                }
            } catch (Exception e) {
                // Si falla el registro de asistencia, no es crítico, solo loguear
                logger.warn("Error al registrar asistencia automática desde encuesta: {}", e.getMessage());
            }
        }
        
        return convertToDTO(poll, userId);
    }
    
    /**
     * Obtiene todas las encuestas activas de un grupo.
     */
    @Transactional(readOnly = true)
    public List<GroupPollDTO> getGroupPolls(String groupId) {
        return getGroupPolls(groupId, null);
    }
    
    /**
     * Obtiene todas las encuestas activas de un grupo, incluyendo el voto del usuario si se proporciona.
     */
    @Transactional(readOnly = true)
    public List<GroupPollDTO> getGroupPolls(String groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        
        List<GroupPoll> polls = groupPollRepository.findByGroupAndIsActiveTrueOrderByCreatedAtDesc(group);
        return polls.stream()
                .map(poll -> convertToDTO(poll, userId))
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene una encuesta por ID.
     */
    @Transactional(readOnly = true)
    public GroupPollDTO getPollById(String pollId) {
        GroupPoll poll = groupPollRepository.findById(pollId)
                .orElseThrow(() -> new IllegalArgumentException("Encuesta no encontrada"));
        return convertToDTO(poll);
    }
    
    /**
     * Convierte una entidad GroupPoll a DTO.
     */
    private GroupPollDTO convertToDTO(GroupPoll poll) {
        return convertToDTO(poll, null);
    }
    
    /**
     * Convierte una entidad GroupPoll a DTO, incluyendo el voto del usuario si se proporciona.
     */
    private GroupPollDTO convertToDTO(GroupPoll poll, Long userId) {
        List<PollVote> votes = pollVoteRepository.findByPoll(poll);
        
        // Calcular conteo de votos por opción
        List<Integer> voteCounts = new java.util.ArrayList<>();
        for (int i = 0; i < poll.getOptions().size(); i++) {
            long count = pollVoteRepository.countByPollAndSelectedOptionIndex(poll, i);
            voteCounts.add((int) count);
        }
        
        // Buscar el voto del usuario actual
        Integer userVoteIndex = null;
        if (userId != null) {
            java.util.Optional<PollVote> userVote = pollVoteRepository.findByPollAndUser(poll, 
                userRepository.findById(userId).orElse(null));
            if (userVote.isPresent()) {
                userVoteIndex = userVote.get().getSelectedOptionIndex();
            }
        }
        
        return new GroupPollDTO(
                poll.getId(),
                poll.getGroup().getId(),
                poll.getCreatedBy().getId(),
                poll.getCreatedBy().getNombre() + " " + poll.getCreatedBy().getApellido(),
                poll.getQuestion(),
                poll.getOptions(),
                poll.isMultipleChoice(),
                poll.getExpiresAt(),
                poll.getCreatedAt(),
                poll.isActive(),
                voteCounts,
                votes.size(),
                userVoteIndex
        );
    }
}

