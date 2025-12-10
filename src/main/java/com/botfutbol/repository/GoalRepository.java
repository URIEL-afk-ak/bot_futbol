package com.botfutbol.repository;

import com.botfutbol.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para administrar goles.
 * Responsabilidad: Guardar, leer y consultar goles registrados usando Spring Data JPA.
 */
@Repository
public interface GoalRepository extends JpaRepository<Goal, String> {
    
    /**
     * Busca goles por jugador (ID).
     */
    List<Goal> findByPlayerId(String playerId);
    
    /**
     * Busca goles por equipo.
     */
    List<Goal> findByTeamId(String teamId);
    
    /**
     * Busca goles por partido.
     */
    List<Goal> findByMatchId(String matchId);
    
    /**
     * Cuenta goles de un jugador.
     */
    long countByPlayerId(String playerId);
    
    /**
     * Cuenta goles de un equipo.
     */
    long countByTeamId(String teamId);
}
