package com.botfutbol.controller;

import com.botfutbol.dto.*;
import com.botfutbol.entity.Goal;
import com.botfutbol.entity.Payment;
import com.botfutbol.entity.Player;
import com.botfutbol.entity.Team;
import com.botfutbol.entity.PlayerLevelHistory;
import com.botfutbol.entity.User;
import com.botfutbol.service.ChatParsingService;
import com.botfutbol.service.MatchService;
import com.botfutbol.service.PaymentService;
import com.botfutbol.service.PlayerService;
import com.botfutbol.service.TeamService;
import com.botfutbol.service.UserService;
import com.botfutbol.repository.PlayerLevelHistoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador principal del bot.
 * Responsabilidad: Recibir comandos del usuario, validarlos y llamar a los servicios.
 * NO contiene lógica de negocio.
 */
@RestController
@RequestMapping("/api/bot")
@CrossOrigin(origins = "*")
public class BotController {
    
    private final PlayerService playerService;
    private final TeamService teamService;
    private final PaymentService paymentService;
    private final MatchService matchService;
    private final ChatParsingService chatParsingService;
    private final PlayerLevelHistoryRepository playerLevelHistoryRepository;
    
    @Autowired
    private UserService userService;
    
    public BotController(PlayerService playerService,
                         TeamService teamService,
                         PaymentService paymentService,
                         MatchService matchService,
                         ChatParsingService chatParsingService,
                         PlayerLevelHistoryRepository playerLevelHistoryRepository) {
        this.playerService = playerService;
        this.teamService = teamService;
        this.paymentService = paymentService;
        this.matchService = matchService;
        this.chatParsingService = chatParsingService;
        this.playerLevelHistoryRepository = playerLevelHistoryRepository;
    }
    
    // ==================== REST API ENDPOINTS ====================
    
    /**
     * Obtener todos los jugadores
     */
    @GetMapping("/players")
    public ResponseEntity<List<Player>> getAllPlayers() {
        return ResponseEntity.ok(playerService.getAllPlayers());
    }
    
    /**
     * Agregar un jugador
     */
    @PostMapping("/player/add")
    public ResponseEntity<String> addPlayerRest(@RequestBody PlayerDTO dto) {
        Player player = playerService.addPlayer(dto);
        return ResponseEntity.ok(String.format("Jugador agregado: %s", player.getName()));
    }
    
