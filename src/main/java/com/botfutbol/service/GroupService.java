package com.botfutbol.service;

import com.botfutbol.dto.GroupDTO;
import com.botfutbol.dto.GroupMemberDTO;
import com.botfutbol.entity.Group;
import com.botfutbol.entity.GroupInvitation;
import com.botfutbol.entity.GroupMember;
import com.botfutbol.entity.User;
import com.botfutbol.repository.GroupInvitationRepository;
import com.botfutbol.repository.GroupMemberRepository;
import com.botfutbol.repository.GroupRepository;
import com.botfutbol.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para administrar grupos y miembros.
 */
@Service
@Transactional
public class GroupService {
    
    private static final Logger logger = LoggerFactory.getLogger(GroupService.class);
    
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final GroupInvitationRepository groupInvitationRepository;
    private final NotificationService notificationService;
    private final GroupMessageService groupMessageService;
    
    public GroupService(GroupRepository groupRepository, 
                       GroupMemberRepository groupMemberRepository,
                       UserRepository userRepository,
                       GroupInvitationRepository groupInvitationRepository,
                       NotificationService notificationService,
                       GroupMessageService groupMessageService) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
        this.groupInvitationRepository = groupInvitationRepository;
        this.notificationService = notificationService;
        this.groupMessageService = groupMessageService;
    }
    
    /**
     * Crea un nuevo grupo y agrega al creador como administrador.
     */
    public GroupDTO createGroup(String name, String description, Long userId, boolean isPrivate, String type) {
        logger.info("Creando grupo '{}' (tipo: {}, privado: {}) para usuario {}", name, type, isPrivate, userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        Group group = new Group(name, description, user, isPrivate, type);
        group = groupRepository.save(group);
        
        // Agregar al creador como administrador
        GroupMember adminMember = new GroupMember(group, user, GroupMember.MemberRole.ADMIN);
        groupMemberRepository.save(adminMember);
        
        logger.info("Grupo creado exitosamente con ID: {}", group.getId());
        return convertToDTO(group);
    }
    
    /**
     * Un usuario se une a un grupo.
     * Si el grupo es privado, solo se puede unir mediante invitación.
     */
    public GroupMemberDTO joinGroup(String groupId, Long userId) {
        logger.info("Usuario {} intentando unirse al grupo {}", userId, groupId);
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        
        if (!group.isActive()) {
            throw new IllegalStateException("El grupo no está activo");
        }
        
        // Si el grupo es privado, solo se puede unir mediante invitación
        if (group.isPrivate()) {
            throw new IllegalStateException("Este grupo es privado. Debes ser invitado por un administrador.");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        // Verificar si ya es miembro
        if (groupMemberRepository.findByGroupAndUser(group, user).isPresent()) {
            throw new IllegalStateException("El usuario ya es miembro del grupo");
        }
        
        GroupMember member = new GroupMember(group, user, GroupMember.MemberRole.MEMBER);
        member = groupMemberRepository.save(member);
        
        // Crear mensaje del sistema en el chat
        try {
            String userName = getUserDisplayName(user);
            groupMessageService.createSystemMessage(
                groupId,
                "👋 " + userName + " se unió al grupo"
            );
        } catch (Exception e) {
            logger.warn("Error al crear mensaje del sistema: {}", e.getMessage());
        }
        
        logger.info("Usuario {} se unió al grupo {}", userId, groupId);
        return convertMemberToDTO(member);
    }
    
    /**
     * Un usuario abandona un grupo.
     */
    public void leaveGroup(String groupId, Long userId) {
        logger.info("Usuario {} abandonando el grupo {}", userId, groupId);
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        GroupMember member = groupMemberRepository.findByGroupAndUser(group, user)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no es miembro del grupo"));
        
        // No permitir que el creador abandone el grupo (o convertir a otro admin)
        if (member.getRole() == GroupMember.MemberRole.ADMIN && 
            group.getCreatedBy().getId().equals(userId)) {
            throw new IllegalStateException("El creador del grupo no puede abandonarlo");
        }
        
        groupMemberRepository.delete(member);
        
        // Crear mensaje del sistema en el chat
        try {
            String userName = getUserDisplayName(user);
            groupMessageService.createSystemMessage(
                groupId,
                "👋 " + userName + " salió del grupo"
            );
        } catch (Exception e) {
            logger.warn("Error al crear mensaje del sistema: {}", e.getMessage());
        }
        
        logger.info("Usuario {} abandonó el grupo {}", userId, groupId);
    }
    
    /**
     * Obtiene todos los grupos de un usuario.
     */
    @Transactional(readOnly = true)
    public List<GroupDTO> getUserGroups(Long userId) {
        logger.debug("Obteniendo grupos del usuario {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        List<GroupMember> memberships = groupMemberRepository.findByUser(user);
        // Filtrar solo grupos activos (no eliminados)
        return memberships.stream()
                .filter(m -> m.getGroup().isActive())
                .map(m -> convertToDTO(m.getGroup()))
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene un grupo por ID.
     */
    @Transactional(readOnly = true)
    public GroupDTO getGroupById(String groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        return convertToDTO(group);
    }
    
    /**
     * Obtiene todos los miembros de un grupo.
     */
    @Transactional(readOnly = true)
    public List<GroupMemberDTO> getGroupMembers(String groupId) {
        logger.debug("Obteniendo miembros del grupo {}", groupId);
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        
        List<GroupMember> members = groupMemberRepository.findByGroup(group);
        return members.stream()
                .map(this::convertMemberToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene todas las entidades de miembros de un grupo (para uso interno).
     */
    @Transactional(readOnly = true)
    public List<GroupMember> getGroupMembersEntities(String groupId) {
        logger.debug("Obteniendo entidades de miembros del grupo {}", groupId);
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        
        return groupMemberRepository.findByGroup(group);
    }
    
    /**
     * Verifica si un usuario es miembro de un grupo.
     */
    @Transactional(readOnly = true)
    public boolean isUserMemberOfGroup(String groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        return groupMemberRepository.findByGroupAndUser(group, user).isPresent();
    }
    
    /**
     * Verifica si un usuario es administrador de un grupo.
     */
    @Transactional(readOnly = true)
    public boolean isUserAdminOfGroup(String groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        return groupMemberRepository.findByGroupAndUser(group, user)
                .map(m -> m.getRole() == GroupMember.MemberRole.ADMIN)
                .orElse(false);
    }
    
    /**
     * Invita a un usuario al grupo por su username.
     */
    public GroupMemberDTO inviteUserByUsername(String groupId, String username, Long inviterUserId) {
        logger.info("Usuario {} invitando a {} al grupo {}", inviterUserId, username, groupId);
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        
        // Verificar que el que invita es miembro del grupo
        if (!isUserMemberOfGroup(groupId, inviterUserId)) {
            throw new IllegalStateException("Solo los miembros del grupo pueden invitar a otros");
        }
        
        // Buscar usuario por username
        User invitedUser = userRepository.findByUsername(username);
        if (invitedUser == null) {
            throw new IllegalArgumentException("Usuario con username '" + username + "' no encontrado");
        }
        
        // Verificar si ya es miembro
        if (groupMemberRepository.findByGroupAndUser(group, invitedUser).isPresent()) {
            throw new IllegalStateException("El usuario ya es miembro del grupo");
        }
        
        GroupMember member = new GroupMember(group, invitedUser, GroupMember.MemberRole.MEMBER);
        member = groupMemberRepository.save(member);
        
        // Crear notificación para el usuario invitado
        User inviter = userRepository.findById(inviterUserId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario que invita no encontrado"));
        String inviterName = inviter.getNombre() + " " + inviter.getApellido();
        notificationService.createGroupNotification(
            invitedUser,
            "Te agregaron a un grupo",
            inviterName + " te agregó al grupo \"" + group.getName() + "\"",
            NotificationService.TYPE_GROUP_INVITATION,
            groupId
        );
        
        // Crear mensaje del sistema en el chat
        try {
            String userName = getUserDisplayName(invitedUser);
            groupMessageService.createSystemMessage(
                groupId,
                "👋 " + userName + " se unió al grupo"
            );
        } catch (Exception e) {
            logger.warn("Error al crear mensaje del sistema: {}", e.getMessage());
        }
        
        logger.info("Usuario {} invitado exitosamente al grupo {}", username, groupId);
        return convertMemberToDTO(member);
    }
    
    /**
     * Elimina a un miembro del grupo (solo administradores).
     */
    public void removeMemberFromGroup(String groupId, Long memberUserId, Long adminUserId) {
        logger.info("Administrador {} eliminando miembro {} del grupo {}", adminUserId, memberUserId, groupId);
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        
        // Verificar que el que elimina es administrador
        if (!isUserAdminOfGroup(groupId, adminUserId)) {
            throw new IllegalStateException("Solo los administradores pueden eliminar miembros");
        }
        
        User memberToRemove = userRepository.findById(memberUserId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario a eliminar no encontrado"));
        
        GroupMember member = groupMemberRepository.findByGroupAndUser(group, memberToRemove)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no es miembro del grupo"));
        
        // No permitir que un admin elimine a otro admin (o al creador)
        if (member.getRole() == GroupMember.MemberRole.ADMIN) {
            throw new IllegalStateException("No se puede eliminar a otro administrador");
        }
        
        groupMemberRepository.delete(member);
        
        // Crear mensaje del sistema en el chat
        try {
            String userName = getUserDisplayName(memberToRemove);
            groupMessageService.createSystemMessage(
                groupId,
                "❌ " + userName + " fue eliminado del grupo"
            );
        } catch (Exception e) {
            logger.warn("Error al crear mensaje del sistema: {}", e.getMessage());
        }
        
        logger.info("Miembro {} eliminado del grupo {}", memberUserId, groupId);
    }
    
    /**
     * Crea un enlace de invitación para un grupo.
     */
    public GroupInvitation createInvitationLink(String groupId, Long userId, LocalDateTime expiresAt, Integer maxUses) {
        logger.info("Usuario {} creando enlace de invitación para grupo {}", userId, groupId);
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        
        // Verificar que el usuario es miembro del grupo
        if (!isUserMemberOfGroup(groupId, userId)) {
            throw new IllegalStateException("Solo los miembros del grupo pueden crear enlaces de invitación");
        }
        
        GroupInvitation invitation = new GroupInvitation(group, userId, expiresAt, maxUses);
        invitation = groupInvitationRepository.save(invitation);
        
        logger.info("Enlace de invitación creado con código: {}", invitation.getInvitationCode());
        return invitation;
    }
    
    /**
     * Unirse a un grupo usando un código de invitación.
     * Si el grupo es privado, genera un error indicando que debe solicitar acceso.
     */
    public GroupMemberDTO joinGroupByInvitationCode(String invitationCode, Long userId) {
        logger.info("Usuario {} uniéndose al grupo con código de invitación {}", userId, invitationCode);
        
        GroupInvitation invitation = groupInvitationRepository.findByInvitationCode(invitationCode)
                .orElseThrow(() -> new IllegalArgumentException("Código de invitación inválido"));
        
        if (!invitation.isValid()) {
            throw new IllegalStateException("El código de invitación ha expirado o ya no es válido");
        }
        
        Group group = invitation.getGroup();
        
        if (!group.isActive()) {
            throw new IllegalStateException("El grupo no está activo");
        }
        
        // Si el grupo es privado, no permitir unión directa
        if (group.isPrivate()) {
            throw new IllegalStateException("Este grupo es privado. Debes solicitar acceso y esperar a que un administrador apruebe tu solicitud.");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        // Verificar si ya es miembro
        if (groupMemberRepository.findByGroupAndUser(group, user).isPresent()) {
            throw new IllegalStateException("El usuario ya es miembro del grupo");
        }
        
        // Agregar como miembro (solo si es público)
        GroupMember member = new GroupMember(group, user, GroupMember.MemberRole.MEMBER);
        member = groupMemberRepository.save(member);
        
        // Incrementar contador de usos
        invitation.incrementUses();
        groupInvitationRepository.save(invitation);
        
        // Crear notificación para el usuario que se unió
        notificationService.createGroupNotification(
            user,
            "Te uniste a un grupo",
            "Te uniste al grupo \"" + group.getName() + "\"",
            NotificationService.TYPE_GROUP_JOINED,
            group.getId()
        );
        
        // Crear mensaje del sistema en el chat
        try {
            String userName = getUserDisplayName(user);
            groupMessageService.createSystemMessage(
                group.getId(),
                "👋 " + userName + " se unió al grupo"
            );
        } catch (Exception e) {
            logger.warn("Error al crear mensaje del sistema: {}", e.getMessage());
        }
        
        logger.info("Usuario {} se unió al grupo {} usando código de invitación", userId, group.getId());
        return convertMemberToDTO(member);
    }
    
    /**
     * Obtiene el enlace de invitación activo de un grupo (si existe).
     */
    @Transactional(readOnly = true)
    public GroupInvitation getActiveInvitationLink(String groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        
        List<GroupInvitation> activeInvitations = groupInvitationRepository.findByGroupAndIsActiveTrue(group);
        return activeInvitations.stream()
                .filter(GroupInvitation::isValid)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Obtiene información de un grupo por código de invitación sin unirse.
     */
    @Transactional(readOnly = true)
    public GroupDTO getGroupByInvitationCode(String invitationCode) {
        logger.info("Obteniendo información de grupo con código de invitación {}", invitationCode);
        
        GroupInvitation invitation = groupInvitationRepository.findByInvitationCode(invitationCode)
                .orElseThrow(() -> new IllegalArgumentException("Código de invitación inválido"));
        
        if (!invitation.isValid()) {
            throw new IllegalStateException("El código de invitación ha expirado o ya no es válido");
        }
        
        Group group = invitation.getGroup();
        
        if (!group.isActive()) {
            throw new IllegalStateException("El grupo no está activo");
        }
        
        return convertToDTO(group);
    }
    
    /**
     * Actualiza un grupo existente (solo administradores o el creador).
     */
    public GroupDTO updateGroup(String groupId, String name, String description, String type, String photoUrl, Boolean isPrivate, Long userId) {
        logger.info("Usuario {} actualizando grupo {}", userId, groupId);
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        
        // Verificar que el usuario es administrador o el creador
        if (!isUserAdminOfGroup(groupId, userId) && !group.getCreatedBy().getId().equals(userId)) {
            throw new IllegalStateException("Solo los administradores o el creador pueden editar el grupo");
        }
        
        if (name != null && !name.trim().isEmpty()) {
            group.setName(name.trim());
        }
        
        if (description != null) {
            group.setDescription(description.trim().isEmpty() ? null : description.trim());
        }
        
        if (type != null) {
            group.setType(type.trim().isEmpty() ? null : type.trim());
        }
        
        if (photoUrl != null) {
            group.setPhotoUrl(photoUrl.trim().isEmpty() ? null : photoUrl.trim());
        }
        
        if (isPrivate != null) {
            group.setPrivate(isPrivate);
            logger.info("Grupo {} cambiado a {}", groupId, isPrivate ? "privado" : "público");
        }
        
        group = groupRepository.save(group);
        logger.info("Grupo {} actualizado exitosamente", groupId);
        return convertToDTO(group);
    }
    
    /**
     * Elimina un grupo (solo el creador o administradores).
     */
    public void deleteGroup(String groupId, Long userId) {
        logger.info("Usuario {} eliminando grupo {}", userId, groupId);
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        
        // Verificar que el usuario es administrador o el creador
        if (!isUserAdminOfGroup(groupId, userId) && !group.getCreatedBy().getId().equals(userId)) {
            throw new IllegalStateException("Solo los administradores o el creador pueden eliminar el grupo");
        }
        
        // Marcar el grupo como inactivo en lugar de eliminarlo físicamente
        // Esto preserva los datos históricos (eventos, calificaciones, etc.)
        group.setActive(false);
        groupRepository.save(group);
        
        logger.info("Grupo {} eliminado (marcado como inactivo) por usuario {}", groupId, userId);
    }
    
    /**
     * Convierte una entidad Group a DTO.
     */
    private GroupDTO convertToDTO(Group group) {
        int memberCount = (int) groupMemberRepository.countByGroup(group);
        String createdByName = group.getCreatedBy().getNombre() + " " + group.getCreatedBy().getApellido();
        
        // Manejar valores null para grupos antiguos
        boolean isPrivate = group.isPrivate();
        String type = group.getType() != null ? group.getType() : "FUTBOL"; // Valor por defecto
        
        return new GroupDTO(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getCreatedAt(),
                group.getCreatedBy().getId(),
                createdByName,
                group.isActive(),
                memberCount,
                isPrivate,
                type,
                group.getPhotoUrl()
        );
    }
    
    /**
     * Busca grupos por nombre (públicos y privados).
     * Incluye información adicional para el usuario actual.
     */
    @Transactional(readOnly = true)
    public List<GroupDTO> searchGroups(String query, Long userId) {
        logger.info("Buscando grupos con query: '{}' para usuario {}", query, userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        // Normalizar el query para búsqueda sin acentos
        String normalizedQuery = normalizeString(query.toLowerCase());
        
        // Buscar grupos activos que coincidan con el query (sin importar acentos)
        List<Group> groups = groupRepository.findAll().stream()
                .filter(g -> g.isActive() && 
                            (normalizeString(g.getName().toLowerCase()).contains(normalizedQuery) ||
                             (g.getDescription() != null && normalizeString(g.getDescription().toLowerCase()).contains(normalizedQuery))))
                .collect(Collectors.toList());
        
        // Convertir a DTO incluyendo información del usuario actual
        return groups.stream()
                .map(group -> {
                    GroupDTO dto = convertToDTO(group);
                    // Verificar si el usuario es miembro
                    boolean isMember = groupMemberRepository.existsByGroupAndUser(group, user);
                    dto.setMember(isMember);
                    // Nota: hasPendingRequest se configurará desde el frontend usando otro endpoint
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Normaliza un string removiendo acentos para búsquedas.
     */
    private String normalizeString(String text) {
        if (text == null) return "";
        
        // Normalizar a NFD (Canonical Decomposition) y remover marcas diacríticas
        String normalized = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD);
        // Remover caracteres de acento (categoría \p{M} = Marks)
        return normalized.replaceAll("\\p{M}", "");
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
     * Convierte una entidad GroupMember a DTO.
     */
    private GroupMemberDTO convertMemberToDTO(GroupMember member) {
        String userName = member.getUser().getNombre() + " " + member.getUser().getApellido();
        String profileImageUrl = member.getUser().getProfileImageUrl();
        
        return new GroupMemberDTO(
                member.getId(),
                member.getGroup().getId(),
                member.getGroup().getName(),
                member.getUser().getId(),
                userName,
                member.getUser().getEmail(),
                profileImageUrl,
                member.getRole().name(),
                member.getJoinedAt()
        );
    }
    
    /**
     * Promueve un miembro a administrador.
     */
    @Transactional
    public void promoteToAdmin(String groupId, Long memberUserId, Long requestingUserId) {
        logger.info("Usuario {} promoviendo a usuario {} como admin en grupo {}", 
                   requestingUserId, memberUserId, groupId);
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        
        User requestingUser = userRepository.findById(requestingUserId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario solicitante no encontrado"));
        
        User memberToPromote = userRepository.findById(memberUserId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario a promover no encontrado"));
        
        // Verificar que el usuario solicitante es admin del grupo
        GroupMember requestingMember = groupMemberRepository.findByGroupAndUser(group, requestingUser)
                .orElseThrow(() -> new IllegalStateException("El usuario solicitante no es miembro del grupo"));
        
        if (requestingMember.getRole() != GroupMember.MemberRole.ADMIN) {
            throw new IllegalStateException("Solo los administradores pueden promover a otros usuarios");
        }
        
        // Verificar que el usuario a promover es miembro del grupo
        GroupMember memberToPromoteEntity = groupMemberRepository.findByGroupAndUser(group, memberToPromote)
                .orElseThrow(() -> new IllegalStateException("El usuario a promover no es miembro del grupo"));
        
        if (memberToPromoteEntity.getRole() == GroupMember.MemberRole.ADMIN) {
            throw new IllegalStateException("El usuario ya es administrador");
        }
        
        // Promover a admin
        memberToPromoteEntity.setRole(GroupMember.MemberRole.ADMIN);
        groupMemberRepository.save(memberToPromoteEntity);
        
        logger.info("Usuario {} promovido a administrador en grupo {}", memberUserId, groupId);
    }
    
    /**
     * Degrada un administrador a miembro regular.
     */
    @Transactional
    public void demoteFromAdmin(String groupId, Long memberUserId, Long requestingUserId) {
        logger.info("Usuario {} degradando a usuario {} de admin en grupo {}", 
                   requestingUserId, memberUserId, groupId);
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        
        User requestingUser = userRepository.findById(requestingUserId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario solicitante no encontrado"));
        
        User memberToDemote = userRepository.findById(memberUserId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario a degradar no encontrado"));
        
        // Verificar que el usuario solicitante es admin del grupo
        GroupMember requestingMember = groupMemberRepository.findByGroupAndUser(group, requestingUser)
                .orElseThrow(() -> new IllegalStateException("El usuario solicitante no es miembro del grupo"));
        
        if (requestingMember.getRole() != GroupMember.MemberRole.ADMIN) {
            throw new IllegalStateException("Solo los administradores pueden degradar a otros usuarios");
        }
        
        // No se puede degradar al creador del grupo
        if (memberUserId.equals(group.getCreatedBy().getId())) {
            throw new IllegalStateException("No se puede degradar al creador del grupo");
        }
        
        // Verificar que el usuario a degradar es miembro del grupo
        GroupMember memberToDemoteEntity = groupMemberRepository.findByGroupAndUser(group, memberToDemote)
                .orElseThrow(() -> new IllegalStateException("El usuario a degradar no es miembro del grupo"));
        
        if (memberToDemoteEntity.getRole() != GroupMember.MemberRole.ADMIN) {
            throw new IllegalStateException("El usuario no es administrador");
        }
        
        // Degradar a miembro regular
        memberToDemoteEntity.setRole(GroupMember.MemberRole.MEMBER);
        groupMemberRepository.save(memberToDemoteEntity);
        
        logger.info("Usuario {} degradado de administrador en grupo {}", memberUserId, groupId);
    }
}

