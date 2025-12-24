package com.botfutbol.repository;

import com.botfutbol.entity.Group;
import com.botfutbol.entity.GroupInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para administrar invitaciones a grupos.
 */
@Repository
public interface GroupInvitationRepository extends JpaRepository<GroupInvitation, String> {
    
    // Busca invitación por código
    Optional<GroupInvitation> findByInvitationCode(String invitationCode);
    
    // Busca todas las invitaciones activas de un grupo
    List<GroupInvitation> findByGroupAndIsActiveTrue(Group group);
    
    // Busca invitaciones creadas por un usuario
    List<GroupInvitation> findByCreatedByUserId(Long userId);
}

