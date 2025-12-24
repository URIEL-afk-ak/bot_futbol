package com.botfutbol.repository;

import com.botfutbol.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para administrar grupos.
 */
@Repository
public interface GroupRepository extends JpaRepository<Group, String> {
    
    // Busca grupos activos
    List<Group> findByIsActiveTrue();
    
    // Busca grupos creados por un usuario
    List<Group> findByCreatedBy_Id(Long userId);
    
    // Busca grupos por nombre (case insensitive)
    List<Group> findByNameIgnoreCaseContaining(String name);
}

