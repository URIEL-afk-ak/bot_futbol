package com.botfutbol.service;

import com.botfutbol.dto.GroupJoinRequestDTO;
import com.botfutbol.entity.Group;
import com.botfutbol.entity.GroupJoinRequest;
import com.botfutbol.entity.GroupMember;
import com.botfutbol.entity.User;
import com.botfutbol.repository.GroupJoinRequestRepository;
import com.botfutbol.repository.GroupMemberRepository;
import com.botfutbol.repository.GroupRepository;
import com.botfutbol.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar solicitudes de ingreso a grupos privados.
 */
@Service
public class GroupJoinRequestService {
    
    private static final Logger logger = LoggerFactory.getLogger(GroupJoinRequestService.class);
    
    @Autowired
    private GroupJoinRequestRepository joinRequestRepository;
    
    @Autowired
    private GroupRepository groupRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private GroupMemberRepository groupMemberRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private GroupMessageService groupMessageService;
    
    /**
     * Crea una solicitud de ingreso a un grupo privado.
     */
    @Transactional
    public GroupJoinRequestDTO createJoinRequest(String groupId, Long userId, String message) {
        logger.info("Creando solicitud de ingreso al grupo {} por usuario {}", groupId, userId);
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        // Verificar que el grupo es privado
        if (!group.isPrivate()) {
            throw new IllegalStateException("El grupo es público, no requiere solicitud");
        }
        
        // Verificar que el usuario no es miembro del grupo
        if (groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw new IllegalStateException("Ya eres miembro de este grupo");
        }
        
        // Verificar que no exista una solicitud pendiente
        if (joinRequestRepository.existsByGroupAndUserAndStatus(group, user, GroupJoinRequest.RequestStatus.PENDING)) {
            throw new IllegalStateException("Ya tienes una solicitud pendiente para este grupo");
        }
        
        // Crear la solicitud
        GroupJoinRequest request = new GroupJoinRequest(group, user, message);
        request = joinRequestRepository.save(request);
        
        // Notificar a los administradores del grupo
        notifyAdmins(group, user, request.getId(), "Nueva solicitud de ingreso");
        
        logger.info("Solicitud de ingreso creada: {}", request.getId());
        return convertToDTO(request);
    }
    
