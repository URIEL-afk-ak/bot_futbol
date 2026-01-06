package com.botfutbol.repository;

import com.botfutbol.entity.Group;
import com.botfutbol.entity.GroupMember;
import com.botfutbol.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para administrar miembros de grupos.
 */
@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, String> {
    
    // Busca todos los miembros de un grupo
    List<GroupMember> findByGroup(Group group);
    
    // Busca todos los grupos de un usuario
    List<GroupMember> findByUser(User user);
    
    // Busca si un usuario es miembro de un grupo específico
    Optional<GroupMember> findByGroupAndUser(Group group, User user);
    
    // Verifica si un usuario es miembro de un grupo
    boolean existsByGroupAndUser(Group group, User user);
    
    // Busca todos los miembros activos de un grupo
    List<GroupMember> findByGroupAndUser_IdIn(Group group, List<Long> userIds);
    
    // Cuenta miembros de un grupo
    long countByGroup(Group group);
    
    // Busca miembros por rol
    List<GroupMember> findByGroupAndRole(Group group, GroupMember.MemberRole role);
}



