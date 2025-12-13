package com.botfutbol.repository;

import com.botfutbol.entity.PlayerLevelHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlayerLevelHistoryRepository extends JpaRepository<PlayerLevelHistory, Long> {
    List<PlayerLevelHistory> findByPlayerNameOrderByDateAsc(String playerName);
}
