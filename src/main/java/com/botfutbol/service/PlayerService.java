package com.botfutbol.service;

import com.botfutbol.constants.PlayerConstants;
import com.botfutbol.dto.PlayerDTO;
import com.botfutbol.entity.Player;
import com.botfutbol.entity.User;
import com.botfutbol.repository.PlayerRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private static final Logger logger = LoggerFactory.getLogger(PlayerService.class);
    
    private final PlayerRepository playerRepository;
    
    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }
    
    /**
     * Agrega un nuevo jugador.
     */
    @CacheEvict(value = {"players", "topScorers"}, allEntries = true)
    public Player addPlayer(PlayerDTO playerDTO, User user) {
        logger.info("Agregando jugador: {} para usuario: {}", playerDTO.getName(), user.getId());
        
        // Verificar si el jugador ya existe (case-insensitive)
        Optional<Player> existingPlayer = playerRepository.findByNameIgnoreCaseAndUser(playerDTO.getName(), user);
        if (existingPlayer.isPresent()) {
            Player player = existingPlayer.get();
            logger.warn("Jugador '{}' ya existe para usuario: {}. Retornando jugador existente.", 
                    playerDTO.getName(), user.getId());
            // Si está inactivo, reactivarlo
            if (!player.isActivo()) {
                player.setActivo(true);
                player = playerRepository.save(player);
            }
            return player;
        }
        
        Player player = new Player();
        // Normalizar nombre: primera letra mayúscula, resto minúsculas
        String normalizedName = normalizePlayerName(playerDTO.getName());
        player.setName(normalizedName);
        player.setSkillLevel(playerDTO.getSkillLevel() != null ? 
                playerDTO.getSkillLevel() : PlayerConstants.DEFAULT_SKILL_LEVEL);
        player.setPosition(playerDTO.getPosition() != null ? 
                playerDTO.getPosition() : PlayerConstants.DEFAULT_POSITION);
        player.setTotalDebt(PlayerConstants.DEFAULT_TOTAL_DEBT);
        player.setTotalPaid(PlayerConstants.DEFAULT_TOTAL_PAID);
        player.setGamesPlayed(PlayerConstants.DEFAULT_GAMES_PLAYED);
        player.setGoalsScored(PlayerConstants.DEFAULT_GOALS_SCORED);
        player.setAttended(PlayerConstants.DEFAULT_ATTENDED);
        player.setUser(user);
        
        try {
            Player savedPlayer = playerRepository.save(player);
            logger.info("Jugador agregado exitosamente con ID: {}", savedPlayer.getId());
            return savedPlayer;
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Si falla por duplicado (race condition), buscar y retornar el existente
            logger.warn("Error de integridad al agregar jugador '{}'. Buscando jugador existente.", 
                    playerDTO.getName());
            Optional<Player> existing = playerRepository.findByNameIgnoreCaseAndUser(playerDTO.getName(), user);
            if (existing.isPresent()) {
                Player existingPlayer2 = existing.get();
                if (!existingPlayer2.isActivo()) {
                    existingPlayer2.setActivo(true);
                    existingPlayer2 = playerRepository.save(existingPlayer2);
                }
                return existingPlayer2;
            }
            throw new IllegalStateException("Error al agregar jugador: ya existe un jugador con ese nombre", e);
        }
    }
    
    /**
     * Normaliza el nombre del jugador (primera letra mayúscula, resto minúsculas)
     */
    private String normalizePlayerName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return name;
        }
        String trimmed = name.trim();
        if (trimmed.length() == 1) {
            return trimmed.toUpperCase();
        }
        return trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1).toLowerCase();
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
        logger.info("Actualizando nivel de habilidad de jugador: {} a nivel: {}", name, skillLevel);
        
        if (skillLevel < PlayerConstants.MIN_SKILL_LEVEL || 
            skillLevel > PlayerConstants.MAX_SKILL_LEVEL) {
            logger.warn("Nivel de habilidad inválido: {} (debe estar entre {} y {})", 
                    skillLevel, PlayerConstants.MIN_SKILL_LEVEL, PlayerConstants.MAX_SKILL_LEVEL);
            throw new IllegalArgumentException(PlayerConstants.ERROR_SKILL_LEVEL_RANGE);
        }

        Optional<Player> playerOpt = playerRepository.findByNameIgnoreCaseAndUser(name, user);
        if (playerOpt.isEmpty()) {
            logger.warn("Jugador no encontrado: {} para usuario: {}", name, user.getId());
            throw new IllegalArgumentException(PlayerConstants.ERROR_PLAYER_NOT_FOUND + ": " + name);
        }

        Player player = playerOpt.get();
        player.setSkillLevel(skillLevel);
        Player updatedPlayer = playerRepository.save(player);
        logger.info("Nivel de habilidad actualizado exitosamente para jugador: {}", name);
        return updatedPlayer;
    }
    
    /**
     * Actualiza un jugador existente.
     */
    @CacheEvict(value = {"players", "topScorers"}, allEntries = true)
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
