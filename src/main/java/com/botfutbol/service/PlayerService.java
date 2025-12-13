package com.botfutbol.service;

import com.botfutbol.dto.PlayerDTO;
import com.botfutbol.dto.PlayerLevelHistoryDTO;
import com.botfutbol.entity.Player;
import com.botfutbol.entity.PlayerLevelHistory;
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
    public Player addPlayer(PlayerDTO playerDTO) {
        Player player = new Player();
        player.setName(playerDTO.getName());
        player.setSkillLevel(playerDTO.getSkillLevel() != null ? playerDTO.getSkillLevel() : 5);
        player.setPosition(playerDTO.getPosition() != null ? playerDTO.getPosition() : "MED");
        player.setTotalDebt(0);
        player.setTotalPaid(0);
        player.setGamesPlayed(0);
        player.setGoalsScored(0);
        player.setAttended(false);
        return playerRepository.save(player);
    }
    
    /**
     * Busca un jugador por nombre.
     */
    public Optional<Player> findPlayerByName(String name) {
        return playerRepository.findByNameIgnoreCase(name);
    }
    
    /**
     * Obtiene todos los jugadores.
     */
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }
    
    /**
     * Elimina un jugador por nombre.
     */
    public boolean removePlayer(String name) {
        Optional<Player> player = playerRepository.findByNameIgnoreCase(name);
        if (player.isPresent()) {
            playerRepository.delete(player.get());
            return true;
        }
        return false;
    }
    
    /**
     * Actualiza el nivel de habilidad de un jugador.
     */
    public Player updateSkillLevel(String name, int skillLevel) {
        if (skillLevel < 1 || skillLevel > 10) {
            throw new IllegalArgumentException("El nivel de habilidad debe estar entre 1 y 10");
        }

        Optional<Player> playerOpt = playerRepository.findByNameIgnoreCase(name);
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
    public List<Player> getPlayersWithDebt() {
        return playerRepository.findPlayersWithDebt();
    }
    
    /**
     * Obtiene los mejores goleadores.
     */
    public List<Player> getTopScorers(int limit) {
        return playerRepository.findTop10ByOrderByGoalsScoredDesc();
    }
    
    /**
     * Marca la asistencia de un jugador.
     */
    public void markAttendance(String playerName, boolean attended) {
        Optional<Player> playerOpt = playerRepository.findByNameIgnoreCase(playerName);
        if (playerOpt.isEmpty()) {
            throw new IllegalArgumentException("Jugador no encontrado: " + playerName);
        }
        
        Player player = playerOpt.get();
        player.setAttended(attended);
        playerRepository.save(player);
    }
    
    /**
     * Obtiene el historial de niveles de habilidad de los jugadores.
     */
    public List<PlayerLevelHistoryDTO> getPlayerLevelHistory() {
        List<PlayerLevelHistoryDTO> result = new ArrayList<>();
        List<Player> players = getAllPlayers(); // Usa tu método real para obtener jugadores

        for (Player player : players) {
            int previousLevel = player.getSkillLevel(); // O el nivel anterior real si tienes historial
            double averageLevel = player.getSkillLevel(); // Calcula el promedio real si tienes historial
            int suggestedLevel = player.getSkillLevel(); // Aplica tu lógica para sugerido

            PlayerLevelHistoryDTO dto = new PlayerLevelHistoryDTO(
                player.getName(),
                previousLevel,
                averageLevel,
                suggestedLevel
            );
            result.add(dto);
        }
        return result;
    }
}
