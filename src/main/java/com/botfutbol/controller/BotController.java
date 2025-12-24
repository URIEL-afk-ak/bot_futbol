package com.botfutbol.controller;

import com.botfutbol.dto.*;
import com.botfutbol.entity.Goal;
import com.botfutbol.entity.Payment;
import com.botfutbol.entity.Player;
import com.botfutbol.entity.Team;
import com.botfutbol.entity.User;
import com.botfutbol.service.ChatParsingService;
import com.botfutbol.service.GameEventService;
import com.botfutbol.service.GroupService;
import com.botfutbol.service.MatchService;
import com.botfutbol.service.PaymentService;
import com.botfutbol.service.PlayerService;
import com.botfutbol.service.TeamService;
import com.botfutbol.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

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
    private final GroupService groupService;
    private final GameEventService gameEventService;
    private final UserService userService;
    
    public BotController(PlayerService playerService,
                         TeamService teamService,
                         PaymentService paymentService,
                         MatchService matchService,
                         ChatParsingService chatParsingService,
                         GroupService groupService,
                         GameEventService gameEventService,
                         UserService userService) {
        this.playerService = playerService;
        this.teamService = teamService;
        this.paymentService = paymentService;
        this.matchService = matchService;
        this.chatParsingService = chatParsingService;
        this.groupService = groupService;
        this.gameEventService = gameEventService;
        this.userService = userService;
    }
    
    // ==================== REST API ENDPOINTS ====================
    
    /**
     * Método auxiliar para obtener el usuario del header
     */
    private User getUserFromRequest(HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null || userIdStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Usuario no autenticado. Header X-User-Id requerido.");
        }
        try {
            Long userId = Long.parseLong(userIdStr.trim());
            User user = userService.findById(userId);
            if (user == null) {
                throw new IllegalArgumentException("Usuario no encontrado con ID: " + userIdStr);
            }
            return user;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID de usuario inválido: " + userIdStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error al obtener usuario: " + e.getMessage());
        }
    }
    
    /**
     * Obtener todos los jugadores
     */
    @GetMapping("/players")
    public ResponseEntity<List<Player>> getAllPlayers(HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            return ResponseEntity.ok(playerService.getAllPlayers(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ArrayList<>());
        }
    }
    
    /**
     * Agregar un jugador
     */
    @PostMapping("/player/add")
    public ResponseEntity<?> addPlayerRest(@Valid @RequestBody PlayerDTO dto, HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            Player player = playerService.addPlayer(dto, user);
            return ResponseEntity.ok(String.format("Jugador agregado: %s", player.getName()));
        } catch (IllegalArgumentException e) {
            // El GlobalExceptionHandler manejará esto
            throw e;
        }
    }
    
    /**
     * Actualizar un jugador
     */
    @PutMapping("/player/update/{oldName}")
    public ResponseEntity<String> updatePlayer(@PathVariable String oldName, @RequestBody PlayerDTO dto, HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            Optional<Player> playerOpt = playerService.findPlayerByName(oldName, user);
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
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar: " + e.getMessage());
        }
    }
    
    /**
     * Eliminar un jugador
     */
    @DeleteMapping("/player/delete/{name}")
    public ResponseEntity<String> deletePlayer(@PathVariable String name, HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            boolean removed = playerService.removePlayer(name, user);
            if (removed) {
                return ResponseEntity.ok("Jugador eliminado: " + name);
            }
            return ResponseEntity.badRequest().body("Jugador no encontrado: " + name);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
    
    /**
     * Obtener jugadores con deuda
     */
    @GetMapping("/players/debt")
    public ResponseEntity<List<Player>> getPlayersWithDebt(HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            return ResponseEntity.ok(playerService.getPlayersWithDebt(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ArrayList<>());
        }
    }
    
    /**
     * Marcar asistencia de un jugador
     */
    @PutMapping("/player/attendance/{name}")
    public ResponseEntity<String> markAttendance(@PathVariable String name, @RequestParam boolean attended, HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            // Usar el método markAttendance del servicio que invalida el caché correctamente
            playerService.markAttendance(name, attended, user);
            return ResponseEntity.ok(String.format("Asistencia actualizada: %s", name));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Generar equipos aleatorios
     */
    @PostMapping("/teams/random")
    public ResponseEntity<Map<String, TeamDTO>> generateRandomTeamsRest(HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            List<Team> teams = teamService.generateRandomTeams(user);
            List<TeamDTO> dtos = teamService.convertToDTOs(teams);
            
            Map<String, TeamDTO> response = new HashMap<>();
            response.put("teamA", dtos.get(0));
            response.put("teamB", dtos.get(1));
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    
    /**
     * Generar equipos balanceados
     */
    @PostMapping("/teams/balanced")
    public ResponseEntity<Map<String, TeamDTO>> generateBalancedTeamsRest(HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            List<Team> teams = teamService.generateBalancedTeams(user);
            List<TeamDTO> dtos = teamService.convertToDTOs(teams);
            
            Map<String, TeamDTO> response = new HashMap<>();
            response.put("teamA", dtos.get(0));
            response.put("teamB", dtos.get(1));
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    
    /**
     * Registrar un gol
     */
    @PostMapping("/goal/record/{playerName}")
    public ResponseEntity<String> recordGoalRest(@PathVariable String playerName, HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            // Por defecto registra en equipo A, el frontend puede ampliarse para seleccionar equipo
            GoalDTO dto = new GoalDTO(playerName, "A");
            Goal goal = matchService.registerGoal(dto, user);
            return ResponseEntity.ok(String.format("Gol registrado para %s", goal.getPlayerName()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * Registrar un gol solo por equipo (para reconocimiento de voz - fútbol 5/7)
     */
    @PostMapping("/goal/record-by-team/{teamId}")
    public ResponseEntity<?> recordGoalByTeam(@PathVariable String teamId, HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            matchService.registerGoalByTeam(teamId, user);
            return ResponseEntity.ok(String.format("Gol registrado para equipo %s", teamId));
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e; // El GlobalExceptionHandler manejará esto
        }
    }
    
    /**
     * Deshacer el último gol registrado
     */
    @PostMapping("/goal/undo-last")
    public ResponseEntity<?> undoLastGoal(HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            boolean success = matchService.undoLastGoal(user);
            if (success) {
                return ResponseEntity.ok("Último gol deshecho exitosamente");
            } else {
                return ResponseEntity.badRequest().body("No hay goles para deshacer");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e; // El GlobalExceptionHandler manejará esto
        }
    }
    
    /**
     * Obtener todos los goles
     */
    @GetMapping("/goals")
    public ResponseEntity<List<Goal>> getAllGoals(HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            return ResponseEntity.ok(matchService.getAllGoals(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ArrayList<>());
        }
    }
    
    /**
     * Registrar un pago
     */
    @PostMapping("/payment/record")
    public ResponseEntity<?> recordPaymentRest(@Valid @RequestBody PaymentDTO dto, HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            Payment payment = paymentService.registerPayment(dto, user);
            double debt = paymentService.getPlayerDebt(payment.getPlayerName(), user);
            return ResponseEntity.ok(String.format("Pago registrado: $%.2f. Deuda restante: $%.2f", 
                    payment.getAmount(), debt));
        } catch (IllegalArgumentException e) {
            throw e; // El GlobalExceptionHandler manejará esto
        }
    }
    
    /**
     * Obtener todos los pagos
     */
    @GetMapping("/payments")
    public ResponseEntity<List<Payment>> getAllPayments(HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            return ResponseEntity.ok(paymentService.getAllPayments(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ArrayList<>());
        }
    }
    
    /**
     * Obtener estadísticas generales
     */
    @GetMapping("/stats")
    public ResponseEntity<StatsDTO> getStats(HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            return ResponseEntity.ok(matchService.getStats(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    
    // ==================== COMANDO DE TEXTO ====================
    
    /**
     * Procesa un mensaje/comando del usuario.
     * Retorna la respuesta formateada.
     */
    @PostMapping("/message")
    public ResponseEntity<String> processMessage(@RequestBody String message, HttpServletRequest request) {
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("❌ Comando vacío");
        }
        
        try {
            User user = getUserFromRequest(request);
            message = message.trim();
            String[] parts = message.split("\\s+");
            String command = parts[0].toLowerCase();
            
            String response = switch (command) {
                case "/agregar", "/add" -> handleAddPlayer(parts, user);
                case "/eliminar", "/remove" -> handleRemovePlayer(parts, user);
                case "/lista", "/list" -> handleListPlayers(user);
                case "/equipos", "/teams" -> handleGenerateTeams(parts, user);
                case "/iniciar", "/start" -> handleStartMatch(parts, user);
                case "/gol", "/goal" -> handleRegisterGoal(parts, user);
                case "/resultado", "/score" -> handleGetScore(user);
                case "/finalizar", "/end" -> handleEndMatch(user);
                case "/pago", "/pay" -> handleRegisterPayment(parts, user);
                case "/deuda", "/debt" -> handleCheckDebt(parts, user);
                case "/stats", "/estadisticas" -> handleGetStats(user);
                case "/ayuda", "/help" -> handleHelp();
                default -> "❌ Comando no reconocido. Usa /ayuda para ver los comandos disponibles.";
            };
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("❌ Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Error: " + e.getMessage());
        }
    }
    
    /**
     * /agregar Juan [nivel]
     */
    private String handleAddPlayer(String[] parts, User user) {
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
        Player player = playerService.addPlayer(dto, user);
        
        return String.format("✅ Jugador agregado: %s (Nivel: %d)", 
                player.getName(), player.getSkillLevel());
    }
    
    /**
     * /eliminar Juan
     */
    private String handleRemovePlayer(String[] parts, User user) {
        if (parts.length < 2) {
            return "❌ Uso: /eliminar <nombre>";
        }
        
        String name = parts[1];
        boolean removed = playerService.removePlayer(name, user);
        
        if (removed) {
            return "✅ Jugador eliminado: " + name;
        } else {
            return "❌ Jugador no encontrado: " + name;
        }
    }
    
    /**
     * /lista
     */
    private String handleListPlayers(User user) {
        List<Player> players = playerService.getAllPlayers(user);
        
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
    private String handleGenerateTeams(String[] parts, User user) {
        boolean balanced = parts.length > 1 && parts[1].equalsIgnoreCase("balanceado");
        
        List<Team> teams = balanced ? 
                teamService.generateBalancedTeams(user) : 
                teamService.generateRandomTeams(user);
        
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
    private String handleStartMatch(String[] parts, User user) {
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
        List<Team> teams = teamService.generateBalancedTeams(user);
        
        // Iniciar partido
        matchService.startMatch(teams.get(0), teams.get(1), cost, user);
        
        StringBuilder sb = new StringBuilder("🏁 ¡Partido iniciado!\n\n");
        sb.append(String.format("💰 Costo por jugador: $%.2f\n\n", cost));
        sb.append(formatTeams(teams));
        
        return sb.toString();
    }
    
    /**
     * /gol Juan A
     */
    private String handleRegisterGoal(String[] parts, User user) {
        if (parts.length < 3) {
            return "❌ Uso: /gol <jugador> <equipo: A o B>";
        }
        
        String playerName = parts[1];
        String teamId = parts[2].toUpperCase();
        
        if (!teamId.equals("A") && !teamId.equals("B")) {
            return "❌ El equipo debe ser A o B";
        }
        
        GoalDTO dto = new GoalDTO(playerName, teamId);
        Goal goal = matchService.registerGoal(dto, user);
        
        String score = matchService.getCurrentMatchScore(user);
        
        return String.format("⚽ ¡GOL de %s!\n\n📊 %s", goal.getPlayerName(), score);
    }
    
    /**
     * /resultado
     */
    private String handleGetScore(User user) {
        String score = matchService.getCurrentMatchScore(user);
        return "📊 " + score;
    }
    
    /**
     * /finalizar
     */
    private String handleEndMatch(User user) {
        String score = matchService.getCurrentMatchScore(user);
        matchService.endMatch(user);
        return "🏁 Partido finalizado\n\n📊 Resultado final: " + score;
    }
    
    /**
     * /pago Juan 1500 [concepto]
     */
    private String handleRegisterPayment(String[] parts, User user) {
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
        Payment payment = paymentService.registerPayment(dto, user);
        
        double debt = paymentService.getPlayerDebt(playerName, user);
        
        return String.format("✅ Pago registrado: %s pagó $%.2f\n💰 Deuda restante: $%.2f",
                payment.getPlayerName(), payment.getAmount(), debt);
    }
    
    /**
     * /deuda [jugador]
     */
    private String handleCheckDebt(String[] parts, User user) {
        if (parts.length < 2) {
            // Mostrar todos los deudores
            List<Player> debtors = playerService.getPlayersWithDebt(user);
            
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
            double debt = paymentService.getPlayerDebt(playerName, user);
            
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
    private String handleGetStats(User user) {
        StatsDTO stats = matchService.getStats(user);
        
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
    public ResponseEntity<ChatParsingResponseDTO> importFromChat(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        try {
            // Log para debugging
            String userIdHeader = httpRequest.getHeader("X-User-Id");
            System.out.println("DEBUG: X-User-Id header: " + userIdHeader);
            
            User user = getUserFromRequest(httpRequest);
            System.out.println("DEBUG: Usuario obtenido: " + user.getEmail() + " (ID: " + user.getId() + ")");
            
            String chatText = request.get("text");
            
            if (chatText == null || chatText.trim().isEmpty()) {
                ChatParsingResponseDTO errorResponse = new ChatParsingResponseDTO();
                errorResponse.setUnrecognizedMessages(List.of("El texto del chat está vacío"));
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            System.out.println("DEBUG: Procesando chat text de longitud: " + chatText.length());
            ChatParsingService.ChatParsingResult result = chatParsingService.processChatText(chatText, user);
            System.out.println("DEBUG: Chat procesado exitosamente");
            
            // Convertir a DTO
            ChatParsingResponseDTO response = new ChatParsingResponseDTO();
            response.setPlayersConfirmed(result.getPlayersConfirmed());
            response.setPaymentsRegistered(result.getPaymentsRegistered());
            response.setAttendanceMarked(result.getAttendanceMarked());
            response.setConfirmedPlayers(result.getConfirmedPlayers());
            response.setPaidPlayers(result.getPaidPlayers());
            response.setAttendanceMarkedPlayers(result.getAttendanceMarkedPlayers());
            response.setUnrecognizedMessages(result.getUnrecognizedMessages());
            response.setNewPlayersAdded(result.getNewPlayersAdded());
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            System.err.println("ERROR IllegalArgumentException: " + e.getMessage());
            e.printStackTrace();
            ChatParsingResponseDTO errorResponse = new ChatParsingResponseDTO();
            errorResponse.setUnrecognizedMessages(List.of("Error de autenticación: " + e.getMessage()));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        } catch (Exception e) {
            System.err.println("ERROR Exception: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            // Retornar un DTO con información del error
            ChatParsingResponseDTO errorResponse = new ChatParsingResponseDTO();
            String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            errorResponse.setUnrecognizedMessages(List.of("Error al procesar chat: " + errorMessage));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Obtener información del partido activo (para reconocimiento de voz)
     */
    @GetMapping("/matches/active")
    public ResponseEntity<com.botfutbol.dto.ActiveMatchDTO> getActiveMatchInfo(HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            com.botfutbol.dto.ActiveMatchDTO matchInfo = matchService.getActiveMatchInfo(user);
            return ResponseEntity.ok(matchInfo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    
    /**
     * Obtener resumen del partido para compartir en WhatsApp
     */
    @GetMapping("/matches/{matchId}/summary")
    public ResponseEntity<MatchSummaryDTO> getMatchSummary(@PathVariable String matchId, HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            MatchSummaryDTO summary = matchService.getMatchSummary(matchId, user);
            return ResponseEntity.ok(summary);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
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
    public ResponseEntity<MatchSummaryDTO> getCurrentMatchSummary(HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            Optional<com.botfutbol.entity.Match> currentMatch = matchService.getActiveMatch(user);
            
            if (currentMatch.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            MatchSummaryDTO summary = matchService.getMatchSummary(currentMatch.get().getId(), user);
            return ResponseEntity.ok(summary);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
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
    public ResponseEntity<?> updatePayment(@PathVariable String id, @RequestBody PaymentDTO dto, HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            Payment updated = paymentService.updatePayment(id, dto, user);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Error al editar pago: " + e.getMessage());
        }
    }

    /**
     * Eliminar un pago
     */
    @DeleteMapping("/payment/delete/{id}")
    public ResponseEntity<?> deletePayment(@PathVariable String id, HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            paymentService.deletePayment(id, user);
            return ResponseEntity.ok("Pago eliminado");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Error al eliminar pago: " + e.getMessage());
        }
    }

    /**
     * Obtener historial de niveles de jugadores
     */

    /**
     * Eliminar todos los pagos
     */
    @DeleteMapping("/payments/reset")
    public ResponseEntity<?> resetAllPayments(HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            paymentService.deleteAllPayments(user);
            return ResponseEntity.ok("Todos los pagos eliminados");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    /**
     * Desmarcar asistencia de todos los jugadores
     */
    @PutMapping("/players/unmark-all-attendance")
    public ResponseEntity<Map<String, Object>> unmarkAllAttendance(HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            int count = playerService.unmarkAllAttendance(user);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", String.format("Asistencia desmarcada para %d jugador(es)", count));
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * Login de usuario
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody com.botfutbol.dto.LoginDTO loginData) {
        String email = loginData.getEmail();
        String password = loginData.getPassword();
        User user = userService.findByEmail(email);
        
        if (user != null && userService.checkPassword(password, user.getPassword())) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Login exitoso");
            response.put("userId", user.getId());
            response.put("nombre", user.getNombre());
            response.put("apellido", user.getApellido());
            response.put("email", user.getEmail());
            response.put("username", user.getUsername());
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Credenciales inválidas");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * Registro de usuario
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody UserDTO userDTO) {
        // La validación de campos se hace automáticamente con @Valid
        
        if (userService.findByEmail(userDTO.getEmail()) != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "El email ya está registrado");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        
        if (userService.findByUsername(userDTO.getUsername()) != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "El nombre de usuario ya está en uso");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        
        User user = new User();
        user.setNombre(userDTO.getNombre().trim());
        user.setApellido(userDTO.getApellido().trim());
        user.setEmail(userDTO.getEmail().trim().toLowerCase());
        user.setUsername(userDTO.getUsername().trim().toLowerCase());
        user.setPassword(userDTO.getPassword()); // El servicio lo hasheará automáticamente
        userService.save(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Usuario registrado exitosamente");
        response.put("userId", user.getId());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Obtener información del usuario actual
     */
    @GetMapping("/user/profile")
    public ResponseEntity<Map<String, Object>> getProfile(HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("id", user.getId());
            response.put("nombre", user.getNombre());
            response.put("apellido", user.getApellido());
            response.put("email", user.getEmail());
            response.put("username", user.getUsername());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener perfil");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Actualizar perfil del usuario
     */
    @PutMapping("/user/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody UserDTO userDTO, HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            User updatedUser = userService.updateProfile(
                user.getId(),
                userDTO.getNombre(),
                userDTO.getApellido(),
                userDTO.getEmail(),
                userDTO.getUsername(),
                userDTO.getPassword()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Perfil actualizado exitosamente");
            response.put("nombre", updatedUser.getNombre());
            response.put("apellido", updatedUser.getApellido());
            response.put("email", updatedUser.getEmail());
            response.put("username", updatedUser.getUsername());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al actualizar perfil");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // ==================== GRUPOS ENDPOINTS ====================
    
    /**
     * Crear un nuevo grupo
     */
    @PostMapping("/groups")
    public ResponseEntity<Map<String, Object>> createGroup(@RequestBody CreateGroupDTO createGroupDTO, 
                                                           HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            GroupDTO group = groupService.createGroup(
                createGroupDTO.getName(),
                createGroupDTO.getDescription(),
                user.getId()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Grupo creado exitosamente");
            response.put("group", group);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al crear grupo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Obtener todos los grupos del usuario
     */
    @GetMapping("/groups")
    public ResponseEntity<Map<String, Object>> getUserGroups(HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            List<GroupDTO> groups = groupService.getUserGroups(user.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("groups", groups);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener grupos");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Obtener un grupo por ID
     */
    @GetMapping("/groups/{groupId}")
    public ResponseEntity<Map<String, Object>> getGroupById(@PathVariable String groupId, 
                                                           HttpServletRequest request) {
        try {
            getUserFromRequest(request); // Autenticación
            GroupDTO group = groupService.getGroupById(groupId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("group", group);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener grupo");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Unirse a un grupo
     */
    @PostMapping("/groups/{groupId}/join")
    public ResponseEntity<Map<String, Object>> joinGroup(@PathVariable String groupId, 
                                                         HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            GroupMemberDTO member = groupService.joinGroup(groupId, user.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Te has unido al grupo exitosamente");
            response.put("member", member);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al unirse al grupo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Abandonar un grupo
     */
    @PostMapping("/groups/{groupId}/leave")
    public ResponseEntity<Map<String, Object>> leaveGroup(@PathVariable String groupId, 
                                                          HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            groupService.leaveGroup(groupId, user.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Has abandonado el grupo exitosamente");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al abandonar el grupo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Obtener miembros de un grupo
     */
    @GetMapping("/groups/{groupId}/members")
    public ResponseEntity<Map<String, Object>> getGroupMembers(@PathVariable String groupId, 
                                                             HttpServletRequest request) {
        try {
            getUserFromRequest(request); // Autenticación
            List<GroupMemberDTO> members = groupService.getGroupMembers(groupId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("members", members);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener miembros");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Invitar usuario al grupo por username
     */
    @PostMapping("/groups/{groupId}/invite")
    public ResponseEntity<Map<String, Object>> inviteUserByUsername(@PathVariable String groupId,
                                                                   @RequestBody Map<String, String> requestBody,
                                                                   HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            String username = requestBody.get("username");
            
            if (username == null || username.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "El username es requerido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            GroupMemberDTO member = groupService.inviteUserByUsername(groupId, username.trim(), user.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Usuario invitado exitosamente");
            response.put("member", member);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al invitar usuario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Eliminar miembro del grupo (solo administradores)
     */
    @DeleteMapping("/groups/{groupId}/members/{memberUserId}")
    public ResponseEntity<Map<String, Object>> removeMemberFromGroup(@PathVariable String groupId,
                                                                    @PathVariable Long memberUserId,
                                                                    HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            groupService.removeMemberFromGroup(groupId, memberUserId, user.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Miembro eliminado del grupo exitosamente");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al eliminar miembro: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Crear enlace de invitación para el grupo
     */
    @PostMapping("/groups/{groupId}/invitation-link")
    public ResponseEntity<Map<String, Object>> createInvitationLink(@PathVariable String groupId,
                                                                   @RequestBody(required = false) Map<String, Object> requestBody,
                                                                   HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            
            java.time.LocalDateTime expiresAt = null;
            Integer maxUses = null;
            
            if (requestBody != null) {
                if (requestBody.containsKey("expiresAt")) {
                    String expiresAtStr = (String) requestBody.get("expiresAt");
                    if (expiresAtStr != null && !expiresAtStr.isEmpty()) {
                        expiresAt = java.time.LocalDateTime.parse(expiresAtStr);
                    }
                }
                if (requestBody.containsKey("maxUses")) {
                    maxUses = (Integer) requestBody.get("maxUses");
                }
            }
            
            com.botfutbol.entity.GroupInvitation invitation = groupService.createInvitationLink(
                groupId, user.getId(), expiresAt, maxUses
            );
            
            // Construir el enlace completo (el frontend puede construir la URL completa)
            String invitationLink = "/join-group/" + invitation.getInvitationCode();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Enlace de invitación creado exitosamente");
            response.put("invitationCode", invitation.getInvitationCode());
            response.put("invitationLink", invitationLink);
            response.put("expiresAt", invitation.getExpiresAt());
            response.put("maxUses", invitation.getMaxUses());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al crear enlace: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Unirse a un grupo usando código de invitación
     */
    @PostMapping("/groups/join-by-code")
    public ResponseEntity<Map<String, Object>> joinGroupByInvitationCode(@RequestBody Map<String, String> requestBody,
                                                                       HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            String invitationCode = requestBody.get("invitationCode");
            
            if (invitationCode == null || invitationCode.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "El código de invitación es requerido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            GroupMemberDTO member = groupService.joinGroupByInvitationCode(invitationCode.trim().toUpperCase(), user.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Te has unido al grupo exitosamente");
            response.put("member", member);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al unirse al grupo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Obtener enlace de invitación activo del grupo
     */
    @GetMapping("/groups/{groupId}/invitation-link")
    public ResponseEntity<Map<String, Object>> getActiveInvitationLink(@PathVariable String groupId,
                                                                     HttpServletRequest request) {
        try {
            getUserFromRequest(request); // Autenticación
            com.botfutbol.entity.GroupInvitation invitation = groupService.getActiveInvitationLink(groupId);
            
            Map<String, Object> response = new HashMap<>();
            if (invitation != null) {
                String invitationLink = "/join-group/" + invitation.getInvitationCode();
                response.put("success", true);
                response.put("invitationCode", invitation.getInvitationCode());
                response.put("invitationLink", invitationLink);
                response.put("expiresAt", invitation.getExpiresAt());
                response.put("maxUses", invitation.getMaxUses());
                response.put("currentUses", invitation.getCurrentUses());
            } else {
                response.put("success", false);
                response.put("message", "No hay enlace de invitación activo para este grupo");
            }
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener enlace");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // ==================== EVENTOS DE JUEGO ENDPOINTS ====================
    
    /**
     * Crear un nuevo evento de juego
     */
    @PostMapping("/groups/{groupId}/events")
    public ResponseEntity<Map<String, Object>> createGameEvent(@PathVariable String groupId,
                                                               @RequestBody CreateGameEventDTO createEventDTO,
                                                               HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            GameEventDTO event = gameEventService.createGameEvent(
                groupId,
                createEventDTO.getDate(),
                createEventDTO.getLocation(),
                createEventDTO.getCostPerPlayer(),
                createEventDTO.getMaxPlayers(),
                createEventDTO.getVotingDeadline(),
                user.getId()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Evento creado exitosamente");
            response.put("event", event);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al crear evento: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Obtener eventos activos de un grupo
     */
    @GetMapping("/groups/{groupId}/events")
    public ResponseEntity<Map<String, Object>> getGroupEvents(@PathVariable String groupId,
                                                               HttpServletRequest request) {
        try {
            getUserFromRequest(request); // Autenticación
            List<GameEventDTO> events = gameEventService.getActiveEventsByGroup(groupId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("events", events);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener eventos");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Obtener un evento por ID
     */
    @GetMapping("/events/{eventId}")
    public ResponseEntity<Map<String, Object>> getEventById(@PathVariable String eventId,
                                                            HttpServletRequest request) {
        try {
            getUserFromRequest(request); // Autenticación
            GameEventDTO event = gameEventService.getEventById(eventId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("event", event);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener evento");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Votar asistencia a un evento (sí o no)
     */
    @PostMapping("/events/{eventId}/vote")
    public ResponseEntity<Map<String, Object>> voteAttendance(@PathVariable String eventId,
                                                             @RequestBody VoteAttendanceDTO voteDTO,
                                                             HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            AttendanceVoteDTO vote = gameEventService.voteAttendance(
                eventId,
                user.getId(),
                voteDTO.isAttending()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", voteDTO.isAttending() ? 
                "Has confirmado tu asistencia" : "Has indicado que no asistirás");
            response.put("vote", vote);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al votar: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Cancelar asistencia (cambiar voto a "no")
     */
    @PostMapping("/events/{eventId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelAttendance(@PathVariable String eventId,
                                                                HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            AttendanceVoteDTO vote = gameEventService.cancelAttendance(eventId, user.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Has cancelado tu asistencia");
            response.put("vote", vote);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al cancelar asistencia: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Obtener lista de confirmados (usuarios que votaron "sí")
     */
    @GetMapping("/events/{eventId}/confirmed")
    public ResponseEntity<Map<String, Object>> getConfirmedAttendees(@PathVariable String eventId,
                                                                     HttpServletRequest request) {
        try {
            getUserFromRequest(request); // Autenticación
            List<AttendanceVoteDTO> confirmed = gameEventService.getConfirmedAttendees(eventId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("confirmed", confirmed);
            response.put("count", confirmed.size());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener confirmados");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Obtener todas las votaciones de un evento
     */
    @GetMapping("/events/{eventId}/votes")
    public ResponseEntity<Map<String, Object>> getAllVotes(@PathVariable String eventId,
                                                          HttpServletRequest request) {
        try {
            getUserFromRequest(request); // Autenticación
            List<AttendanceVoteDTO> votes = gameEventService.getAllVotes(eventId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("votes", votes);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener votaciones");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Formar equipos desde un evento de grupo (aleatorio)
     */
    @PostMapping("/events/{eventId}/teams/random")
    public ResponseEntity<Map<String, Object>> formRandomTeamsFromEvent(@PathVariable String eventId,
                                                                        HttpServletRequest request) {
        try {
            getUserFromRequest(request); // Autenticación
            List<AttendanceVoteDTO> confirmed = gameEventService.getConfirmedAttendees(eventId);
            
            if (confirmed.size() < 2) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Se necesitan al menos 2 usuarios confirmados para formar equipos");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // Obtener usuarios confirmados
            List<User> confirmedUsers = confirmed.stream()
                    .map(v -> userService.findById(v.getUserId()))
                    .filter(u -> u != null)
                    .collect(java.util.stream.Collectors.toList());
            
            List<Team> teams = teamService.generateRandomTeamsFromUsers(confirmedUsers);
            List<TeamDTO> teamDTOs = teamService.convertToDTOs(teams);
            
            // Marcar que los equipos fueron formados
            gameEventService.markTeamsFormed(eventId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Equipos formados exitosamente");
            response.put("teams", teamDTOs);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al formar equipos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Formar equipos desde un evento de grupo (balanceado)
     */
    @PostMapping("/events/{eventId}/teams/balanced")
    public ResponseEntity<Map<String, Object>> formBalancedTeamsFromEvent(@PathVariable String eventId,
                                                                          HttpServletRequest request) {
        try {
            getUserFromRequest(request); // Autenticación
            List<AttendanceVoteDTO> confirmed = gameEventService.getConfirmedAttendees(eventId);
            
            if (confirmed.size() < 2) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Se necesitan al menos 2 usuarios confirmados para formar equipos");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            List<User> confirmedUsers = confirmed.stream()
                    .map(v -> userService.findById(v.getUserId()))
                    .filter(u -> u != null)
                    .collect(java.util.stream.Collectors.toList());
            
            List<Team> teams = teamService.generateBalancedTeamsFromUsers(confirmedUsers);
            List<TeamDTO> teamDTOs = teamService.convertToDTOs(teams);
            
            gameEventService.markTeamsFormed(eventId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Equipos balanceados formados exitosamente");
            response.put("teams", teamDTOs);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al formar equipos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Formar equipos desde un evento de grupo (por posición)
     */
    @PostMapping("/events/{eventId}/teams/position")
    public ResponseEntity<Map<String, Object>> formTeamsByPositionFromEvent(@PathVariable String eventId,
                                                                            @RequestParam(defaultValue = "false") boolean balance,
                                                                            HttpServletRequest request) {
        try {
            getUserFromRequest(request); // Autenticación
            List<AttendanceVoteDTO> confirmed = gameEventService.getConfirmedAttendees(eventId);
            
            if (confirmed.size() < 2) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Se necesitan al menos 2 usuarios confirmados para formar equipos");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            List<User> confirmedUsers = confirmed.stream()
                    .map(v -> userService.findById(v.getUserId()))
                    .filter(u -> u != null)
                    .collect(java.util.stream.Collectors.toList());
            
            List<Team> teams = teamService.generateTeamsByPositionFromUsers(confirmedUsers, balance);
            List<TeamDTO> teamDTOs = teamService.convertToDTOs(teams);
            
            gameEventService.markTeamsFormed(eventId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Equipos formados por posición exitosamente");
            response.put("teams", teamDTOs);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al formar equipos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // ==================== REGISTRO Y CALIFICACIONES DE EVENTOS ====================
    
    /**
     * Registrar/finalizar un evento (solo administradores)
     */
    @PostMapping("/events/{eventId}/register")
    public ResponseEntity<Map<String, Object>> registerEvent(@PathVariable String eventId,
                                                             HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            GameEventDTO event = gameEventService.registerEvent(eventId, user.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Evento registrado exitosamente");
            response.put("event", event);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al registrar evento: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Calificar a un jugador después de un evento (1-10)
     */
    @PostMapping("/events/rate-player")
    public ResponseEntity<Map<String, Object>> ratePlayer(@RequestBody com.botfutbol.dto.RatePlayerDTO rateDTO,
                                                         HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            com.botfutbol.entity.PlayerRating rating = gameEventService.ratePlayer(
                rateDTO.getEventId(),
                rateDTO.getPlayerUserId(),
                user.getId(),
                rateDTO.getRating(),
                rateDTO.getComment()
            );
            
            // Convertir a DTO
            com.botfutbol.dto.PlayerRatingDTO ratingDTO = convertRatingToDTO(rating);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Jugador calificado exitosamente");
            response.put("rating", ratingDTO);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al calificar jugador: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Obtener calificaciones de un evento
     */
    @GetMapping("/events/{eventId}/ratings")
    public ResponseEntity<Map<String, Object>> getEventRatings(@PathVariable String eventId,
                                                               HttpServletRequest request) {
        try {
            getUserFromRequest(request); // Autenticación
            List<com.botfutbol.entity.PlayerRating> ratings = gameEventService.getEventRatings(eventId);
            
            List<com.botfutbol.dto.PlayerRatingDTO> ratingDTOs = ratings.stream()
                    .map(this::convertRatingToDTO)
                    .collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("ratings", ratingDTOs);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener calificaciones");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Obtener historial de calificaciones de un jugador
     */
    @GetMapping("/players/{playerUserId}/ratings")
    public ResponseEntity<Map<String, Object>> getPlayerRatingHistory(@PathVariable Long playerUserId,
                                                                     HttpServletRequest request) {
        try {
            getUserFromRequest(request); // Autenticación
            List<com.botfutbol.entity.PlayerRating> ratings = gameEventService.getPlayerRatingHistory(playerUserId);
            
            List<com.botfutbol.dto.PlayerRatingDTO> ratingDTOs = ratings.stream()
                    .map(this::convertRatingToDTO)
                    .collect(java.util.stream.Collectors.toList());
            
            // Calcular promedio
            Double average = gameEventService.getPlayerAverageRating(playerUserId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("ratings", ratingDTOs);
            response.put("averageRating", average);
            response.put("totalRatings", ratings.size());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener historial");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Obtener promedio de calificaciones de un jugador en un grupo
     */
    @GetMapping("/groups/{groupId}/players/{playerUserId}/average-rating")
    public ResponseEntity<Map<String, Object>> getPlayerAverageRatingInGroup(@PathVariable String groupId,
                                                                             @PathVariable Long playerUserId,
                                                                             HttpServletRequest request) {
        try {
            getUserFromRequest(request); // Autenticación
            Double average = gameEventService.getPlayerAverageRatingInGroup(playerUserId, groupId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("playerUserId", playerUserId);
            response.put("groupId", groupId);
            response.put("averageRating", average);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al calcular promedio");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Método auxiliar para convertir PlayerRating a DTO
     */
    private com.botfutbol.dto.PlayerRatingDTO convertRatingToDTO(com.botfutbol.entity.PlayerRating rating) {
        String playerName = rating.getPlayer().getNombre() + " " + rating.getPlayer().getApellido();
        String ratedByName = rating.getRatedBy().getNombre() + " " + rating.getRatedBy().getApellido();
        String eventName = rating.getEvent().getGroup().getName() + " - " + 
                          rating.getEvent().getDate().toString();
        
        return new com.botfutbol.dto.PlayerRatingDTO(
                rating.getId(),
                rating.getEvent().getId(),
                eventName,
                rating.getPlayer().getId(),
                playerName,
                rating.getPlayer().getUsername(),
                rating.getRatedBy().getId(),
                ratedByName,
                rating.getRating(),
                rating.getComment(),
                rating.getRatedAt()
        );
    }
}
