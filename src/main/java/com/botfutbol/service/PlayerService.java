package com.botfutbol.service;

import com.botfutbol.dto.PlayerDTO;
import com.botfutbol.entity.Player;
import com.botfutbol.entity.User;
import com.botfutbol.repository.PlayerRepository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    
    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }
    
    /**
     * Agrega un nuevo jugador.
     */
    @CacheEvict(value = {"players", "topScorers"}, allEntries = true)
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
     * Optimizado: usa caché para reducir consultas a la base de datos.
     */
    @Cacheable(value = "players", key = "#user.id")
    public List<Player> getAllPlayers(User user) {
        return playerRepository.findByUser(user);
    }
    
    /**
     * Elimina un jugador por nombre.
     */
    @CacheEvict(value = {"players", "topScorers"}, allEntries = true)
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
    @CacheEvict(value = {"players", "topScorers"}, allEntries = true)
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
     * Optimizado: usa batch update para múltiples jugadores.
     */
    @CacheEvict(value = "players", allEntries = true)
    public void incrementGamesPlayed(String playerId) {
        Optional<Player> playerOpt = playerRepository.findById(playerId);
        if (playerOpt.isPresent()) {
            Player player = playerOpt.get();
            player.setGamesPlayed(player.getGamesPlayed() + 1);
            playerRepository.save(player);
        }
    }
    
    /**
     * Incrementa partidos jugados para múltiples jugadores (optimizado).
     */
    @CacheEvict(value = "players", allEntries = true)
    public void incrementGamesPlayedBatch(List<String> playerIds) {
        List<Player> players = playerRepository.findAllById(playerIds);
        for (Player player : players) {
            player.setGamesPlayed(player.getGamesPlayed() + 1);
        }
        playerRepository.saveAll(players);
    }
    
    /**
     * Obtiene jugadores con deuda.
     */
    public List<Player> getPlayersWithDebt(User user) {
        return playerRepository.findPlayersWithDebtByUser(user);
    }
    
    /**
     * Obtiene los mejores goleadores.
     * Optimizado: usa caché para reducir consultas a la base de datos.
     */
    @Cacheable(value = "topScorers", key = "#user.id")
    public List<Player> getTopScorers(int limit, User user) {
        return playerRepository.findTop10ByUserOrderByGoalsScoredDesc(user);
    }
    
    /**
     * Marca la asistencia de un jugador.
     */
    @CacheEvict(value = "players", allEntries = true)
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
     * Optimizado: usa actualización masiva en lugar de N saves individuales.
     */
    @CacheEvict(value = "players", allEntries = true)
    public int unmarkAllAttendance(User user) {
        return playerRepository.unmarkAllAttendanceByUser(user);
    }
    
}
