package com.botfutbol.service;

import com.botfutbol.dto.*;
import com.botfutbol.dto.MatchSummaryDTO.TeamSummaryDTO;
import com.botfutbol.dto.MatchSummaryDTO.PaymentStatusDTO;
import com.botfutbol.entity.*;
import com.botfutbol.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para administrar partidos.
 * Responsabilidad: Lógica de negocio relacionada con partidos, goles y estadísticas.
 */
@Service
@Transactional
public class MatchService {
    
    private final MatchRepository matchRepository;
    private final GoalRepository goalRepository;
    private final PlayerRepository playerRepository;
    private final PaymentService paymentService;
    private final PlayerService playerService;
    
    public MatchService(MatchRepository matchRepository,
                        GoalRepository goalRepository,
                        PlayerRepository playerRepository,
                        PaymentService paymentService,
                        PlayerService playerService) {
        this.matchRepository = matchRepository;
        this.goalRepository = goalRepository;
        this.playerRepository = playerRepository;
        this.paymentService = paymentService;
        this.playerService = playerService;
    }
    
    /**
     * Inicia un nuevo partido.
     */
    public Match startMatch(Team teamA, Team teamB, double costPerPlayer) {
        Match match = new Match(teamA, teamB, costPerPlayer);
        matchRepository.save(match);
        
        // Incrementar partidos jugados y agregar deuda a cada jugador
        teamA.getPlayers().forEach(player -> {
            playerService.incrementGamesPlayed(player.getId());
            paymentService.addDebtToPlayer(player.getId(), costPerPlayer);
        });
        
        teamB.getPlayers().forEach(player -> {
            playerService.incrementGamesPlayed(player.getId());
            paymentService.addDebtToPlayer(player.getId(), costPerPlayer);
        });
        
        return match;
    }
    
    /**
     * Registra un gol en el partido actual.
     */
    public Goal registerGoal(GoalDTO goalDTO) {
        // Verificar que hay un partido activo
        Optional<Match> matchOpt = getActiveMatch();
        if (matchOpt.isEmpty()) {
            throw new IllegalStateException("No hay un partido activo");
        }
        
        Match match = matchOpt.get();
        
        // Buscar el jugador
        Optional<Player> playerOpt = playerRepository.findByNameIgnoreCase(goalDTO.getPlayerName());
        if (playerOpt.isEmpty()) {
            throw new IllegalArgumentException("Jugador no encontrado: " + goalDTO.getPlayerName());
        }
        
        Player player = playerOpt.get();
        
        // Crear el gol
        Goal goal = new Goal(
                player.getId(),
                player.getName(),
                goalDTO.getTeamId(),
                match.getId()
        );
        
        // Actualizar estadísticas del jugador
        playerService.recordGoal(player.getId());
        
        // Actualizar goles del equipo
        if (goalDTO.getTeamId().equals(match.getTeamA().getId())) {
            match.getTeamA().setGoals(match.getTeamA().getGoals() + 1);
        } else if (goalDTO.getTeamId().equals(match.getTeamB().getId())) {
            match.getTeamB().setGoals(match.getTeamB().getGoals() + 1);
        }
        
        matchRepository.save(match);
        return goalRepository.save(goal);
    }
    
    /**
     * Obtiene el partido actual.
     */
    public Optional<Match> getCurrentMatch() {
        return matchRepository.findFirstByActiveTrue();
    }
    
    /**
     * Finaliza el partido actual.
     */
    public void endMatch() {
        Optional<Match> matchOpt = matchRepository.findFirstByActiveTrue();
        if (matchOpt.isPresent()) {
            Match match = matchOpt.get();
            match.setActive(false);
            matchRepository.save(match);
        }
    }
    
    /**
     * Obtiene estadísticas generales.
     */
    public StatsDTO getStats() {
        StatsDTO stats = new StatsDTO();
        
        // Top goleadores
        List<Player> topScorers = playerService.getTopScorers(5);
        List<PlayerStatsDTO> topScorersDTOs = topScorers.stream()
                .map(p -> new PlayerStatsDTO(p.getName(), p.getGoalsScored(), p.getGamesPlayed(), 0))
                .collect(Collectors.toList());
        stats.setTopScorers(topScorersDTOs);
        
        // Deudores
        List<Player> debtors = playerService.getPlayersWithDebt();
        List<PlayerDebtDTO> debtorsDTOs = debtors.stream()
                .map(p -> new PlayerDebtDTO(p.getName(), p.getTotalDebt(), p.getTotalPaid()))
                .collect(Collectors.toList());
        stats.setDebtors(debtorsDTOs);
        
        // Totales
        stats.setTotalPlayers((int) playerRepository.count());
        stats.setTotalGoals((int) goalRepository.count());
        
        double totalDebt = debtors.stream()
                .mapToDouble(p -> p.getTotalDebt() - p.getTotalPaid())
                .sum();
        stats.setTotalDebt(totalDebt);
        
        return stats;
    }
    
    /**
     * Obtiene el resultado del partido actual.
     */
    public String getCurrentMatchScore() {
        Optional<Match> matchOpt = matchRepository.findFirstByActiveTrue();
        if (matchOpt.isEmpty()) {
            return "No hay partido activo";
        }
        
        Match match = matchOpt.get();
        return String.format("%s %d - %d %s",
                match.getTeamA().getName(),
                match.getTeamA().getGoals(),
                match.getTeamB().getGoals(),
                match.getTeamB().getName());
    }
    
    /**
     * Obtiene todos los goles registrados.
     */
    public List<Goal> getAllGoals() {
        return goalRepository.findAll();
    }
    
    /**
     * Genera un resumen del partido para compartir en WhatsApp
     */
    public MatchSummaryDTO getMatchSummary(String matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalStateException("Partido no encontrado"));
        
        // Formatear fecha
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String matchDate = match.getDate().format(formatter);
        
        // Preparar equipos
        List<TeamSummaryDTO> teams = new ArrayList<>();
        
        TeamSummaryDTO teamADto = new TeamSummaryDTO(
            match.getTeamA().getName(),
            match.getTeamA().getPlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList())
        );
        
        TeamSummaryDTO teamBDto = new TeamSummaryDTO(
            match.getTeamB().getName(),
            match.getTeamB().getPlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList())
        );
        
        teams.add(teamADto);
        teams.add(teamBDto);
        
        // Preparar estado de pagos
        List<String> allMatchPlayers = new ArrayList<>();
        allMatchPlayers.addAll(match.getTeamA().getPlayers().stream()
            .map(Player::getName)
            .collect(Collectors.toList()));
        allMatchPlayers.addAll(match.getTeamB().getPlayers().stream()
            .map(Player::getName)
            .collect(Collectors.toList()));
        
        List<String> paidPlayers = new ArrayList<>();
        List<String> pendingPlayers = new ArrayList<>();
        
        for (String playerName : allMatchPlayers) {
            Optional<Player> playerOpt = playerRepository.findByNameIgnoreCase(playerName);
            if (playerOpt.isPresent()) {
                Player player = playerOpt.get();
                if (player.getTotalDebt() <= 0) {
                    paidPlayers.add(playerName);
                } else {
                    pendingPlayers.add(playerName);
                }
            }
        }
        
        PaymentStatusDTO paymentStatus = new PaymentStatusDTO(paidPlayers, pendingPlayers);
        
        return new MatchSummaryDTO(matchDate, teams, paymentStatus);
    }
    
    /**
     * Obtiene el partido activo actual.
     */
    public Optional<Match> getActiveMatch() {
        return matchRepository.findFirstByActiveTrue();
    }
}
