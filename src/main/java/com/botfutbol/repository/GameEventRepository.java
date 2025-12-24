package com.botfutbol.repository;

import com.botfutbol.entity.GameEvent;
import com.botfutbol.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para administrar eventos de juego.
 */
@Repository
public interface GameEventRepository extends JpaRepository<GameEvent, String> {
    
    // Busca eventos activos de un grupo
    List<GameEvent> findByGroupAndActiveTrueOrderByDateAsc(Group group);
    
    // Busca todos los eventos de un grupo
    List<GameEvent> findByGroupOrderByDateDesc(Group group);
    
    // Busca eventos futuros de un grupo
    List<GameEvent> findByGroupAndDateAfterAndActiveTrueOrderByDateAsc(Group group, LocalDateTime date);
    
    // Busca eventos creados por un usuario
    List<GameEvent> findByCreatedBy_Id(Long userId);
}

