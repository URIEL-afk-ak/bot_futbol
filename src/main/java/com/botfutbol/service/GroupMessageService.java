package com.botfutbol.service;

import com.botfutbol.dto.GroupMessageDTO;
import com.botfutbol.entity.Group;
import com.botfutbol.entity.GroupMessage;
import com.botfutbol.entity.User;
import com.botfutbol.entity.DeletedMessage;
import com.botfutbol.repository.GroupMessageRepository;
import com.botfutbol.repository.GroupMemberRepository;
import com.botfutbol.repository.GroupRepository;
import com.botfutbol.repository.UserRepository;
import com.botfutbol.repository.DeletedMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para administrar mensajes de grupos.
 */
@Service
@Transactional
public class GroupMessageService {
    
    private static final Logger logger = LoggerFactory.getLogger(GroupMessageService.class);
    
    private final GroupMessageRepository groupMessageRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final DeletedMessageRepository deletedMessageRepository;
    
    public GroupMessageService(GroupMessageRepository groupMessageRepository,
                              GroupRepository groupRepository,
                              UserRepository userRepository,
                              GroupMemberRepository groupMemberRepository,
                              DeletedMessageRepository deletedMessageRepository) {
        this.groupMessageRepository = groupMessageRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.deletedMessageRepository = deletedMessageRepository;
    }
    
    /**
     * Envía un mensaje en un grupo.
     */
    public GroupMessageDTO sendMessage(String groupId, Long userId, String message) {
        logger.info("Usuario {} enviando mensaje en grupo {}", userId, groupId);
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        // Verificar que el usuario es miembro del grupo
        if (!groupMemberRepository.findByGroupAndUser(group, user).isPresent()) {
            throw new IllegalStateException("Debes ser miembro del grupo para enviar mensajes");
        }
        
        GroupMessage groupMessage = new GroupMessage(group, user, message);
        groupMessage = groupMessageRepository.save(groupMessage);
        
        logger.info("Mensaje enviado exitosamente con ID: {}", groupMessage.getId());
        return convertToDTO(groupMessage, user);
    }
    
    /**
     * Obtiene los mensajes de un grupo.
     */
    @Transactional(readOnly = true)
    public List<GroupMessageDTO> getGroupMessages(String groupId, Long userId) {
        logger.debug("Obteniendo mensajes del grupo {} para usuario {}", groupId, userId);
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        
        try {
            List<GroupMessage> messages = groupMessageRepository.findByGroupOrderByCreatedAtAsc(group);
            
            // Verificar y desfijar mensajes expirados
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            for (GroupMessage msg : messages) {
                if (msg.isPinned() && msg.getPinnedUntil() != null && msg.getPinnedUntil().isBefore(now)) {
                    msg.setPinned(false);
                    msg.setPinnedAt(null);
                    msg.setPinnedUntil(null);
                    groupMessageRepository.save(msg);
                    logger.info("Mensaje {} desfijado automáticamente por expiración", msg.getId());
                }
            }
            
            // Si hay más de 50 mensajes, tomar solo los últimos 50
            if (messages.size() > 50) {
                messages = messages.subList(messages.size() - 50, messages.size());
            }
            
            return messages.stream()
                    .map(msg -> convertToDTO(msg, user))
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error al obtener mensajes del grupo {}", groupId, e);
            throw new RuntimeException("Error al obtener mensajes: " + e.getMessage(), e);
        }
    }
    
    /**
     * Crea un mensaje del sistema (ej: "Usuario se unió al grupo").
     */
    public GroupMessageDTO createSystemMessage(String groupId, String message) {
        logger.debug("Creando mensaje del sistema en grupo {}: {}", groupId, message);
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        
        // Usar el usuario creador del grupo como "autor" del mensaje del sistema
        User systemUser = group.getCreatedBy();
        
        GroupMessage systemMessage = new GroupMessage(group, systemUser, message, true);
        systemMessage = groupMessageRepository.save(systemMessage);
        
        return convertToDTO(systemMessage, systemUser);
    }
    
    /**
     * Convierte una entidad GroupMessage a DTO.
     */
    private GroupMessageDTO convertToDTO(GroupMessage message, User currentUser) {
        if (message == null) {
            logger.warn("Intento de convertir un mensaje null a DTO");
            throw new IllegalArgumentException("No se puede convertir un mensaje null a DTO");
        }
        
        try {
            User user = message.getUser();
            String userName;
            Long userId;
            
            if (user == null) {
                logger.warn("Mensaje {} tiene usuario null", message.getId());
                userName = "Usuario desconocido";
                userId = null;
            } else {
                userId = user.getId();
                String nombre = user.getNombre() != null ? user.getNombre() : "";
                String apellido = user.getApellido() != null ? user.getApellido() : "";
                userName = (nombre + " " + apellido).trim();
                if (userName.isEmpty()) {
                    userName = "Usuario " + userId;
                }
            }
            
            String groupId = message.getGroup() != null ? message.getGroup().getId() : null;
            String messageText = message.getMessage() != null ? message.getMessage() : "";
            java.time.LocalDateTime createdAt = message.getCreatedAt() != null 
                    ? message.getCreatedAt() 
                    : java.time.LocalDateTime.now();
            
            // Verificar si el mensaje fue eliminado para el usuario actual
            boolean isDeletedForMe = false;
            if (currentUser != null) {
                isDeletedForMe = deletedMessageRepository.existsByMessageAndUser(message, currentUser);
            }
            
            return new GroupMessageDTO(
                    message.getId() != null ? message.getId() : java.util.UUID.randomUUID().toString(),
                    groupId,
                    userId,
                    userName,
                    messageText,
                    createdAt,
                    message.isSystemMessage(),
                    message.isPinned(),
                    message.getPinnedAt(),
                    message.getPinnedUntil(),
                    message.isHighlighted(),
                    message.isDeleted(),
                    message.getEditedAt(),
                    isDeletedForMe
            );
        } catch (Exception e) {
            logger.error("Error al convertir mensaje {} a DTO", message.getId(), e);
            throw new RuntimeException("Error al convertir mensaje a DTO: " + e.getMessage(), e);
        }
    }
    