    /**
     * Actualizar un jugador
     */
    @PutMapping("/player/update/{oldName}")
    public ResponseEntity<String> updatePlayer(@PathVariable String oldName, @RequestBody PlayerDTO dto) {
        try {
            Optional<Player> playerOpt = playerService.findPlayerByName(oldName);
            if (playerOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Jugador no encontrado: " + oldName);
            }
            Player player = playerOpt.get();
            player.setName(dto.getName());
            player.setSkillLevel(dto.getSkillLevel() != null ? dto.getSkillLevel() : player.getSkillLevel());
            player.setPosition(dto.getPosition() != null ? dto.getPosition() : player.getPosition());
            // NO toques player.setAttended(...) aquí, así se mantiene igual
            playerService.updatePlayer(player);
            return ResponseEntity.ok(String.format("Jugador actualizado: %s", player.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar: " + e.getMessage());
        }
    }
    
    /**
     * Eliminar un jugador
     */
    @DeleteMapping("/player/delete/{name}")
    public ResponseEntity<String> deletePlayer(@PathVariable String name) {
        boolean removed = playerService.removePlayer(name);
        if (removed) {
            return ResponseEntity.ok("Jugador eliminado: " + name);
        }
        return ResponseEntity.badRequest().body("Jugador no encontrado: " + name);
    }
    
    /**
     * Obtener jugadores con deuda
     */
    @GetMapping("/players/debt")
    public ResponseEntity<List<Player>> getPlayersWithDebt() {
        return ResponseEntity.ok(playerService.getPlayersWithDebt());
    }
    
    /**
     * Marcar asistencia de un jugador
     */
    @PutMapping("/player/attendance/{name}")
    public ResponseEntity<String> markAttendance(@PathVariable String name, @RequestParam boolean attended) {
        try {
            Optional<Player> playerOpt = playerService.findPlayerByName(name);
            if (playerOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Jugador no encontrado: " + name);
            }
            Player player = playerOpt.get();
            player.setAttended(attended);
            playerService.updatePlayer(player);
            return ResponseEntity.ok(String.format("Asistencia actualizada: %s", name));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Generar equipos aleatorios
     */
    @PostMapping("/teams/random")
    public ResponseEntity<Map<String, TeamDTO>> generateRandomTeamsRest() {
        List<Team> teams = teamService.generateRandomTeams();
        List<TeamDTO> dtos = teamService.convertToDTOs(teams);
        
        Map<String, TeamDTO> response = new HashMap<>();
        response.put("teamA", dtos.get(0));
        response.put("teamB", dtos.get(1));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Generar equipos balanceados
     */
    @PostMapping("/teams/balanced")
    public ResponseEntity<Map<String, TeamDTO>> generateBalancedTeamsRest() {
        List<Team> teams = teamService.generateBalancedTeams();
        List<TeamDTO> dtos = teamService.convertToDTOs(teams);
        
        Map<String, TeamDTO> response = new HashMap<>();
        response.put("teamA", dtos.get(0));
        response.put("teamB", dtos.get(1));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Registrar un gol
     */
    @PostMapping("/goal/record/{playerName}")
    public ResponseEntity<String> recordGoalRest(@PathVariable String playerName) {
        // Por defecto registra en equipo A, el frontend puede ampliarse para seleccionar equipo
        GoalDTO dto = new GoalDTO(playerName, "A");
        Goal goal = matchService.registerGoal(dto);
        return ResponseEntity.ok(String.format("Gol registrado para %s", goal.getPlayerName()));
    }
    
    /**
     * Obtener todos los goles
     */
    @GetMapping("/goals")
    public ResponseEntity<List<Goal>> getAllGoals() {
        return ResponseEntity.ok(matchService.getAllGoals());
    }
    
    /**
     * Registrar un pago
     */
    @PostMapping("/payment/record")
    public ResponseEntity<String> recordPaymentRest(@RequestBody PaymentDTO dto) {
        Payment payment = paymentService.registerPayment(dto);
        double debt = paymentService.getPlayerDebt(payment.getPlayerName());
        return ResponseEntity.ok(String.format("Pago registrado: $%.2f. Deuda restante: $%.2f", 
                payment.getAmount(), debt));
    }
    
    /**
     * Obtener todos los pagos
     */
    @GetMapping("/payments")
    public ResponseEntity<List<Payment>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }
    
    /**
     * Obtener estadísticas generales
     */
    @GetMapping("/stats")
    public ResponseEntity<StatsDTO> getStats() {
        return ResponseEntity.ok(matchService.getStats());
    }
    
    // ==================== COMANDO DE TEXTO ====================
    
    /**
     * Procesa un mensaje/comando del usuario.
     * Retorna la respuesta formateada.
     */
    @PostMapping("/message")
    public ResponseEntity<String> processMessage(@RequestBody String message) {
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("❌ Comando vacío");
        }
        
        message = message.trim();
        String[] parts = message.split("\\s+");
        String command = parts[0].toLowerCase();
        
        try {
            String response = switch (command) {
                case "/agregar", "/add" -> handleAddPlayer(parts);
                case "/eliminar", "/remove" -> handleRemovePlayer(parts);
                case "/lista", "/list" -> handleListPlayers();
                case "/equipos", "/teams" -> handleGenerateTeams(parts);
                case "/iniciar", "/start" -> handleStartMatch(parts);
                case "/gol", "/goal" -> handleRegisterGoal(parts);
                case "/resultado", "/score" -> handleGetScore();
                case "/finalizar", "/end" -> handleEndMatch();
                case "/pago", "/pay" -> handleRegisterPayment(parts);
                case "/deuda", "/debt" -> handleCheckDebt(parts);
                case "/stats", "/estadisticas" -> handleGetStats();
                case "/ayuda", "/help" -> handleHelp();
                default -> "❌ Comando no reconocido. Usa /ayuda para ver los comandos disponibles.";
            };
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Error: " + e.getMessage());
        }
    }
    
    /**
     * /agregar Juan [nivel]
     */
    private String handleAddPlayer(String[] parts) {
        if (parts.length < 2) {
            return "❌ Uso: /agregar <nombre> [nivel 1-10]";
        }
        
        String name = parts[1];
        Integer skillLevel = null;
        
        if (parts.length >= 3) {
            try {
                skillLevel = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                return "❌ El nivel debe ser un número entre 1 y 10";
            }
        }
        
        PlayerDTO dto = new PlayerDTO(name, skillLevel);
        Player player = playerService.addPlayer(dto);
        
        return String.format("✅ Jugador agregado: %s (Nivel: %d)", 
                player.getName(), player.getSkillLevel());
    }
    
    /**
     * /eliminar Juan
     */
    private String handleRemovePlayer(String[] parts) {
        if (parts.length < 2) {
            return "❌ Uso: /eliminar <nombre>";
        }
        
        String name = parts[1];
        boolean removed = playerService.removePlayer(name);
        
        if (removed) {
            return "✅ Jugador eliminado: " + name;
        } else {
            return "❌ Jugador no encontrado: " + name;
        }
    }
    
    /**
     * /lista
     */
    private String handleListPlayers() {
        List<Player> players = playerService.getAllPlayers();
        
        if (players.isEmpty()) {
            return "📋 No hay jugadores registrados";
        }
        
        StringBuilder sb = new StringBuilder("📋 Lista de jugadores:\n\n");
        for (Player player : players) {
            sb.append(String.format("• %s (Nivel: %d, Goles: %d, Partidos: %d)\n",
                    player.getName(),
                    player.getSkillLevel(),
                    player.getGoalsScored(),
                    player.getGamesPlayed()));
        }
        
        return sb.toString();
    }
    
    /**
     * /equipos [random|balanceado]
     */
    private String handleGenerateTeams(String[] parts) {
        boolean balanced = parts.length > 1 && parts[1].equalsIgnoreCase("balanceado");
        
        List<Team> teams = balanced ? 
                teamService.generateBalancedTeams() : 
                teamService.generateRandomTeams();
        
        List<TeamDTO> teamDTOs = teamService.convertToDTOs(teams);
        
        StringBuilder sb = new StringBuilder("⚽ Equipos generados:\n\n");
        for (TeamDTO team : teamDTOs) {
            sb.append(String.format("🔵 %s (Nivel total: %d)\n", 
                    team.getName(), team.getTotalSkillLevel()));
            for (String playerName : team.getPlayerNames()) {
                sb.append("  • ").append(playerName).append("\n");
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * /iniciar <costo>
     */
    private String handleStartMatch(String[] parts) {
        if (parts.length < 2) {
            return "❌ Uso: /iniciar <costo_por_jugador>";
        }
        
        double cost;
        try {
            cost = Double.parseDouble(parts[1]);
        } catch (NumberFormatException e) {
            return "❌ El costo debe ser un número válido";
        }
        
        // Generar equipos
        List<Team> teams = teamService.generateBalancedTeams();
        
        // Iniciar partido
        matchService.startMatch(teams.get(0), teams.get(1), cost);
        
        StringBuilder sb = new StringBuilder("🏁 ¡Partido iniciado!\n\n");
        sb.append(String.format("💰 Costo por jugador: $%.2f\n\n", cost));
        sb.append(formatTeams(teams));
        
        return sb.toString();
    }
    
    /**
     * /gol Juan A
     */
    private String handleRegisterGoal(String[] parts) {
        if (parts.length < 3) {
            return "❌ Uso: /gol <jugador> <equipo: A o B>";
        }
        
        String playerName = parts[1];
        String teamId = parts[2].toUpperCase();
        
        if (!teamId.equals("A") && !teamId.equals("B")) {
            return "❌ El equipo debe ser A o B";
        }
        
        GoalDTO dto = new GoalDTO(playerName, teamId);
        Goal goal = matchService.registerGoal(dto);
        
        String score = matchService.getCurrentMatchScore();
        
        return String.format("⚽ ¡GOL de %s!\n\n📊 %s", goal.getPlayerName(), score);
    }
    
    /**
     * /resultado
     */
    private String handleGetScore() {
        String score = matchService.getCurrentMatchScore();
        return "📊 " + score;
    }
    
    /**
     * /finalizar
     */
    private String handleEndMatch() {
        String score = matchService.getCurrentMatchScore();
        matchService.endMatch();
        return "🏁 Partido finalizado\n\n📊 Resultado final: " + score;
    }
    
    /**
     * /pago Juan 1500 [concepto]
     */
    private String handleRegisterPayment(String[] parts) {
        if (parts.length < 3) {
            return "❌ Uso: /pago <jugador> <monto> [concepto]";
        }
        
        String playerName = parts[1];
        double amount;
        
        try {
            amount = Double.parseDouble(parts[2]);
        } catch (NumberFormatException e) {
            return "❌ El monto debe ser un número válido";
        }
        
        String concept = parts.length > 3 ? String.join(" ", java.util.Arrays.copyOfRange(parts, 3, parts.length)) : null;
        
        PaymentDTO dto = new PaymentDTO(playerName, amount, concept);
        Payment payment = paymentService.registerPayment(dto);
        
        double debt = paymentService.getPlayerDebt(playerName);
        
        return String.format("✅ Pago registrado: %s pagó $%.2f\n💰 Deuda restante: $%.2f",
                payment.getPlayerName(), payment.getAmount(), debt);
    }
    
    /**
     * /deuda [jugador]
     */
    private String handleCheckDebt(String[] parts) {
        if (parts.length < 2) {
            // Mostrar todos los deudores
            List<Player> debtors = playerService.getPlayersWithDebt();
            
            if (debtors.isEmpty()) {
                return "✅ No hay deudores";
            }
            
            StringBuilder sb = new StringBuilder("💰 Deudores:\n\n");
            for (Player player : debtors) {
                double debt = player.getTotalDebt() - player.getTotalPaid();
                sb.append(String.format("• %s: $%.2f\n", player.getName(), debt));
            }
            return sb.toString();
        } else {
            // Mostrar deuda de un jugador específico
            String playerName = parts[1];
            double debt = paymentService.getPlayerDebt(playerName);
            
            if (debt > 0) {
                return String.format("💰 %s debe: $%.2f", playerName, debt);
            } else {
                return String.format("✅ %s no tiene deudas", playerName);
            }
        }
    }
    
    /**
     * /stats
     */
    private String handleGetStats() {
        StatsDTO stats = matchService.getStats();
        
        StringBuilder sb = new StringBuilder("📊 Estadísticas:\n\n");
        
     
        
        sb.append("\n💰 Deudores:\n");
        if (stats.getDebtors().isEmpty()) {
            sb.append("  No hay deudores\n");
        } else {
            for (PlayerDebtDTO debtor : stats.getDebtors()) {
                double debt = debtor.getDebt() - debtor.getPaid();
                sb.append(String.format("  • %s: $%.2f\n", debtor.getName(), debt));
            }
        }
        
        sb.append(String.format("\n📈 Totales:\n"));
        sb.append(String.format("  • Jugadores: %d\n", stats.getTotalPlayers()));
        sb.append(String.format("  • Goles: %d\n", stats.getTotalGoals()));
        sb.append(String.format("  • Deuda total: $%.2f\n", stats.getTotalDebt()));
        
        return sb.toString();
    }
    
    /**
     * /ayuda
     */
    private String handleHelp() {
        return "🤖 Comandos disponibles:\n\n" +
                "👥 Jugadores:\n" +
                "  /agregar <nombre> [nivel] - Agregar jugador\n" +
                "  /eliminar <nombre> - Eliminar jugador\n" +
                "  /lista - Ver todos los jugadores\n\n" +
                "⚽ Partido:\n" +
                "  /equipos [balanceado] - Generar equipos\n" +
                "  /iniciar <costo> - Iniciar partido\n" +
                "  /gol <jugador> <A|B> - Registrar gol\n" +
                "  /resultado - Ver marcador\n" +
                "  /finalizar - Finalizar partido\n\n" +
                "💰 Pagos:\n" +
                "  /pago <jugador> <monto> - Registrar pago\n" +
                "  /deuda [jugador] - Ver deudas\n\n" +
                "📊 Otros:\n" +
                "  /stats - Ver estadísticas\n" +
                "  /ayuda - Mostrar esta ayuda";
    }
    
    // ==================== NUEVAS FUNCIONALIDADES ====================
    
    /**
     * Importar jugadores y pagos desde texto de chat de WhatsApp
     */
    @PostMapping("/matches/import-from-text")
    public ResponseEntity<ChatParsingResponseDTO> importFromChat(@RequestBody Map<String, String> request) {
        try {
            String chatText = request.get("text");
            
            if (chatText == null || chatText.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            ChatParsingService.ChatParsingResult result = chatParsingService.processChatText(chatText);
            
            // Convertir a DTO
            ChatParsingResponseDTO response = new ChatParsingResponseDTO();
            response.setPlayersConfirmed(result.getPlayersConfirmed());
            response.setPaymentsRegistered(result.getPaymentsRegistered());
            response.setConfirmedPlayers(result.getConfirmedPlayers());
            response.setPaidPlayers(result.getPaidPlayers());
            response.setUnrecognizedMessages(result.getUnrecognizedMessages());
            response.setNewPlayersAdded(result.getNewPlayersAdded());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Obtener resumen del partido para compartir en WhatsApp
     */
    @GetMapping("/matches/{matchId}/summary")
    public ResponseEntity<MatchSummaryDTO> getMatchSummary(@PathVariable String matchId) {
        try {
            MatchSummaryDTO summary = matchService.getMatchSummary(matchId);
            return ResponseEntity.ok(summary);
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Obtener resumen del partido activo actual
     */
    @GetMapping("/matches/current/summary")
    public ResponseEntity<MatchSummaryDTO> getCurrentMatchSummary() {
        try {
            Optional<com.botfutbol.entity.Match> currentMatch = matchService.getActiveMatch();
            
            if (currentMatch.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            MatchSummaryDTO summary = matchService.getMatchSummary(currentMatch.get().getId());
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Formatea equipos para mostrar.
     */
    private String formatTeams(List<Team> teams) {
        StringBuilder sb = new StringBuilder();
        for (Team team : teams) {
            sb.append(String.format("🔵 %s:\n", team.getName()));
            for (Player player : team.getPlayers()) {
                sb.append(String.format("  • %s\n", player.getName()));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Actualizar un pago
     */
    @PutMapping("/payment/update/{id}")
    public ResponseEntity<?> updatePayment(@PathVariable String id, @RequestBody PaymentDTO dto) {
        try {
            Payment updated = paymentService.updatePayment(id, dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Error al editar pago: " + e.getMessage());
        }
    }

    /**
     * Eliminar un pago
     */
    @DeleteMapping("/payment/delete/{id}")
    public ResponseEntity<?> deletePayment(@PathVariable String id) {
        try {
            paymentService.deletePayment(id);
            return ResponseEntity.ok("Pago eliminado");
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Error al eliminar pago: " + e.getMessage());
        }
    }

    /**
     * Obtener historial de niveles de jugadores
     */
    @GetMapping("/player-level-history")
    public List<PlayerLevelHistoryDTO> getPlayerLevelHistory() {
        List<PlayerLevelHistoryDTO> result = new ArrayList<>();
        List<Player> players = playerService.getAllPlayers();

        for (Player player : players) {
            List<PlayerLevelHistory> history = playerLevelHistoryRepository.findByPlayerNameOrderByDateAsc(player.getName());

            int previousLevel = player.getSkillLevel();
            double averageLevel = player.getSkillLevel();
            int suggestedLevel = player.getSkillLevel();

            if (!history.isEmpty()) {
                // Nivel anterior: penúltimo cambio, o el primero si solo hay uno
                previousLevel = history.size() > 1
                    ? history.get(history.size() - 2).getNewLevel()
                    : history.get(0).getPreviousLevel();

                // Promedio: de todos los newLevel históricos
                averageLevel = history.stream().mapToInt(PlayerLevelHistory::getNewLevel).average().orElse(player.getSkillLevel());

                // Nivel sugerido: redondeo del promedio
                suggestedLevel = (int) Math.round(averageLevel);
            }

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

    /**
     * Eliminar todos los pagos
     */
    @DeleteMapping("/payments/reset")
    public ResponseEntity<?> resetAllPayments() {
        paymentService.deleteAllPayments();
        return ResponseEntity.ok("Todos los pagos eliminados");
    }

    /**
     * Login de usuario
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserDTO loginData) {
        String email = loginData.getEmail();
        String password = loginData.getPassword();
        User user = userService.findByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            return ResponseEntity.ok("Login exitoso");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
        }
    }

    /**
     * Registro de usuario
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserDTO userDTO) {
        if (userService.findByEmail(userDTO.getEmail()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El email ya está registrado");
        }
        User user = new User();
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword()); // Para producción, usa hash
        userService.save(user);
        return ResponseEntity.ok("Usuario registrado exitosamente");
    }
}
