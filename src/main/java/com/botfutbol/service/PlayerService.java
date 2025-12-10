package com.botfutbol.service;

import com.botfutbol.dto.PlayerDTO;
import com.botfutbol.entity.Player;
import com.botfutbol.repository.PlayerRepository;

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
    public Player addPlayer(PlayerDTO playerDTO) {
        // Validar que el nombre no esté vacío
        if (playerDTO.getName() == null || playerDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del jugador no puede estar vacío");
        }
        
        // Verificar que no exista un jugador con el mismo nombre
        Optional<Player> existing = playerRepository.findByNameIgnoreCase(playerDTO.getName());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Ya existe un jugador con ese nombre");
        }
        
        // Crear el jugador
        Player player = new Player(playerDTO.getName());
        
        // Asignar nivel de habilidad si se proporciona
        if (playerDTO.getSkillLevel() != null) {
            if (playerDTO.getSkillLevel() < 1 || playerDTO.getSkillLevel() > 10) {
                throw new IllegalArgumentException("El nivel de habilidad debe estar entre 1 y 10");
            }
            player.setSkillLevel(playerDTO.getSkillLevel());
        }
        
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
}
