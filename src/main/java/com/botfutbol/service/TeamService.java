package com.botfutbol.service;

import com.botfutbol.dto.PlayerResponseDTO;
import com.botfutbol.dto.TeamDTO;
import com.botfutbol.entity.Player;
import com.botfutbol.entity.Team;
import com.botfutbol.repository.PlayerRepository;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para generar y administrar equipos.
 * Responsabilidad: Lógica para armar equipos balanceados o aleatorios.
 */
@Service
public class TeamService {
    
    private final PlayerRepository playerRepository;
    
    public TeamService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }
    
    /**
     * Genera dos equipos de forma aleatoria.
     */
    public List<Team> generateRandomTeams() {
        // Filtrar solo jugadores que asistieron
        List<Player> allPlayers = playerRepository.findAll().stream()
                .filter(Player::isAttended)
                .collect(Collectors.toList());
        
        if (allPlayers.size() < 2) {
            throw new IllegalStateException("Se necesitan al menos 2 jugadores que asistieron para formar equipos");
        }
        
        // Mezclar jugadores aleatoriamente
        Collections.shuffle(allPlayers);
        
        // Dividir en dos equipos
        int mid = allPlayers.size() / 2;
        List<Player> teamAPlayers = allPlayers.subList(0, mid);
        List<Player> teamBPlayers = allPlayers.subList(mid, allPlayers.size());
        
        Team teamA = new Team("A", "Equipo A");
        teamAPlayers.forEach(teamA::addPlayer);
        
        Team teamB = new Team("B", "Equipo B");
        teamBPlayers.forEach(teamB::addPlayer);
        
        return Arrays.asList(teamA, teamB);
    }
    
    /**
     * Genera dos equipos balanceados por nivel de habilidad.
     */
    public List<Team> generateBalancedTeams() {
        // Filtrar solo jugadores que asistieron
        List<Player> allPlayers = playerRepository.findAll().stream()
                .filter(Player::isAttended)
                .collect(Collectors.toList());
        
        if (allPlayers.size() < 2) {
            throw new IllegalStateException("Se necesitan al menos 2 jugadores que asistieron para formar equipos");
        }
        
        // Ordenar jugadores por habilidad (descendente)
        allPlayers.sort((p1, p2) -> Integer.compare(p2.getSkillLevel(), p1.getSkillLevel()));
        
        Team teamA = new Team("A", "Equipo A");
        Team teamB = new Team("B", "Equipo B");
        
        // Asignar jugadores alternando para equilibrar
        for (int i = 0; i < allPlayers.size(); i++) {
            if (i % 2 == 0) {
                teamA.addPlayer(allPlayers.get(i));
            } else {
                teamB.addPlayer(allPlayers.get(i));
            }
        }
        
        return Arrays.asList(teamA, teamB);
    }
    
    /**
     * Convierte un equipo a DTO.
     */
    public TeamDTO convertToDTO(Team team) {
        List<PlayerResponseDTO> playerDTOs = team.getPlayers().stream()
                .map(this::convertPlayerToDTO)
                .collect(Collectors.toList());
        
        TeamDTO dto = new TeamDTO(
                team.getId(),
                team.getName(),
                playerDTOs,
                team.getTotalSkillLevel()
        );
        dto.setGoals(team.getGoals());
        return dto;
    }
    
    /**
     * Convierte un Player a PlayerResponseDTO.
     */
    private PlayerResponseDTO convertPlayerToDTO(Player player) {
        return new PlayerResponseDTO(
                player.getName(),
                player.getSkillLevel(),
                player.getGoalsScored(),
                player.getGamesPlayed(),
                (long) player.getTotalPaid(),
                (long) player.getTotalDebt(),
                player.isAttended()
        );
    }
    
    /**
     * Convierte lista de equipos a DTOs.
     */
    public List<TeamDTO> convertToDTOs(List<Team> teams) {
        return teams.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
