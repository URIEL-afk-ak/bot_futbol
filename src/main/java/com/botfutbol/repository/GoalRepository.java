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

    // Busca goles por usuario
    List<Goal> findByUser(com.botfutbol.entity.User user);

    // Busca goles por jugador (ID) y usuario
    List<Goal> findByPlayerIdAndUser(String playerId, com.botfutbol.entity.User user);

    // Busca goles por equipo y usuario
    List<Goal> findByTeamIdAndUser(String teamId, com.botfutbol.entity.User user);

    // Busca goles por partido y usuario
    List<Goal> findByMatchIdAndUser(String matchId, com.botfutbol.entity.User user);

    // Cuenta goles de un jugador y usuario
    long countByPlayerIdAndUser(String playerId, com.botfutbol.entity.User user);

    // Cuenta goles de un equipo y usuario
    long countByTeamIdAndUser(String teamId, com.botfutbol.entity.User user);

    // Obtiene el último gol de un partido y usuario (para deshacer)
    List<Goal> findTop1ByMatchIdAndUserOrderByTimestampDesc(String matchId, com.botfutbol.entity.User user);

    // Métodos originales (si necesitas compatibilidad)
    List<Goal> findByPlayerId(String playerId);
    List<Goal> findByTeamId(String teamId);
    List<Goal> findByMatchId(String matchId);
    long countByPlayerId(String playerId);
    long countByTeamId(String teamId);
}
