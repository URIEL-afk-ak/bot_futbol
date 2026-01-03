package com.botfutbol.repository;

import com.botfutbol.entity.EventTeam;
import com.botfutbol.entity.GameEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para administrar equipos formados para eventos.
 */
@Repository
public interface EventTeamRepository extends JpaRepository<EventTeam, String> {
    
    // Busca todos los equipos de un evento
    List<EventTeam> findByEventOrderByTeamId(GameEvent event);
    
    // Elimina todos los equipos de un evento
    void deleteByEvent(GameEvent event);
}

