package com.botfutbol.repository;

import com.botfutbol.entity.Group;
import com.botfutbol.entity.GroupJoinRequest;
import com.botfutbol.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar solicitudes de ingreso a grupos.
 */
@Repository
public interface GroupJoinRequestRepository extends JpaRepository<GroupJoinRequest, String> {
    
    /**
     * Encuentra todas las solicitudes pendientes de un grupo.
     */
    List<GroupJoinRequest> findByGroupAndStatus(Group group, GroupJoinRequest.RequestStatus status);
    
    /**
     * Encuentra todas las solicitudes de un usuario.
     */
    List<GroupJoinRequest> findByUserOrderByCreatedAtDesc(User user);
    
    /**
     * Encuentra solicitudes pendientes de un usuario para un grupo específico.
     */
    Optional<GroupJoinRequest> findByGroupAndUserAndStatus(Group group, User user, GroupJoinRequest.RequestStatus status);
    
    /**
     * Verifica si existe una solicitud pendiente.
     */
    boolean existsByGroupAndUserAndStatus(Group group, User user, GroupJoinRequest.RequestStatus status);
    
    /**
     * Cuenta solicitudes pendientes de un grupo.
     */
    long countByGroupAndStatus(Group group, GroupJoinRequest.RequestStatus status);
}

