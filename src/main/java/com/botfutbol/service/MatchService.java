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
     * Registra un gol solo por equipo (sin jugador específico) - Para fútbol 5/7 con reconocimiento de voz.
     */
    @CacheEvict(value = {"activeMatch", "stats", "topScorers"}, allEntries = true)
    public Goal registerGoalByTeam(String teamId, User user) {
        logger.info("Registrando gol por equipo: {} para usuario: {}", teamId, user.getId());
        
        // Verificar que hay un partido activo
        Optional<Match> matchOpt = getActiveMatch(user);
        if (matchOpt.isEmpty()) {
            logger.warn("Intento de registrar gol sin partido activo para usuario: {}", user.getId());
            throw new IllegalStateException("No hay un partido activo");
        }
        
        Match match = matchOpt.get();
        
        // Validar que el teamId corresponde a uno de los equipos del partido
        if (!teamId.equals(match.getTeamA().getId()) && !teamId.equals(match.getTeamB().getId())) {
            logger.warn("TeamId inválido: {} para partido: {}", teamId, match.getId());
            throw new IllegalArgumentException("Equipo no válido para este partido");
        }
        
        // Crear el gol sin jugador específico (para fútbol 5/7)
        Goal goal = new Goal(
                "VOZ", // ID especial para goles registrados por voz
                "Gol por voz", // Nombre genérico
                teamId,
                match.getId()
        );
        goal.setUser(user);
        
        // Actualizar goles del equipo
        if (teamId.equals(match.getTeamA().getId())) {
            match.getTeamA().setGoals(match.getTeamA().getGoals() + 1);
        } else if (teamId.equals(match.getTeamB().getId())) {
            match.getTeamB().setGoals(match.getTeamB().getGoals() + 1);
        }
        
        matchRepository.save(match);
        Goal savedGoal = goalRepository.save(goal);
        logger.info("Gol registrado exitosamente por voz con ID: {}", savedGoal.getId());
        return savedGoal;
    }
    
    /**
     * Deshace el último gol registrado en el partido activo.
     */
    @CacheEvict(value = {"activeMatch", "stats", "topScorers"}, allEntries = true)
    public boolean undoLastGoal(User user) {
        logger.info("Deshaciendo último gol para usuario: {}", user.getId());
        
        // Verificar que hay un partido activo
        Optional<Match> matchOpt = getActiveMatch(user);
        if (matchOpt.isEmpty()) {
            logger.warn("Intento de deshacer gol sin partido activo para usuario: {}", user.getId());
            throw new IllegalStateException("No hay un partido activo");
        }
        
        Match match = matchOpt.get();
        
        // Obtener el último gol del partido
        List<Goal> lastGoals = goalRepository.findTop1ByMatchIdAndUserOrderByTimestampDesc(match.getId(), user);
        if (lastGoals.isEmpty()) {
            logger.warn("No hay goles para deshacer en partido: {}", match.getId());
            return false;
        }
        
        Goal lastGoal = lastGoals.get(0);
        String teamId = lastGoal.getTeamId();
        
        // Actualizar goles del equipo (reducir en 1)
        if (teamId.equals(match.getTeamA().getId())) {
            int currentGoals = match.getTeamA().getGoals();
            if (currentGoals > 0) {
                match.getTeamA().setGoals(currentGoals - 1);
            }
        } else if (teamId.equals(match.getTeamB().getId())) {
            int currentGoals = match.getTeamB().getGoals();
            if (currentGoals > 0) {
                match.getTeamB().setGoals(currentGoals - 1);
            }
        }
        
        // Si el gol tenía un jugador asociado, actualizar sus estadísticas
        if (lastGoal.getPlayerId() != null && !lastGoal.getPlayerId().equals("VOZ")) {
            Optional<Player> playerOpt = playerRepository.findById(lastGoal.getPlayerId());
            if (playerOpt.isPresent()) {
                Player player = playerOpt.get();
                if (player.getGoalsScored() > 0) {
                    player.setGoalsScored(player.getGoalsScored() - 1);
                    playerRepository.save(player);
                }
            }
        }
        
        // Eliminar el gol
        goalRepository.delete(lastGoal);
        matchRepository.save(match);
        
        logger.info("Último gol deshecho exitosamente. Gol ID: {}", lastGoal.getId());
        return true;
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
     * Obtiene información del partido activo para reconocimiento de voz.
     */
    public com.botfutbol.dto.ActiveMatchDTO getActiveMatchInfo(User user) {
        Optional<Match> matchOpt = getActiveMatch(user);
        if (matchOpt.isEmpty()) {
            return new com.botfutbol.dto.ActiveMatchDTO();
        }
        
        Match match = matchOpt.get();
        return new com.botfutbol.dto.ActiveMatchDTO(
                match.getId(),
                match.getTeamA().getId(),
                match.getTeamB().getId(),
                match.getTeamA().getName(),
                match.getTeamB().getName(),
                match.getTeamA().getGoals(),
                match.getTeamB().getGoals()
        );
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
