package com.botfutbol.repository;

import com.botfutbol.entity.PlayerLevelHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlayerLevelHistoryRepository extends JpaRepository<PlayerLevelHistory, Long> {

    // Busca historial de nivel por usuario
    List<PlayerLevelHistory> findByUser(com.botfutbol.entity.User user);

    // Busca historial de nivel por nombre de jugador y usuario
    List<PlayerLevelHistory> findByPlayerNameAndUserOrderByDateAsc(String playerName, com.botfutbol.entity.User user);

    // Método original (si necesitas compatibilidad)
    List<PlayerLevelHistory> findByPlayerNameOrderByDateAsc(String playerName);
}
