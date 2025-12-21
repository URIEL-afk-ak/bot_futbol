package com.botfutbol.service;

import com.botfutbol.dto.*;
import com.botfutbol.dto.MatchSummaryDTO.TeamSummaryDTO;
import com.botfutbol.dto.MatchSummaryDTO.PaymentStatusDTO;
import com.botfutbol.entity.*;
import com.botfutbol.repository.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    
    private static final Logger logger = LoggerFactory.getLogger(MatchService.class);
    
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
    @CacheEvict(value = {"activeMatch", "stats"}, allEntries = true)
    public Match startMatch(Team teamA, Team teamB, double costPerPlayer, User user) {
        logger.info("Iniciando partido para usuario: {} con costo por jugador: ${}", user.getId(), costPerPlayer);
        
        Match match = new Match(teamA, teamB, costPerPlayer);
        match.setUser(user);
        Match savedMatch = matchRepository.save(match);
        logger.info("Partido iniciado exitosamente con ID: {}", savedMatch.getId());
        
        // Optimizado: procesar todos los jugadores en batch
        List<String> allPlayerIds = new ArrayList<>();
        teamA.getPlayers().forEach(player -> allPlayerIds.add(player.getId()));
        teamB.getPlayers().forEach(player -> allPlayerIds.add(player.getId()));
        
        // Incrementar partidos jugados en batch
        playerService.incrementGamesPlayedBatch(allPlayerIds);
        
        // Agregar deuda a cada jugador (mantener individual por lógica de negocio)
        teamA.getPlayers().forEach(player -> {
            paymentService.addDebtToPlayer(player.getId(), costPerPlayer);
        });
        
        teamB.getPlayers().forEach(player -> {
            paymentService.addDebtToPlayer(player.getId(), costPerPlayer);
        });
        
        logger.info("Partido iniciado con {} jugadores en total", allPlayerIds.size());
        return savedMatch;
    }
    
    /**
     * Registra un gol en el partido actual.
     */
    @CacheEvict(value = {"activeMatch", "stats", "topScorers"}, allEntries = true)
    public Goal registerGoal(GoalDTO goalDTO, User user) {
        logger.info("Registrando gol de jugador: {} para usuario: {}", goalDTO.getPlayerName(), user.getId());
        
        // Verificar que hay un partido activo
        Optional<Match> matchOpt = getActiveMatch(user);
        if (matchOpt.isEmpty()) {
            logger.warn("Intento de registrar gol sin partido activo para usuario: {}", user.getId());
            throw new IllegalStateException("No hay un partido activo");
        }
        
        Match match = matchOpt.get();
        
        // Buscar el jugador
        Optional<Player> playerOpt = playerRepository.findByNameIgnoreCaseAndUser(goalDTO.getPlayerName(), user);
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
        goal.setUser(user);
        
        // Actualizar estadísticas del jugador
        playerService.recordGoal(player.getId());
        
        // Actualizar goles del equipo
        if (goalDTO.getTeamId().equals(match.getTeamA().getId())) {
            match.getTeamA().setGoals(match.getTeamA().getGoals() + 1);
        } else if (goalDTO.getTeamId().equals(match.getTeamB().getId())) {
            match.getTeamB().setGoals(match.getTeamB().getGoals() + 1);
        }
        
        matchRepository.save(match);
        Goal savedGoal = goalRepository.save(goal);
        logger.info("Gol registrado exitosamente con ID: {}", savedGoal.getId());
        return savedGoal;
    }
    
    /**
     * Obtiene el partido activo.
     * Optimizado: usa caché para reducir consultas a la base de datos.
     */
    @Cacheable(value = "activeMatch", key = "#user.id")
    public Optional<Match> getActiveMatch(User user) {
        return matchRepository.findFirstByActiveTrueAndUser(user);
    }
    
    /**
     * Obtiene el partido actual (alias para compatibilidad).
     */
    public Optional<Match> getCurrentMatch(User user) {
        return getActiveMatch(user);
    }
    
    /**
     * Finaliza el partido actual.
     */
    @CacheEvict(value = {"activeMatch", "stats"}, allEntries = true)
    public void endMatch(User user) {
        Optional<Match> matchOpt = matchRepository.findFirstByActiveTrueAndUser(user);
        if (matchOpt.isPresent()) {
            Match match = matchOpt.get();
            match.setActive(false);
            matchRepository.save(match);
        }
    }
    
    /**
     * Obtiene estadísticas generales.
     * Optimizado: usa caché para reducir consultas a la base de datos.
     */
    @Cacheable(value = "stats", key = "#user.id")
    public StatsDTO getStats(User user) {
        StatsDTO stats = new StatsDTO();
        
        // Top goleadores
        List<Player> topScorers = playerService.getTopScorers(5, user);
        List<PlayerStatsDTO> topScorersDTOs = topScorers.stream()
                .map(p -> new PlayerStatsDTO(p.getName(), p.getGoalsScored(), p.getGamesPlayed(), 0))
                .collect(Collectors.toList());
        stats.setTopScorers(topScorersDTOs);
        
        // Deudores
        List<Player> debtors = playerService.getPlayersWithDebt(user);
        List<PlayerDebtDTO> debtorsDTOs = debtors.stream()
                .map(p -> new PlayerDebtDTO(p.getName(), p.getTotalDebt(), p.getTotalPaid()))
                .collect(Collectors.toList());
        stats.setDebtors(debtorsDTOs);
        
        // Totales
        long totalPlayers = playerRepository.findByUser(user).size();
        long totalGoals = goalRepository.findByUser(user).size();
        stats.setTotalPlayers((int) totalPlayers);
        stats.setTotalGoals((int) totalGoals);
        
        double totalDebt = debtors.stream()
                .mapToDouble(p -> p.getTotalDebt() - p.getTotalPaid())
                .sum();
        stats.setTotalDebt(totalDebt);
        
        return stats;
    }
    
    /**
     * Obtiene el resultado del partido actual.
     */
    public String getCurrentMatchScore(User user) {
        Optional<Match> matchOpt = getActiveMatch(user);
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
    public List<Goal> getAllGoals(User user) {
        return goalRepository.findByUser(user);
    }
    
    /**
     * Genera un resumen del partido para compartir en WhatsApp
     */
    public MatchSummaryDTO getMatchSummary(String matchId, User user) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalStateException("Partido no encontrado"));
        
        // Verificar que el partido pertenece al usuario
        if (match.getUser() == null || !match.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("Partido no encontrado o no autorizado");
        }
        
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
            Optional<Player> playerOpt = playerRepository.findByNameIgnoreCaseAndUser(playerName, user);
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
}
