package com.botfutbol.service;

import com.botfutbol.dto.PlayerDTO;
import com.botfutbol.dto.PlayerLevelHistoryDTO;
import com.botfutbol.entity.Player;
import com.botfutbol.entity.PlayerLevelHistory;
import com.botfutbol.entity.User;
import com.botfutbol.repository.PlayerLevelHistoryRepository;
import com.botfutbol.repository.PlayerRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para administrar jugadores.
 * Responsabilidad: Lógica de negocio relacionada con jugadores.
 */
@Service
@Transactional
public class PlayerService {
    
    private final PlayerRepository playerRepository;

    @Autowired
    private PlayerLevelHistoryRepository playerLevelHistoryRepository;
    
    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }
    
    /**
     * Agrega un nuevo jugador.
     */
    public Player addPlayer(PlayerDTO playerDTO, User user) {
        Player player = new Player();
        player.setName(playerDTO.getName());
        player.setSkillLevel(playerDTO.getSkillLevel() != null ? playerDTO.getSkillLevel() : 5);
        player.setPosition(playerDTO.getPosition() != null ? playerDTO.getPosition() : "MED");
        player.setTotalDebt(0);
        player.setTotalPaid(0);
        player.setGamesPlayed(0);
        player.setGoalsScored(0);
        player.setAttended(false);
        player.setUser(user);
        return playerRepository.save(player);
    }
    
    /**
     * Busca un jugador por nombre.
     */
    public Optional<Player> findPlayerByName(String name, User user) {
        return playerRepository.findByNameIgnoreCaseAndUser(name, user);
    }
    
    /**
     * Obtiene todos los jugadores.
     */
    public List<Player> getAllPlayers(User user) {
        return playerRepository.findByUser(user);
    }
    
    /**
     * Elimina un jugador por nombre.
     */
    public boolean removePlayer(String name, User user) {
        Optional<Player> player = playerRepository.findByNameIgnoreCaseAndUser(name, user);
        if (player.isPresent()) {
            playerRepository.delete(player.get());
            return true;
        }
        return false;
    }
    
    /**
     * Actualiza el nivel de habilidad de un jugador.
     */
    public Player updateSkillLevel(String name, int skillLevel, User user) {
        if (skillLevel < 1 || skillLevel > 10) {
            throw new IllegalArgumentException("El nivel de habilidad debe estar entre 1 y 10");
        }

        Optional<Player> playerOpt = playerRepository.findByNameIgnoreCaseAndUser(name, user);
        if (playerOpt.isEmpty()) {
            throw new IllegalArgumentException("Jugador no encontrado");
        }

        Player player = playerOpt.get();
        int previousLevel = player.getSkillLevel();

        // Guarda el historial antes de cambiar el nivel
        PlayerLevelHistory history = new PlayerLevelHistory(
            player.getName(),
            previousLevel,
            skillLevel,
            java.time.LocalDateTime.now()
        );
        history.setUser(user);
        playerLevelHistoryRepository.save(history);

        player.setSkillLevel(skillLevel);
        return playerRepository.save(player);
    }
    
    /**
     * Actualiza un jugador existente.
     */
    public Player updatePlayer(Player player) {
        return playerRepository.save(player);
    }
    
    /**
     * Registra un gol para un jugador.
     */
    public void recordGoal(String playerId) {
        Optional<Player> playerOpt = playerRepository.findById(playerId);
        if (playerOpt.isPresent()) {
            Player player = playerOpt.get();
            player.setGoalsScored(player.getGoalsScored() + 1);
            playerRepository.save(player);
        }
    }
    
    /**
     * Incrementa partidos jugados de un jugador.
     */
    public void incrementGamesPlayed(String playerId) {
        Optional<Player> playerOpt = playerRepository.findById(playerId);
        if (playerOpt.isPresent()) {
            Player player = playerOpt.get();
            player.setGamesPlayed(player.getGamesPlayed() + 1);
            playerRepository.save(player);
        }
    }
    
    /**
     * Obtiene jugadores con deuda.
     */
    public List<Player> getPlayersWithDebt(User user) {
        return playerRepository.findPlayersWithDebtByUser(user);
    }
    
    /**
     * Obtiene los mejores goleadores.
     */
    public List<Player> getTopScorers(int limit, User user) {
        return playerRepository.findTop10ByUserOrderByGoalsScoredDesc(user);
    }
    
    /**
     * Marca la asistencia de un jugador.
     */
    public void markAttendance(String playerName, boolean attended, User user) {
        Optional<Player> playerOpt = playerRepository.findByNameIgnoreCaseAndUser(playerName, user);
        if (playerOpt.isEmpty()) {
            throw new IllegalArgumentException("Jugador no encontrado: " + playerName);
        }
        
        Player player = playerOpt.get();
        player.setAttended(attended);
        playerRepository.save(player);
    }
    
    /**
     * Desmarca la asistencia de todos los jugadores del usuario.
     */
    public int unmarkAllAttendance(User user) {
        List<Player> players = playerRepository.findByUser(user);
        int count = 0;
        for (Player player : players) {
            if (player.isAttended()) {
                player.setAttended(false);
                playerRepository.save(player);
                count++;
            }
        }
        return count;
    }
    
    /**
     * Obtiene el historial de niveles de habilidad de los jugadores.
     */
    public List<PlayerLevelHistoryDTO> getPlayerLevelHistory(User user) {
        List<PlayerLevelHistoryDTO> result = new ArrayList<>();
        List<Player> players = playerRepository.findAllByActivoTrueAndUser(user);

        for (Player player : players) {
            // Filtrar historial por usuario
            List<PlayerLevelHistory> history = playerLevelHistoryRepository
                .findByPlayerNameAndUserOrderByDateAsc(player.getName(), user);

            int previousLevel;
            double averageLevel;

            if (history.size() >= 1) {
                // Nivel anterior: previousLevel del último cambio (nivel antes del cambio actual)
                previousLevel = history.get(history.size() - 1).getPreviousLevel();
                // Promedio: promedio de todos los newLevel del historial (incluyendo el actual)
                averageLevel = history.stream()
                    .mapToInt(PlayerLevelHistory::getNewLevel)
                    .average()
                    .orElse(player.getSkillLevel());
            } else {
                // Si no hay historial, usar el nivel actual como referencia
                previousLevel = player.getSkillLevel();
                averageLevel = player.getSkillLevel();
            }

            PlayerLevelHistoryDTO dto = new PlayerLevelHistoryDTO(
                player.getName(),
                previousLevel,
                averageLevel
            );
            result.add(dto);
        }
        return result;
    }
}
