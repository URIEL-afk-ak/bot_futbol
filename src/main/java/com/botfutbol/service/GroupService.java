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
    
    public GroupService(GroupRepository groupRepository, 
                       GroupMemberRepository groupMemberRepository,
                       UserRepository userRepository,
                       GroupInvitationRepository groupInvitationRepository) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
        this.groupInvitationRepository = groupInvitationRepository;
    }
    
    /**
     * Crea un nuevo grupo y agrega al creador como administrador.
     */
    public GroupDTO createGroup(String name, String description, Long userId) {
        logger.info("Creando grupo '{}' para usuario {}", name, userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        Group group = new Group(name, description, user);
        group = groupRepository.save(group);
        
        // Agregar al creador como administrador
        GroupMember adminMember = new GroupMember(group, user, GroupMember.MemberRole.ADMIN);
        groupMemberRepository.save(adminMember);
        
        logger.info("Grupo creado exitosamente con ID: {}", group.getId());
        return convertToDTO(group);
    }
    
    /**
     * Un usuario se une a un grupo.
     */
    public GroupMemberDTO joinGroup(String groupId, Long userId) {
        logger.info("Usuario {} uniéndose al grupo {}", userId, groupId);
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        
        if (!group.isActive()) {
            throw new IllegalStateException("El grupo no está activo");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        // Verificar si ya es miembro
        if (groupMemberRepository.findByGroupAndUser(group, user).isPresent()) {
            throw new IllegalStateException("El usuario ya es miembro del grupo");
        }
        
        GroupMember member = new GroupMember(group, user, GroupMember.MemberRole.MEMBER);
        member = groupMemberRepository.save(member);
        
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
        return memberships.stream()
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
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        // Verificar si ya es miembro
        if (groupMemberRepository.findByGroupAndUser(group, user).isPresent()) {
            throw new IllegalStateException("El usuario ya es miembro del grupo");
        }
        
        // Agregar como miembro
        GroupMember member = new GroupMember(group, user, GroupMember.MemberRole.MEMBER);
        member = groupMemberRepository.save(member);
        
        // Incrementar contador de usos
        invitation.incrementUses();
        groupInvitationRepository.save(invitation);
        
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
     * Convierte una entidad Group a DTO.
     */
    private GroupDTO convertToDTO(Group group) {
        int memberCount = (int) groupMemberRepository.countByGroup(group);
        String createdByName = group.getCreatedBy().getNombre() + " " + group.getCreatedBy().getApellido();
        
        return new GroupDTO(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getCreatedAt(),
                group.getCreatedBy().getId(),
                createdByName,
                group.isActive(),
                memberCount
        );
    }
    
    /**
     * Convierte una entidad GroupMember a DTO.
     */
    private GroupMemberDTO convertMemberToDTO(GroupMember member) {
        String userName = member.getUser().getNombre() + " " + member.getUser().getApellido();
        
        return new GroupMemberDTO(
                member.getId(),
                member.getGroup().getId(),
                member.getGroup().getName(),
                member.getUser().getId(),
                userName,
                member.getUser().getEmail(),
                member.getRole().name(),
                member.getJoinedAt()
        );
    }
}