    /**
     * Destaca un mensaje.
     */
    public GroupMessageDTO highlightMessage(String messageId, Long userId) {
        logger.info("Usuario {} destacando mensaje {}", userId, messageId);
        
        GroupMessage message = groupMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Mensaje no encontrado"));
        
        // Verificar permisos (solo el autor o admin puede destacar)
        if (!message.getUser().getId().equals(userId)) {
            // Verificar si es admin
            Group group = message.getGroup();
            boolean isAdmin = groupMemberRepository.findByGroupAndUser(group, 
                    userRepository.findById(userId).orElseThrow())
                    .map(m -> m.getRole().name().equals("ADMIN"))
                    .orElse(false);
            if (!isAdmin) {
                throw new IllegalStateException("Solo el autor o un administrador puede destacar el mensaje");
            }
        }
        
        message.setHighlighted(!message.isHighlighted());
        message = groupMessageRepository.save(message);
        
        User currentUser = userRepository.findById(userId).orElse(null);
        return convertToDTO(message, currentUser);
    }
    
    /**
     * Fija un mensaje con duración opcional.
     * @param messageId ID del mensaje
     * @param userId ID del usuario
     * @param durationInDays Duración en días (null = indefinido)
     * @return DTO del mensaje actualizado
     */
    public GroupMessageDTO pinMessage(String messageId, Long userId, Integer durationInDays) {
        logger.info("Usuario {} fijando mensaje {} con duración {} días", userId, messageId, durationInDays);
        
        GroupMessage message = groupMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Mensaje no encontrado"));
        
        // Solo administradores pueden fijar mensajes
        Group group = message.getGroup();
        boolean isAdmin = groupMemberRepository.findByGroupAndUser(group, 
                userRepository.findById(userId).orElseThrow())
                .map(m -> m.getRole().name().equals("ADMIN"))
                .orElse(false);
        if (!isAdmin) {
            throw new IllegalStateException("Solo los administradores pueden fijar mensajes");
        }
        
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        
        if (message.isPinned()) {
            // Desfijar
            message.setPinned(false);
            message.setPinnedAt(null);
            message.setPinnedUntil(null);
        } else {
            // Fijar
            message.setPinned(true);
            message.setPinnedAt(now);
            
            if (durationInDays != null && durationInDays > 0) {
                message.setPinnedUntil(now.plusDays(durationInDays));
            } else {
                // Indefinido
                message.setPinnedUntil(null);
            }
        }
        
        message = groupMessageRepository.save(message);
        
        User currentUser = userRepository.findById(userId).orElse(null);
        return convertToDTO(message, currentUser);
    }
    
    /**
     * Elimina un mensaje para todos (solo el autor o admin).
     */
    public void deleteMessageForAll(String messageId, Long userId) {
        logger.info("Usuario {} eliminando mensaje {} para todos", userId, messageId);
        
        GroupMessage message = groupMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Mensaje no encontrado"));
        
        // Verificar permisos
        if (!message.getUser().getId().equals(userId)) {
            Group group = message.getGroup();
            boolean isAdmin = groupMemberRepository.findByGroupAndUser(group, 
                    userRepository.findById(userId).orElseThrow())
                    .map(m -> m.getRole().name().equals("ADMIN"))
                    .orElse(false);
            if (!isAdmin) {
                throw new IllegalStateException("Solo el autor o un administrador puede eliminar el mensaje para todos");
            }
        }
        
        message.setDeleted(true);
        groupMessageRepository.save(message);
    }
    
    /**
     * Elimina un mensaje solo para el usuario actual (soft delete).
     */
    public void deleteMessageForMe(String messageId, Long userId) {
        logger.info("Usuario {} eliminando mensaje {} para sí mismo", userId, messageId);
        
        GroupMessage message = groupMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Mensaje no encontrado"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        // Verificar si ya está eliminado
        if (deletedMessageRepository.existsByMessageAndUser(message, user)) {
            return; // Ya está eliminado
        }
        
        DeletedMessage deletedMessage = new DeletedMessage(message, user);
        deletedMessageRepository.save(deletedMessage);
    }
    
    /**
     * Modifica un mensaje (solo el autor, con límite de tiempo de 15 minutos).
     */
    public GroupMessageDTO editMessage(String messageId, Long userId, String newMessage) {
        logger.info("Usuario {} editando mensaje {}", userId, messageId);
        
        GroupMessage message = groupMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Mensaje no encontrado"));
        
        // Solo el autor puede editar
        if (!message.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Solo el autor puede editar el mensaje");
        }
        
        // Verificar límite de tiempo (15 minutos)
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        long minutesSinceCreation = java.time.Duration.between(message.getCreatedAt(), now).toMinutes();
        if (minutesSinceCreation > 15) {
            throw new IllegalStateException("No se puede editar el mensaje después de 15 minutos");
        }
        
        message.setMessage(newMessage);
        message.setEditedAt(now);
        message = groupMessageRepository.save(message);
        
        User currentUser = userRepository.findById(userId).orElse(null);
        return convertToDTO(message, currentUser);
    }
}

