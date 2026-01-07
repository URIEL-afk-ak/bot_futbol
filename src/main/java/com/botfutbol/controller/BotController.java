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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Controlador principal del bot.
 * Responsabilidad: Recibir comandos del usuario, validarlos y llamar a los servicios.
 * NO contiene lógica de negocio.
 */
@RestController
@RequestMapping("/api/bot")
@CrossOrigin(origins = "*")
public class BotController {
    
    private static final Logger logger = LoggerFactory.getLogger(BotController.class);
    
    private final PlayerService playerService;
    private final TeamService teamService;
    private final PaymentService paymentService;
    private final MatchService matchService;
    private final ChatParsingService chatParsingService;
    private final GroupService groupService;
    private final GameEventService gameEventService;
    private final UserService userService;
    private final com.botfutbol.service.NotificationService notificationService;
    private final com.botfutbol.service.GroupPlayerRatingService groupPlayerRatingService;
    private final com.botfutbol.service.GroupMessageService groupMessageService;
    private final com.botfutbol.service.GroupPollService groupPollService;
    private final com.botfutbol.service.FCMService fcmService;
    private final com.botfutbol.service.GroupJoinRequestService groupJoinRequestService;
    
    public BotController(PlayerService playerService,
                         TeamService teamService,
                         PaymentService paymentService,
                         MatchService matchService,
                         ChatParsingService chatParsingService,
                         GroupService groupService,
                         GameEventService gameEventService,
                         UserService userService,
                         com.botfutbol.service.NotificationService notificationService,
                         com.botfutbol.service.GroupPlayerRatingService groupPlayerRatingService,
                         com.botfutbol.service.GroupMessageService groupMessageService,
                         com.botfutbol.service.GroupPollService groupPollService,
                         com.botfutbol.service.FCMService fcmService,
                         com.botfutbol.service.GroupJoinRequestService groupJoinRequestService) {
        this.playerService = playerService;
        this.teamService = teamService;
        this.paymentService = paymentService;
        this.matchService = matchService;
        this.chatParsingService = chatParsingService;
        this.groupService = groupService;
        this.gameEventService = gameEventService;
        this.userService = userService;
        this.notificationService = notificationService;
        this.groupPlayerRatingService = groupPlayerRatingService;
        this.groupMessageService = groupMessageService;
        this.groupPollService = groupPollService;
        this.fcmService = fcmService;
        this.groupJoinRequestService = groupJoinRequestService;
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
        Double skillLevel = null;
        
        if (parts.length >= 3) {
            try {
                skillLevel = Double.parseDouble(parts[2]);
            } catch (NumberFormatException e) {
                return "❌ El nivel debe ser un número entre 1 y 10";
            }
        }
        
        PlayerDTO dto = new PlayerDTO(name, skillLevel);
        Player player = playerService.addPlayer(dto, user);
        
        return String.format("✅ Jugador agregado: %s (Nivel: %.1f)", 
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
        try {
            String identifier = loginData.getEmail(); // Puede ser email o username
            String password = loginData.getPassword();
            
            // Intentar encontrar el usuario por email o username
            User user = userService.findByEmail(identifier);
            if (user == null) {
                user = userService.findByUsername(identifier);
            }
            
            if (user != null && userService.checkPassword(password, user.getPassword())) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Login exitoso");
                response.put("userId", user.getId());
                response.put("nombre", user.getNombre());
                response.put("apellido", user.getApellido());
                response.put("email", user.getEmail());
                // Manejar username de forma segura (puede ser null si la columna no existe)
                try {
                    response.put("username", user.getUsername() != null ? user.getUsername() : "");
                } catch (Exception e) {
                    // Si hay error al acceder a username (columna no existe), usar string vacío
                    response.put("username", "");
                }
                // Agregar foto de perfil
                response.put("profileImageUrl", user.getProfileImageUrl() != null ? user.getProfileImageUrl() : "");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Credenciales inválidas");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            System.err.println("Error en login: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al iniciar sesión: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Registro de usuario
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody UserDTO userDTO) {
        try {
            // La validación de campos se hace automáticamente con @Valid
            
            if (userService.findByEmail(userDTO.getEmail()) != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "El email ya está registrado");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            
            // Verificar username solo si no hay error de base de datos
            try {
                if (userDTO.getUsername() != null && !userDTO.getUsername().trim().isEmpty()) {
                    String username = userDTO.getUsername().trim();
                    
                    // Validar que el username solo contenga caracteres permitidos
                    // Permitidos: letras, números, y caracteres especiales: _ , : ; * - @ # $ % & \ ¡ ¿ ? ' | ° ¬ y espacios
                    if (!username.matches("^[a-zA-Z0-9_,;:*\\-@#$%&\\\\¡¿?'|°¬ ]+$")) {
                        Map<String, Object> response = new HashMap<>();
                        response.put("success", false);
                        response.put("message", "El nombre de usuario contiene caracteres no permitidos");
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                    }
                    
                    User existingUser = userService.findByUsername(username);
                    if (existingUser != null) {
                        Map<String, Object> response = new HashMap<>();
                        response.put("success", false);
                        response.put("message", "El nombre de usuario ya está en uso");
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
                    }
                }
            } catch (Exception e) {
                // Si hay error al buscar por username (columna no existe), continuar sin validar
                logger.warn("No se pudo validar username (columna puede no existir): {}", e.getMessage());
            }
            
            User user = new User();
            user.setNombre(userDTO.getNombre() != null ? userDTO.getNombre().trim() : "");
            user.setApellido(userDTO.getApellido() != null ? userDTO.getApellido().trim() : "");
            user.setEmail(userDTO.getEmail() != null ? userDTO.getEmail().trim().toLowerCase() : "");
            
            if (userDTO.getUsername() != null && !userDTO.getUsername().trim().isEmpty()) {
                user.setUsername(userDTO.getUsername().trim()); // Mantener mayúsculas/minúsculas originales
            }
            
            user.setPassword(userDTO.getPassword()); // El servicio lo hasheará automáticamente
            userService.save(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Usuario registrado exitosamente");
            response.put("userId", user.getId());
            return ResponseEntity.ok(response);
        } catch (org.springframework.dao.DataAccessException e) {
            // Error de base de datos (probablemente columna username no existe)
            logger.error("Error de base de datos en registro: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error de base de datos. Por favor, contacta al administrador.");
            // Log del error completo para debugging
            logger.error("Detalles del error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            // Log del error para debugging
            logger.error("Error en registro: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al registrar usuario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
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
            response.put("profileImageUrl", user.getProfileImageUrl() != null ? user.getProfileImageUrl() : "");
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
    @PutMapping(value = "/user/profile", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String apellido,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String password,
            @RequestPart(required = false) org.springframework.web.multipart.MultipartFile profileImage,
            HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            
            // Procesar imagen si existe
            byte[] imageBytes = null;
            if (profileImage != null && !profileImage.isEmpty()) {
                imageBytes = profileImage.getBytes();
            }
            
            User updatedUser = userService.updateProfile(
                user.getId(),
                nombre,
                apellido,
                email,
                username,
                password,
                imageBytes
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Perfil actualizado exitosamente");
            response.put("nombre", updatedUser.getNombre());
            response.put("apellido", updatedUser.getApellido());
            response.put("email", updatedUser.getEmail());
            response.put("username", updatedUser.getUsername());
            if (updatedUser.getProfileImageUrl() != null) {
                response.put("profileImageUrl", updatedUser.getProfileImageUrl());
            }
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("Error al actualizar perfil", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al actualizar perfil: " + e.getMessage());
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
                user.getId(),
                createGroupDTO.isPrivate(),
                createGroupDTO.getType()
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
            logger.error("Error al obtener grupos", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener grupos: " + e.getMessage());
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
     * Actualizar un grupo (solo administradores o el creador)
     */
    @PutMapping("/groups/{groupId}")
    public ResponseEntity<Map<String, Object>> updateGroup(@PathVariable String groupId,
                                                           @RequestBody UpdateGroupDTO updateGroupDTO,
                                                           HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            logger.info("Actualizando grupo {} - isPrivate recibido: {}", groupId, updateGroupDTO.getIsPrivate());
            GroupDTO group = groupService.updateGroup(
                groupId,
                updateGroupDTO.getName(),
                updateGroupDTO.getDescription(),
                updateGroupDTO.getType(),
                updateGroupDTO.getPhotoUrl(),
                updateGroupDTO.getIsPrivate(),
                user.getId()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Grupo actualizado exitosamente");
            response.put("group", group);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al actualizar grupo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Eliminar un grupo (solo administradores o el creador)
     */
    @DeleteMapping("/groups/{groupId}")
    public ResponseEntity<Map<String, Object>> deleteGroup(@PathVariable String groupId,
                                                           HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            groupService.deleteGroup(groupId, user.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Grupo eliminado exitosamente");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al eliminar grupo: " + e.getMessage());
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
     * Promover miembro a administrador (solo administradores)
     */
    @PostMapping("/groups/{groupId}/members/{memberUserId}/promote")
    public ResponseEntity<Map<String, Object>> promoteToAdmin(@PathVariable String groupId,
                                                             @PathVariable Long memberUserId,
                                                             HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            groupService.promoteToAdmin(groupId, memberUserId, user.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Usuario promovido a administrador exitosamente");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al promover usuario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Degradar administrador a miembro regular (solo administradores)
     */
    @PostMapping("/groups/{groupId}/members/{memberUserId}/demote")
    public ResponseEntity<Map<String, Object>> demoteFromAdmin(@PathVariable String groupId,
                                                              @PathVariable Long memberUserId,
                                                              HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            groupService.demoteFromAdmin(groupId, memberUserId, user.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Usuario degradado de administrador exitosamente");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al degradar usuario: " + e.getMessage());
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
     * Obtener información de un grupo por código de invitación (sin unirse)
     */
    @GetMapping("/groups/by-code/{invitationCode}")
    public ResponseEntity<Map<String, Object>> getGroupByInvitationCode(@PathVariable String invitationCode) {
        try {
            GroupDTO group = groupService.getGroupByInvitationCode(invitationCode.trim().toUpperCase());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("group", group);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener información del grupo: " + e.getMessage());
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
     * Obtener eventos históricos (completados) de un grupo
     */
    @GetMapping("/groups/{groupId}/events/history")
    public ResponseEntity<Map<String, Object>> getGroupHistoricalEvents(@PathVariable String groupId,
                                                                        HttpServletRequest request) {
        try {
            getUserFromRequest(request); // Autenticación
            List<GameEventDTO> events = gameEventService.getHistoricalEventsByGroup(groupId);
            
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
            response.put("message", "Error al obtener eventos históricos");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // ==================== PUNTUACIONES INICIALES Y PROMEDIOS ====================
    
    /**
     * Asignar o actualizar puntuación inicial de un jugador en un grupo (solo admin)
     */
    @PostMapping("/groups/{groupId}/players/{playerUserId}/initial-rating")
    public ResponseEntity<Map<String, Object>> setInitialRating(@PathVariable String groupId,
                                                                @PathVariable Long playerUserId,
                                                                @RequestBody SetInitialRatingDTO ratingDTO,
                                                                HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            com.botfutbol.entity.GroupPlayerInitialRating rating = 
                    groupPlayerRatingService.setInitialRating(
                            groupId, playerUserId, user.getId(), 
                            ratingDTO.getInitialRating(), ratingDTO.getComment());
            
            // Convertir a DTO
            GroupPlayerInitialRatingDTO dto = new GroupPlayerInitialRatingDTO();
            dto.setId(rating.getId());
            dto.setGroupId(rating.getGroup().getId());
            dto.setGroupName(rating.getGroup().getName());
            dto.setPlayerUserId(rating.getPlayer().getId());
            dto.setPlayerName(rating.getPlayer().getNombre() + " " + rating.getPlayer().getApellido());
            dto.setInitialRating(rating.getInitialRating());
            dto.setAssignedByUserId(rating.getAssignedBy().getId());
            dto.setAssignedByName(rating.getAssignedBy().getNombre() + " " + rating.getAssignedBy().getApellido());
            dto.setComment(rating.getComment());
            dto.setAssignedAt(rating.getAssignedAt());
            dto.setUpdatedAt(rating.getUpdatedAt());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Puntuación inicial asignada exitosamente");
            response.put("rating", dto);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al asignar puntuación inicial: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Obtener promedio de un jugador en un grupo (incluye puntuación inicial + partidos)
     */
    @GetMapping("/groups/{groupId}/players/{playerUserId}/average-rating")
    public ResponseEntity<Map<String, Object>> getPlayerAverageRating(@PathVariable String groupId,
                                                                      @PathVariable Long playerUserId,
                                                                      HttpServletRequest request) {
        try {
            getUserFromRequest(request); // Autenticación
            double average = groupPlayerRatingService.calculatePlayerAverageRating(groupId, playerUserId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("averageRating", average);
            response.put("playerUserId", playerUserId);
            response.put("groupId", groupId);
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
     * Obtener promedios de todos los jugadores de un grupo
     */
    @GetMapping("/groups/{groupId}/players/average-ratings")
    public ResponseEntity<Map<String, Object>> getGroupPlayersAverageRatings(@PathVariable String groupId,
                                                                              HttpServletRequest request) {
        try {
            getUserFromRequest(request); // Autenticación
            List<com.botfutbol.entity.GroupMember> members = groupService.getGroupMembersEntities(groupId);
            
            List<Map<String, Object>> playerRatings = new ArrayList<>();
            for (com.botfutbol.entity.GroupMember member : members) {
                double average = groupPlayerRatingService.calculatePlayerAverageRating(
                        groupId, member.getUser().getId());
                
                Map<String, Object> playerData = new HashMap<>();
                playerData.put("userId", member.getUser().getId());
                playerData.put("userName", member.getUser().getNombre() + " " + member.getUser().getApellido());
                playerData.put("averageRating", average);
                playerRatings.add(playerData);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("playerRatings", playerRatings);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener promedios");
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
     * Cancelar un evento (solo administradores)
     */
    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<Map<String, Object>> cancelEvent(@PathVariable String eventId,
                                                           HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            GameEventDTO event = gameEventService.cancelEvent(eventId, user.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Evento cancelado exitosamente");
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
            response.put("message", "Error al cancelar evento: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Actualizar un evento (solo administradores)
     */
    @PutMapping("/events/{eventId}")
    public ResponseEntity<Map<String, Object>> updateEvent(@PathVariable String eventId,
                                                            @RequestBody CreateGameEventDTO updateEventDTO,
                                                            HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            GameEventDTO event = gameEventService.updateEvent(
                eventId,
                updateEventDTO.getDate(),
                updateEventDTO.getLocation(),
                updateEventDTO.getCostPerPlayer(),
                updateEventDTO.getMaxPlayers(),
                updateEventDTO.getVotingDeadline(),
                user.getId()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Evento actualizado exitosamente");
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
            response.put("message", "Error al actualizar evento: " + e.getMessage());
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
            
            // Guardar equipos formados
            gameEventService.saveEventTeams(eventId, teams, confirmedUsers);
            
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
            
            // Obtener el grupo del evento para usar promedios del grupo
            String groupId = null;
            try {
                com.botfutbol.entity.GameEvent event = gameEventService.getEventEntity(eventId);
                if (event != null && event.getGroup() != null) {
                    groupId = event.getGroup().getId();
                }
            } catch (Exception e) {
                // Si no se puede obtener el grupo, usar promedio global
            }
            
            List<User> confirmedUsers = confirmed.stream()
                    .map(v -> userService.findById(v.getUserId()))
                    .filter(u -> u != null)
                    .collect(java.util.stream.Collectors.toList());
            
            List<Team> teams = teamService.generateBalancedTeamsFromUsers(confirmedUsers, groupId);
            List<TeamDTO> teamDTOs = teamService.convertToDTOs(teams);
            
            // Guardar equipos formados
            gameEventService.saveEventTeams(eventId, teams, confirmedUsers);
            
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
            
            // Obtener el grupo del evento para usar promedios del grupo
            String groupId = null;
            try {
                com.botfutbol.entity.GameEvent event = gameEventService.getEventEntity(eventId);
                if (event != null && event.getGroup() != null) {
                    groupId = event.getGroup().getId();
                }
            } catch (Exception e) {
                // Si no se puede obtener el grupo, usar promedio global
            }
            
            List<User> confirmedUsers = confirmed.stream()
                    .map(v -> userService.findById(v.getUserId()))
                    .filter(u -> u != null)
                    .collect(java.util.stream.Collectors.toList());
            
            List<Team> teams = teamService.generateTeamsByPositionFromUsers(confirmedUsers, balance, groupId);
            List<TeamDTO> teamDTOs = teamService.convertToDTOs(teams);
            
            // Guardar equipos formados
            gameEventService.saveEventTeams(eventId, teams, confirmedUsers);
            
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
    
    /**
     * Obtener equipos formados para un evento
     */
    @GetMapping("/events/{eventId}/teams")
    public ResponseEntity<Map<String, Object>> getEventTeams(@PathVariable String eventId,
                                                              HttpServletRequest request) {
        try {
            getUserFromRequest(request); // Autenticación
            List<com.botfutbol.entity.EventTeam> eventTeams = gameEventService.getEventTeams(eventId);
            
            // Obtener lista de usuarios actualmente confirmados
            List<AttendanceVoteDTO> confirmedVotes = gameEventService.getConfirmedAttendees(eventId);
            Set<Long> confirmedUserIds = confirmedVotes.stream()
                    .map(AttendanceVoteDTO::getUserId)
                    .collect(java.util.stream.Collectors.toSet());
            
            List<Map<String, Object>> teamsData = new ArrayList<>();
            for (com.botfutbol.entity.EventTeam eventTeam : eventTeams) {
                Map<String, Object> teamData = new HashMap<>();
                teamData.put("id", eventTeam.getId());
                teamData.put("teamId", eventTeam.getTeamId());
                teamData.put("teamName", eventTeam.getTeamName());
                teamData.put("averageSkill", eventTeam.getAverageSkill());
                
                // Filtrar solo los jugadores que siguen confirmados
                List<Map<String, Object>> playersData = new ArrayList<>();
                for (User player : eventTeam.getPlayers()) {
                    // Solo incluir si el jugador está confirmado actualmente
                    if (confirmedUserIds.contains(player.getId())) {
                        Map<String, Object> playerData = new HashMap<>();
                        playerData.put("userId", player.getId());
                        playerData.put("userName", player.getNombre() + " " + player.getApellido());
                        playersData.add(playerData);
                    }
                }
                teamData.put("players", playersData);
                teamsData.add(teamData);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("teams", teamsData);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener equipos");
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
     * Obtener jugadores que asistieron al evento para calificar (excluyendo al usuario actual)
     */
    @GetMapping("/events/{eventId}/attendees-for-rating")
    public ResponseEntity<Map<String, Object>> getAttendeesForRating(@PathVariable String eventId,
                                                                     HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            List<AttendanceVoteDTO> attendees = gameEventService.getAttendeesForRating(eventId, user.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("attendees", attendees);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener asistentes");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Obtener calificaciones que el usuario ya hizo en un evento
     */
    @GetMapping("/events/{eventId}/my-ratings")
    public ResponseEntity<Map<String, Object>> getMyRatingsForEvent(@PathVariable String eventId,
                                                                    HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            List<com.botfutbol.entity.PlayerRating> ratings = 
                    gameEventService.getRatingsByUserForEvent(eventId, user.getId());
            
            List<Map<String, Object>> ratingDTOs = new ArrayList<>();
            for (com.botfutbol.entity.PlayerRating rating : ratings) {
                Map<String, Object> ratingData = new HashMap<>();
                ratingData.put("id", rating.getId());
                ratingData.put("playerUserId", rating.getPlayer().getId());
                ratingData.put("playerName", rating.getPlayer().getNombre() + " " + rating.getPlayer().getApellido());
                ratingData.put("rating", rating.getRating());
                ratingData.put("comment", rating.getComment());
                ratingData.put("ratedAt", rating.getRatedAt());
                ratingDTOs.add(ratingData);
            }
            
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
     * Verificar si se puede calificar jugadores en un evento
     */
    @GetMapping("/events/{eventId}/can-rate")
    public ResponseEntity<Map<String, Object>> canRatePlayers(@PathVariable String eventId,
                                                               HttpServletRequest request) {
        try {
            getUserFromRequest(request); // Autenticación
            boolean canRate = gameEventService.canRatePlayers(eventId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("canRate", canRate);
            
            if (!canRate) {
                // Obtener información del evento para calcular el tiempo restante
                com.botfutbol.entity.GameEvent event = gameEventService.getEventEntity(eventId);
                if (event.getRegisteredAt() != null) {
                    java.time.LocalDateTime registeredAt = event.getRegisteredAt();
                    java.time.LocalDateTime now = java.time.LocalDateTime.now();
                    java.time.Duration duration = java.time.Duration.between(registeredAt, now);
                    long hoursPassed = duration.toHours();
                    
                    response.put("message", "El tiempo para calificar jugadores ha expirado. Solo puedes calificar durante 2 horas después de que se registre el evento.");
                    response.put("hoursPassed", hoursPassed);
                } else {
                    response.put("message", "El evento aún no ha sido registrado/finalizado por un administrador.");
                }
            }
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("canRate", false);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al verificar si se puede calificar: " + e.getMessage());
            response.put("canRate", false);
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
    
    // ==================== NOTIFICACIONES ====================
    
    /**
     * Obtener todas las notificaciones del usuario
     */
    @GetMapping("/notifications")
    public ResponseEntity<Map<String, Object>> getNotifications(HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            List<com.botfutbol.entity.Notification> notifications = notificationService.getUserNotifications(user);
            
            List<NotificationDTO> notificationDTOs = notifications.stream()
                .map(this::convertNotificationToDTO)
                .collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("notifications", notificationDTOs);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener notificaciones: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Obtener notificaciones no leídas
     */
    @GetMapping("/notifications/unread")
    public ResponseEntity<Map<String, Object>> getUnreadNotifications(HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            List<com.botfutbol.entity.Notification> notifications = notificationService.getUnreadNotifications(user);
            long unreadCount = notificationService.getUnreadCount(user);
            
            List<NotificationDTO> notificationDTOs = notifications.stream()
                .map(this::convertNotificationToDTO)
                .collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("notifications", notificationDTOs);
            response.put("unreadCount", unreadCount);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener notificaciones: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Obtener contador de notificaciones no leídas
     */
    @GetMapping("/notifications/count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            long unreadCount = notificationService.getUnreadCount(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("unreadCount", unreadCount);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener contador: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Marcar una notificación como leída
     */
    @PutMapping("/notifications/{notificationId}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable Long notificationId,
                                                           HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            notificationService.markAsRead(notificationId, user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Notificación marcada como leída");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al marcar notificación: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Marcar todas las notificaciones como leídas
     */
    @PutMapping("/notifications/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            int count = notificationService.markAllAsRead(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", count + " notificaciones marcadas como leídas");
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al marcar notificaciones: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Eliminar una notificación
     */
    @DeleteMapping("/notifications/{notificationId}")
    public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable Long notificationId,
                                                                  HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            notificationService.deleteNotification(notificationId, user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Notificación eliminada");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al eliminar notificación: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Método auxiliar para convertir Notification a DTO
     */
    private NotificationDTO convertNotificationToDTO(com.botfutbol.entity.Notification notification) {
        return new NotificationDTO(
            notification.getId(),
            notification.getTitle(),
            notification.getMessage(),
            notification.getType(),
            notification.isRead(),
            notification.getCreatedAt(),
            notification.getRelatedGroupId(),
            notification.getRelatedEventId(),
            notification.getActionUrl()
        );
    }
    
    // ==================== MENSAJES DE GRUPO ====================
    
    /**
     * Obtener mensajes de un grupo
     */
    @GetMapping("/groups/{groupId}/messages")
    public ResponseEntity<Map<String, Object>> getGroupMessages(@PathVariable String groupId,
                                                                HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            List<com.botfutbol.dto.GroupMessageDTO> messages = groupMessageService.getGroupMessages(groupId, user.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("messages", messages);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("Error al obtener mensajes del grupo {}", groupId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener mensajes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Enviar mensaje en un grupo
     */
    @PostMapping("/groups/{groupId}/messages")
    public ResponseEntity<Map<String, Object>> sendGroupMessage(@PathVariable String groupId,
                                                               @RequestBody Map<String, String> requestBody,
                                                               HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            String content = requestBody.get("content");
            if (content == null || content.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "El contenido del mensaje no puede estar vacío");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            com.botfutbol.dto.GroupMessageDTO message = groupMessageService.sendMessage(groupId, user.getId(), content);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", message);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("Error al enviar mensaje en grupo {}", groupId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al enviar mensaje: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Destacar un mensaje
     */
    @PutMapping("/groups/{groupId}/messages/{messageId}/highlight")
    public ResponseEntity<Map<String, Object>> highlightMessage(@PathVariable String groupId,
                                                               @PathVariable String messageId,
                                                               HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            com.botfutbol.dto.GroupMessageDTO message = groupMessageService.highlightMessage(messageId, user.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", message);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("Error al destacar mensaje {}", messageId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al destacar mensaje: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Fijar un mensaje
     */
    @PutMapping("/groups/{groupId}/messages/{messageId}/pin")
    public ResponseEntity<Map<String, Object>> pinMessage(@PathVariable String groupId,
                                                         @PathVariable String messageId,
                                                         @RequestBody(required = false) Map<String, Object> requestBody,
                                                         HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            
            // Obtener duración del body (opcional)
            Integer durationInDays = null;
            if (requestBody != null && requestBody.containsKey("durationInDays")) {
                Object durationObj = requestBody.get("durationInDays");
                if (durationObj instanceof Number) {
                    durationInDays = ((Number) durationObj).intValue();
                } else if (durationObj instanceof String) {
                    try {
                        durationInDays = Integer.parseInt((String) durationObj);
                    } catch (NumberFormatException e) {
                        // Ignorar si no es un número válido
                    }
                }
            }
            
            com.botfutbol.dto.GroupMessageDTO message = groupMessageService.pinMessage(messageId, user.getId(), durationInDays);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", message);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("Error al fijar mensaje {}", messageId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al fijar mensaje: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Eliminar mensaje para todos
     */
    @DeleteMapping("/groups/{groupId}/messages/{messageId}")
    public ResponseEntity<Map<String, Object>> deleteMessageForAll(@PathVariable String groupId,
                                                                 @PathVariable String messageId,
                                                                 HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            groupMessageService.deleteMessageForAll(messageId, user.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Mensaje eliminado exitosamente");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("Error al eliminar mensaje {}", messageId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al eliminar mensaje: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Eliminar mensaje para el usuario actual
     */
    @DeleteMapping("/groups/{groupId}/messages/{messageId}/for-me")
    public ResponseEntity<Map<String, Object>> deleteMessageForMe(@PathVariable String groupId,
                                                                  @PathVariable String messageId,
                                                                  HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            groupMessageService.deleteMessageForMe(messageId, user.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Mensaje eliminado para ti");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("Error al eliminar mensaje {} para usuario", messageId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al eliminar mensaje: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Editar un mensaje
     */
    @PutMapping("/groups/{groupId}/messages/{messageId}")
    public ResponseEntity<Map<String, Object>> editMessage(@PathVariable String groupId,
                                                          @PathVariable String messageId,
                                                          @RequestBody Map<String, String> requestBody,
                                                          HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            String newMessage = requestBody.get("content");
            if (newMessage == null || newMessage.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "El contenido del mensaje no puede estar vacío");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            com.botfutbol.dto.GroupMessageDTO message = groupMessageService.editMessage(messageId, user.getId(), newMessage);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", message);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("Error al editar mensaje {}", messageId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al editar mensaje: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Crear una encuesta en un grupo
     */
    @PostMapping("/groups/{groupId}/polls")
    public ResponseEntity<Map<String, Object>> createPoll(@PathVariable String groupId,
                                                          @RequestBody Map<String, Object> requestBody,
                                                          HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            String question = (String) requestBody.get("question");
            @SuppressWarnings("unchecked")
            List<String> options = (List<String>) requestBody.get("options");
            Boolean isMultipleChoice = requestBody.get("isMultipleChoice") != null 
                    ? (Boolean) requestBody.get("isMultipleChoice") : false;
            String eventId = requestBody.get("eventId") != null 
                    ? (String) requestBody.get("eventId") : null;
            java.time.LocalDateTime expiresAt = null;
            if (requestBody.get("expiresAt") != null) {
                expiresAt = java.time.LocalDateTime.parse((String) requestBody.get("expiresAt"));
            }
            
            if (question == null || question.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "La pregunta es requerida");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            if (options == null || options.size() < 2) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Una encuesta debe tener al menos 2 opciones");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            com.botfutbol.dto.GroupPollDTO poll = groupPollService.createPoll(
                    groupId, user.getId(), question, options, isMultipleChoice, expiresAt, eventId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Encuesta creada exitosamente");
            response.put("poll", poll);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("Error al crear encuesta: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al crear encuesta: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Votar en una encuesta
     */
    @PostMapping("/polls/{pollId}/vote")
    public ResponseEntity<Map<String, Object>> votePoll(@PathVariable String pollId,
                                                       @RequestBody Map<String, Object> requestBody,
                                                       HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            Integer optionIndex = (Integer) requestBody.get("optionIndex");
            
            if (optionIndex == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "El índice de opción es requerido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            com.botfutbol.dto.GroupPollDTO poll = groupPollService.votePoll(pollId, user.getId(), optionIndex);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Voto registrado exitosamente");
            response.put("poll", poll);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("Error al votar en encuesta: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al votar: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Obtener encuestas de un grupo
     */
    @GetMapping("/groups/{groupId}/polls")
    public ResponseEntity<Map<String, Object>> getGroupPolls(@PathVariable String groupId,
                                                              HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request); // Verificar autenticación
            
            List<com.botfutbol.dto.GroupPollDTO> polls = groupPollService.getGroupPolls(groupId, user.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("polls", polls);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al obtener encuestas: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener encuestas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // ==================== NOTIFICACIONES PUSH (FCM) ====================
    
    /**
     * Registrar token FCM de un dispositivo
     */
    @PostMapping("/fcm/register")
    public ResponseEntity<Map<String, Object>> registerFCMToken(@RequestBody Map<String, String> requestBody,
                                                                HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            String token = requestBody.get("token");
            String deviceType = requestBody.get("deviceType"); // "ANDROID" o "IOS"
            String deviceName = requestBody.get("deviceName");
            
            if (token == null || token.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "El token FCM es requerido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            fcmService.registerToken(user, token, deviceType, deviceName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Token FCM registrado exitosamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al registrar token FCM: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al registrar token: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Desactivar token FCM de un dispositivo
     */
    @PostMapping("/fcm/unregister")
    public ResponseEntity<Map<String, Object>> unregisterFCMToken(@RequestBody Map<String, String> requestBody,
                                                                  HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            String token = requestBody.get("token");
            
            if (token == null || token.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "El token FCM es requerido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            fcmService.unregisterToken(user, token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Token FCM desactivado exitosamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al desactivar token FCM: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al desactivar token: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // ==================== BÚSQUEDA Y SOLICITUDES DE GRUPOS ====================
    
    /**
     * Buscar grupos por nombre (públicos y privados)
     */
    @GetMapping("/groups/search")
    public ResponseEntity<Map<String, Object>> searchGroups(@RequestParam String query,
                                                            HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            List<GroupDTO> groups = groupService.searchGroups(query, user.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("groups", groups);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al buscar grupos: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al buscar grupos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Crear solicitud de ingreso a un grupo privado
     */
    @PostMapping("/groups/{groupId}/join-request")
    public ResponseEntity<Map<String, Object>> createJoinRequest(@PathVariable String groupId,
                                                                 @RequestBody(required = false) Map<String, String> requestBody,
                                                                 HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            String message = requestBody != null ? requestBody.get("message") : null;
            
            com.botfutbol.dto.GroupJoinRequestDTO joinRequest = 
                groupJoinRequestService.createJoinRequest(groupId, user.getId(), message);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Solicitud enviada exitosamente");
            response.put("request", joinRequest);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("Error al crear solicitud: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al crear solicitud: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Obtener solicitudes pendientes de un grupo (solo admins)
     */
    @GetMapping("/groups/{groupId}/join-requests/pending")
    public ResponseEntity<Map<String, Object>> getPendingJoinRequests(@PathVariable String groupId,
                                                                      HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            List<com.botfutbol.dto.GroupJoinRequestDTO> requests = 
                groupJoinRequestService.getPendingRequests(groupId, user.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("requests", requests);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        } catch (Exception e) {
            logger.error("Error al obtener solicitudes: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener solicitudes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Aprobar solicitud de ingreso (solo admins)
     */
    @PostMapping("/groups/{groupId}/join-requests/{requestId}/approve")
    public ResponseEntity<Map<String, Object>> approveJoinRequest(@PathVariable String groupId,
                                                                  @PathVariable String requestId,
                                                                  HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            com.botfutbol.dto.GroupJoinRequestDTO joinRequest = 
                groupJoinRequestService.approveRequest(requestId, user.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Solicitud aprobada exitosamente");
            response.put("request", joinRequest);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        } catch (Exception e) {
            logger.error("Error al aprobar solicitud: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al aprobar solicitud: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Rechazar solicitud de ingreso (solo admins)
     */
    @PostMapping("/groups/{groupId}/join-requests/{requestId}/reject")
    public ResponseEntity<Map<String, Object>> rejectJoinRequest(@PathVariable String groupId,
                                                                 @PathVariable String requestId,
                                                                 @RequestBody(required = false) Map<String, String> requestBody,
                                                                 HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            String reason = requestBody != null ? requestBody.get("reason") : null;
            
            com.botfutbol.dto.GroupJoinRequestDTO joinRequest = 
                groupJoinRequestService.rejectRequest(requestId, user.getId(), reason);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Solicitud rechazada");
            response.put("request", joinRequest);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        } catch (Exception e) {
            logger.error("Error al rechazar solicitud: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al rechazar solicitud: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Obtener historial de solicitudes del usuario
     */
    @GetMapping("/groups/my-join-requests")
    public ResponseEntity<Map<String, Object>> getMyJoinRequests(HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            List<com.botfutbol.dto.GroupJoinRequestDTO> requests = 
                groupJoinRequestService.getUserRequests(user.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("requests", requests);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al obtener historial de solicitudes: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener solicitudes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Cancelar solicitud pendiente
     */
    @DeleteMapping("/groups/{groupId}/join-requests/{requestId}")
    public ResponseEntity<Map<String, Object>> cancelJoinRequest(@PathVariable String groupId,
                                                                 @PathVariable String requestId,
                                                                 HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            groupJoinRequestService.cancelRequest(requestId, user.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Solicitud cancelada");
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        } catch (Exception e) {
            logger.error("Error al cancelar solicitud: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al cancelar solicitud: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
