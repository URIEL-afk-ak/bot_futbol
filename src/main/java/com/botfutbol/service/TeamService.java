package com.botfutbol.service;

import com.botfutbol.dto.PlayerResponseDTO;
import com.botfutbol.dto.TeamDTO;
import com.botfutbol.entity.Player;
import com.botfutbol.entity.Team;
import com.botfutbol.entity.User;
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
    public List<Team> generateRandomTeams(User user) {
        // Filtrar solo jugadores que asistieron y pertenecen al usuario
        List<Player> allPlayers = playerRepository.findByUser(user).stream()
                .filter(Player::isAttended)
                .collect(Collectors.toList());
        
        if (allPlayers.size() < 2) {
            throw new IllegalStateException("Se necesitan al menos 2 jugadores que asistieron para formar equipos");
        }
        
        // Mezclar jugadores aleatoriamente
        Collections.shuffle(allPlayers);
        
        // Calcular tamaño de equipos
        int total = allPlayers.size();
        int teamSize = total / 2;

        // Equipos equitativos
        List<Player> teamAPlayers = allPlayers.subList(0, teamSize);
        List<Player> teamBPlayers = allPlayers.subList(teamSize, teamSize * 2);

        // Si hay un jugador impar, lo dejas afuera o lo asignas como suplente
        List<Player> suplentes = new ArrayList<>();
        if (total % 2 != 0) {
            suplentes.add(allPlayers.get(total - 1));
        }
        
        Team teamA = new Team("A", "Equipo A");
        teamAPlayers.forEach(teamA::addPlayer);
        
        Team teamB = new Team("B", "Equipo B");
        teamBPlayers.forEach(teamB::addPlayer);
        
        // Puedes devolver los suplentes en la respuesta si quieres
        return Arrays.asList(teamA, teamB);
    }
    
    /**
     * Genera dos equipos balanceados por nivel de habilidad.
     */
    public List<Team> generateBalancedTeams(User user) {
        // Filtrar solo jugadores que asistieron y pertenecen al usuario
        List<Player> allPlayers = playerRepository.findByUser(user).stream()
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
    
    /**
     * Genera equipos aleatorios desde una lista de usuarios confirmados.
     * Busca los jugadores asociados a cada usuario.
     */
    public List<Team> generateRandomTeamsFromUsers(List<User> confirmedUsers) {
        // Obtener jugadores de todos los usuarios confirmados
        List<Player> allPlayers = new ArrayList<>();
        for (User user : confirmedUsers) {
            List<Player> userPlayers = playerRepository.findByUser(user);
            // Si el usuario tiene jugadores, agregar el primero (o todos)
            if (!userPlayers.isEmpty()) {
                // Usar el primer jugador activo o el primero disponible
                Player player = userPlayers.stream()
                        .filter(Player::isActivo)
                        .findFirst()
                        .orElse(userPlayers.get(0));
                allPlayers.add(player);
            } else {
                // Si no tiene jugador, crear uno temporal con el nombre del usuario
                Player tempPlayer = new Player(user.getNombre() + " " + user.getApellido());
                tempPlayer.setUser(user);
                allPlayers.add(tempPlayer);
            }
        }
        
        if (allPlayers.size() < 2) {
            throw new IllegalStateException("Se necesitan al menos 2 usuarios confirmados para formar equipos");
        }
        
        // Mezclar jugadores aleatoriamente
        Collections.shuffle(allPlayers);
        
        // Calcular tamaño de equipos
        int total = allPlayers.size();
        int teamSize = total / 2;
        
        // Equipos equitativos
        List<Player> teamAPlayers = allPlayers.subList(0, teamSize);
        List<Player> teamBPlayers = allPlayers.subList(teamSize, teamSize * 2);
        
        Team teamA = new Team("A", "Equipo A");
        teamAPlayers.forEach(teamA::addPlayer);
        
        Team teamB = new Team("B", "Equipo B");
        teamBPlayers.forEach(teamB::addPlayer);
        
        return Arrays.asList(teamA, teamB);
    }
    
    /**
     * Genera equipos balanceados por habilidad desde una lista de usuarios confirmados.
     */
    public List<Team> generateBalancedTeamsFromUsers(List<User> confirmedUsers) {
        // Obtener jugadores de todos los usuarios confirmados
        List<Player> allPlayers = new ArrayList<>();
        for (User user : confirmedUsers) {
            List<Player> userPlayers = playerRepository.findByUser(user);
            if (!userPlayers.isEmpty()) {
                Player player = userPlayers.stream()
                        .filter(Player::isActivo)
                        .findFirst()
                        .orElse(userPlayers.get(0));
                allPlayers.add(player);
            } else {
                Player tempPlayer = new Player(user.getNombre() + " " + user.getApellido());
                tempPlayer.setUser(user);
                allPlayers.add(tempPlayer);
            }
        }
        
        if (allPlayers.size() < 2) {
            throw new IllegalStateException("Se necesitan al menos 2 usuarios confirmados para formar equipos");
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
     * Genera equipos por posición desde una lista de usuarios confirmados.
     * Divide jugadores por posición (POR, DEF, MED, DEL) y distribuye equitativamente.
     */
    public List<Team> generateTeamsByPositionFromUsers(List<User> confirmedUsers, boolean balance) {
        // Obtener jugadores de todos los usuarios confirmados
        List<Player> allPlayers = new ArrayList<>();
        for (User user : confirmedUsers) {
            List<Player> userPlayers = playerRepository.findByUser(user);
            if (!userPlayers.isEmpty()) {
                Player player = userPlayers.stream()
                        .filter(Player::isActivo)
                        .findFirst()
                        .orElse(userPlayers.get(0));
                allPlayers.add(player);
            } else {
                Player tempPlayer = new Player(user.getNombre() + " " + user.getApellido());
                tempPlayer.setUser(user);
                allPlayers.add(tempPlayer);
            }
        }
        
        if (allPlayers.size() < 2) {
            throw new IllegalStateException("Se necesitan al menos 2 usuarios confirmados para formar equipos");
        }
        
        // Separar por posición
        List<Player> por = allPlayers.stream()
                .filter(p -> "POR".equals(p.getPosition()))
                .collect(Collectors.toList());
        List<Player> def = allPlayers.stream()
                .filter(p -> "DEF".equals(p.getPosition()))
                .collect(Collectors.toList());
        List<Player> med = allPlayers.stream()
                .filter(p -> "MED".equals(p.getPosition()))
                .collect(Collectors.toList());
        List<Player> del = allPlayers.stream()
                .filter(p -> "DEL".equals(p.getPosition()))
                .collect(Collectors.toList());
        
        // Función para dividir una lista en dos equipos
        java.util.function.Function<List<Player>, List<List<Player>>> split = (list) -> {
            List<Player> a = new ArrayList<>();
            List<Player> b = new ArrayList<>();
            
            if (balance) {
                // Ordenar por habilidad descendente
                List<Player> sorted = new ArrayList<>(list);
                sorted.sort((p1, p2) -> Integer.compare(p2.getSkillLevel(), p1.getSkillLevel()));
                for (int i = 0; i < sorted.size(); i++) {
                    if (i % 2 == 0) {
                        a.add(sorted.get(i));
                    } else {
                        b.add(sorted.get(i));
                    }
                }
            } else {
                // Aleatorio
                List<Player> shuffled = new ArrayList<>(list);
                Collections.shuffle(shuffled);
                for (int i = 0; i < shuffled.size(); i++) {
                    if (i % 2 == 0) {
                        a.add(shuffled.get(i));
                    } else {
                        b.add(shuffled.get(i));
                    }
                }
            }
            return Arrays.asList(a, b);
        };
        
        List<List<Player>> porSplit = split.apply(por);
        List<List<Player>> defSplit = split.apply(def);
        List<List<Player>> medSplit = split.apply(med);
        List<List<Player>> delSplit = split.apply(del);
        
        Team teamA = new Team("A", "Equipo A");
        Team teamB = new Team("B", "Equipo B");
        
        porSplit.get(0).forEach(teamA::addPlayer);
        porSplit.get(1).forEach(teamB::addPlayer);
        defSplit.get(0).forEach(teamA::addPlayer);
        defSplit.get(1).forEach(teamB::addPlayer);
        medSplit.get(0).forEach(teamA::addPlayer);
        medSplit.get(1).forEach(teamB::addPlayer);
        delSplit.get(0).forEach(teamA::addPlayer);
        delSplit.get(1).forEach(teamB::addPlayer);
        
        return Arrays.asList(teamA, teamB);
    }
}