    /**
     * Aprueba una solicitud de ingreso.
     */
    @Transactional
    public GroupJoinRequestDTO approveRequest(String requestId, Long adminUserId) {
        logger.info("Aprobando solicitud {} por admin {}", requestId, adminUserId);
        
        GroupJoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));
        
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        // Verificar que el usuario es administrador del grupo
        GroupMember adminMember = groupMemberRepository.findByGroupAndUser(request.getGroup(), admin)
                .orElseThrow(() -> new IllegalStateException("No eres miembro del grupo"));
        
        if (adminMember.getRole() != GroupMember.MemberRole.ADMIN) {
            throw new IllegalStateException("Solo los administradores pueden aprobar solicitudes");
        }
        
        // Verificar que la solicitud está pendiente
        if (request.getStatus() != GroupJoinRequest.RequestStatus.PENDING) {
            throw new IllegalStateException("La solicitud ya fue procesada");
        }
        
        // Aprobar la solicitud
        request.setStatus(GroupJoinRequest.RequestStatus.APPROVED);
        request.setRespondedAt(LocalDateTime.now());
        request.setRespondedBy(admin);
        request = joinRequestRepository.save(request);
        
        // Agregar al usuario como miembro del grupo
        GroupMember newMember = new GroupMember(request.getGroup(), request.getUser(), GroupMember.MemberRole.MEMBER);
        groupMemberRepository.save(newMember);
        
        // Notificar al usuario
        String userName = getUserDisplayName(request.getUser());
        notificationService.createGroupNotification(
            request.getUser(),
            "Solicitud aprobada",
            "Tu solicitud para unirte a \"" + request.getGroup().getName() + "\" fue aprobada",
            NotificationService.TYPE_GROUP_INVITATION,
            request.getGroup().getId()
        );
        
        // Mensaje del sistema en el chat del grupo
        try {
            groupMessageService.createSystemMessage(
                request.getGroup().getId(),
                "👋 " + userName + " se unió al grupo"
            );
        } catch (Exception e) {
            logger.warn("Error al crear mensaje del sistema: {}", e.getMessage());
        }
        
        logger.info("Solicitud aprobada: {}", request.getId());
        return convertToDTO(request);
    }
    
    /**
     * Rechaza una solicitud de ingreso.
     */
    @Transactional
    public GroupJoinRequestDTO rejectRequest(String requestId, Long adminUserId, String rejectionReason) {
        logger.info("Rechazando solicitud {} por admin {}", requestId, adminUserId);
        
        GroupJoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));
        
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        // Verificar que el usuario es administrador del grupo
        GroupMember adminMember = groupMemberRepository.findByGroupAndUser(request.getGroup(), admin)
                .orElseThrow(() -> new IllegalStateException("No eres miembro del grupo"));
        
        if (adminMember.getRole() != GroupMember.MemberRole.ADMIN) {
            throw new IllegalStateException("Solo los administradores pueden rechazar solicitudes");
        }
        
        // Verificar que la solicitud está pendiente
        if (request.getStatus() != GroupJoinRequest.RequestStatus.PENDING) {
            throw new IllegalStateException("La solicitud ya fue procesada");
        }
        
        // Rechazar la solicitud
        request.setStatus(GroupJoinRequest.RequestStatus.REJECTED);
        request.setRespondedAt(LocalDateTime.now());
        request.setRespondedBy(admin);
        request.setRejectionReason(rejectionReason);
        request = joinRequestRepository.save(request);
        
        // Notificar al usuario
        notificationService.createGroupNotification(
            request.getUser(),
            "Solicitud rechazada",
            "Tu solicitud para unirte a \"" + request.getGroup().getName() + "\" fue rechazada",
            NotificationService.TYPE_GROUP_INVITATION,
            request.getGroup().getId()
        );
        
        logger.info("Solicitud rechazada: {}", request.getId());
        return convertToDTO(request);
    }
    
    /**
     * Obtiene las solicitudes pendientes de un grupo.
     */
    @Transactional(readOnly = true)
    public List<GroupJoinRequestDTO> getPendingRequests(String groupId, Long adminUserId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        // Verificar que el usuario es administrador
        GroupMember adminMember = groupMemberRepository.findByGroupAndUser(group, admin)
                .orElseThrow(() -> new IllegalStateException("No eres miembro del grupo"));
        
        if (adminMember.getRole() != GroupMember.MemberRole.ADMIN) {
            throw new IllegalStateException("Solo los administradores pueden ver las solicitudes");
        }
        
        List<GroupJoinRequest> requests = joinRequestRepository.findByGroupAndStatus(
                group, GroupJoinRequest.RequestStatus.PENDING);
        
        return requests.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene el historial de solicitudes de un usuario.
     */
    @Transactional(readOnly = true)
    public List<GroupJoinRequestDTO> getUserRequests(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        List<GroupJoinRequest> requests = joinRequestRepository.findByUserOrderByCreatedAtDesc(user);
        
        return requests.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Cancela una solicitud pendiente.
     */
    @Transactional
    public void cancelRequest(String requestId, Long userId) {
        GroupJoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));
        
        // Verificar que el usuario es el solicitante
        if (!request.getUser().getId().equals(userId)) {
            throw new IllegalStateException("No puedes cancelar esta solicitud");
        }
        
        // Verificar que está pendiente
        if (request.getStatus() != GroupJoinRequest.RequestStatus.PENDING) {
            throw new IllegalStateException("Solo puedes cancelar solicitudes pendientes");
        }
        
        joinRequestRepository.delete(request);
        logger.info("Solicitud cancelada: {}", requestId);
    }
    
    /**
     * Cuenta las solicitudes pendientes de un grupo.
     */
    public long countPendingRequests(String groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        
        return joinRequestRepository.countByGroupAndStatus(group, GroupJoinRequest.RequestStatus.PENDING);
    }
    
    /**
     * Crea notificaciones en la app para los administradores del grupo (sin push).
     * Las solicitudes de ingreso solo se ven dentro de la app, no generan notificaciones push.
     */
    private void notifyAdmins(Group group, User requester, String requestId, String message) {
        List<GroupMember> admins = groupMemberRepository.findByGroupAndRole(group, GroupMember.MemberRole.ADMIN);
        
        String userName = getUserDisplayName(requester);
        String title = message;
        String body = userName + " quiere unirse a \"" + group.getName() + "\"";
        
        for (GroupMember admin : admins) {
            try {
                // Crear notificación SOLO en base de datos (visible en la app)
                // NO se envía notificación push para solicitudes de ingreso
                notificationService.createNotification(
                    admin.getUser(),
                    title,
                    body,
                    "GROUP_JOIN_REQUEST"
                );
                
                logger.debug("Notificación de solicitud creada para admin {} (sin push)", admin.getUser().getId());
            } catch (Exception e) {
                logger.warn("Error al crear notificación para admin {}: {}", admin.getUser().getId(), e.getMessage());
            }
        }
    }
    
    /**
     * Obtiene el nombre de usuario para mostrar.
     */
    private String getUserDisplayName(User user) {
        String name = "";
        if (user.getNombre() != null && !user.getNombre().isEmpty()) {
            name = user.getNombre();
        }
        if (user.getApellido() != null && !user.getApellido().isEmpty()) {
            name += (name.isEmpty() ? "" : " ") + user.getApellido();
        }
        return name.isEmpty() ? "Usuario " + user.getId() : name.trim();
    }
    
    /**
     * Convierte una entidad a DTO.
     */
    private GroupJoinRequestDTO convertToDTO(GroupJoinRequest request) {
        GroupJoinRequestDTO dto = new GroupJoinRequestDTO();
        dto.setId(request.getId());
        dto.setGroupId(request.getGroup().getId());
        dto.setGroupName(request.getGroup().getName());
        dto.setGroupPhotoUrl(request.getGroup().getPhotoUrl());
        dto.setUserId(request.getUser().getId());
        dto.setUserName(getUserDisplayName(request.getUser()));
        dto.setUserEmail(request.getUser().getEmail());
        // User no tiene photoUrl por ahora
        dto.setUserPhotoUrl(null);
        dto.setStatus(request.getStatus().name());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setRespondedAt(request.getRespondedAt());
        
        if (request.getRespondedBy() != null) {
            dto.setRespondedByUserId(request.getRespondedBy().getId());
            dto.setRespondedByUserName(getUserDisplayName(request.getRespondedBy()));
        }
        
        dto.setMessage(request.getMessage());
        dto.setRejectionReason(request.getRejectionReason());
        
        return dto;
    }
}

