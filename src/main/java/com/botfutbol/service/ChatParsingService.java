package com.botfutbol.service;

import com.botfutbol.dto.PaymentDTO;
import com.botfutbol.dto.PlayerDTO;
import com.botfutbol.entity.Player;
import com.botfutbol.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servicio para procesar texto de chat de WhatsApp y extraer información.
 * Responsabilidad: Parsear texto, identificar jugadores, asistencias y pagos.
 */
@Service
public class ChatParsingService {
    
    private final PlayerRepository playerRepository;
    private final PlayerService playerService;
    private final PaymentService paymentService;
    
    // Patrones regex para identificar confirmaciones
    private static final Pattern CONFIRMATION_PATTERN = Pattern.compile(
        "(?i)(\\+1|voy|me anoto|confirmo|presente|asisto|cuenta conmigo|ahí estoy)"
    );
    
    // Patrones para pagos
    private static final Pattern PAYMENT_PATTERN = Pattern.compile(
        "(?i)(pagué|pagado|transferencia|ya está|listo el pago|enviado|depositado)"
    );
    
    // Patrón para extraer nombre del mensaje de WhatsApp
    // Formato típico: [HH:MM, DD/MM/YYYY] Nombre del Contacto: mensaje
    private static final Pattern WHATSAPP_NAME_PATTERN = Pattern.compile(
        "\\[\\d{1,2}:\\d{2}(?:,\\s*\\d{1,2}/\\d{1,2}/\\d{2,4})?\\]\\s*([^:]+):\\s*(.+)"
    );
    
    // Patrón alternativo para formato sin corchetes: Nombre: mensaje
    private static final Pattern SIMPLE_NAME_PATTERN = Pattern.compile(
        "^([A-Za-zÀ-ÿ\\s]+):\\s*(.+)"
    );
    
    // Patrón para lista numerada: 1. Nombre o 1) Nombre
    private static final Pattern NUMBERED_LIST_PATTERN = Pattern.compile(
        "^\\d+[.)\\s]+\\s*([A-Za-zÀ-ÿ\\s⁠]+)\\s*$"
    );
    
    public ChatParsingService(PlayerRepository playerRepository, 
                            PlayerService playerService,
                            PaymentService paymentService) {
        this.playerRepository = playerRepository;
        this.playerService = playerService;
        this.paymentService = paymentService;
    }
    
    /**
     * Resultado del procesamiento del chat
     */
    public static class ChatParsingResult {
        private int playersConfirmed = 0;
        private int paymentsRegistered = 0;
        private List<String> confirmedPlayers = new ArrayList<>();
        private List<String> paidPlayers = new ArrayList<>();
        private List<String> unrecognizedMessages = new ArrayList<>();
        private List<String> newPlayersAdded = new ArrayList<>();
        
        // Getters y setters
        public int getPlayersConfirmed() { return playersConfirmed; }
        public void setPlayersConfirmed(int playersConfirmed) { this.playersConfirmed = playersConfirmed; }
        
        public int getPaymentsRegistered() { return paymentsRegistered; }
        public void setPaymentsRegistered(int paymentsRegistered) { this.paymentsRegistered = paymentsRegistered; }
        
        public List<String> getConfirmedPlayers() { return confirmedPlayers; }
        public void setConfirmedPlayers(List<String> confirmedPlayers) { this.confirmedPlayers = confirmedPlayers; }
        
        public List<String> getPaidPlayers() { return paidPlayers; }
        public void setPaidPlayers(List<String> paidPlayers) { this.paidPlayers = paidPlayers; }
        
        public List<String> getUnrecognizedMessages() { return unrecognizedMessages; }
        public void setUnrecognizedMessages(List<String> unrecognizedMessages) { this.unrecognizedMessages = unrecognizedMessages; }
        
        public List<String> getNewPlayersAdded() { return newPlayersAdded; }
        public void setNewPlayersAdded(List<String> newPlayersAdded) { this.newPlayersAdded = newPlayersAdded; }
    }
    
    /**
     * Procesa el texto del chat y extrae información de jugadores y pagos
     */
    @Transactional
    public ChatParsingResult processChatText(String chatText) {
        ChatParsingResult result = new ChatParsingResult();
        
        if (chatText == null || chatText.trim().isEmpty()) {
            return result;
        }
        
        // Dividir el texto en líneas
        String[] lines = chatText.split("\\r?\\n");
        
        for (String line : lines) {
            if (line.trim().isEmpty()) {
                continue;
            }
            
            // Intentar extraer nombre y mensaje
            ParsedMessage parsed = extractNameAndMessage(line);
            
            if (parsed == null) {
                result.getUnrecognizedMessages().add(line);
                continue;
            }
            
            String name = parsed.name.trim();
            String message = parsed.message.trim();
            
            // Buscar o crear jugador
            Player player = findOrCreatePlayer(name);
            
            if (player == null) {
                result.getUnrecognizedMessages().add(line);
                continue;
            }
            
            // Verificar si es confirmación de asistencia
            if (isConfirmation(message)) {
                if (!player.isAttended()) {
                    try {
                        playerService.markAttendance(player.getName(), true);
                        result.setPlayersConfirmed(result.getPlayersConfirmed() + 1);
                        result.getConfirmedPlayers().add(player.getName());
                    } catch (Exception e) {
                        result.getUnrecognizedMessages().add("Error al confirmar asistencia de " + player.getName());
                    }
                }
            }
            
            // Verificar si es confirmación de pago
            if (isPayment(message)) {
                try {
                    // Registrar pago usando PaymentDTO
                    PaymentDTO paymentDTO = new PaymentDTO();
                    paymentDTO.setPlayerName(player.getName());
                    paymentDTO.setAmount(0.0); // Monto 0, se ajusta manualmente después
                    paymentDTO.setConcept("Pago detectado desde chat");
                    
                    paymentService.registerPayment(paymentDTO);
                    result.setPaymentsRegistered(result.getPaymentsRegistered() + 1);
                    result.getPaidPlayers().add(player.getName());
                } catch (Exception e) {
                    result.getUnrecognizedMessages().add("Error al registrar pago de " + player.getName());
                }
            }
        }
        
        return result;
    }
    
    /**
     * Extrae nombre y mensaje de una línea de chat
     */
    private ParsedMessage extractNameAndMessage(String line) {
        // Intentar formato de WhatsApp con timestamp
        Matcher whatsappMatcher = WHATSAPP_NAME_PATTERN.matcher(line);
        if (whatsappMatcher.find()) {
            return new ParsedMessage(whatsappMatcher.group(1), whatsappMatcher.group(2));
        }
        
        // Intentar formato simple
        Matcher simpleMatcher = SIMPLE_NAME_PATTERN.matcher(line);
        if (simpleMatcher.find()) {
            return new ParsedMessage(simpleMatcher.group(1), simpleMatcher.group(2));
        }
        
        // Intentar formato de lista numerada (1. Nombre o 1) Nombre)
        Matcher numberedMatcher = NUMBERED_LIST_PATTERN.matcher(line.trim());
        if (numberedMatcher.find()) {
            // En lista numerada, el nombre es confirmación implícita
            return new ParsedMessage(numberedMatcher.group(1), "confirmo");
        }
        
        return null;
    }
    
    /**
     * Busca un jugador existente o crea uno nuevo si no existe
     */
    private Player findOrCreatePlayer(String name) {
        // Limpiar el nombre
        String cleanName = cleanPlayerName(name);
        
        if (cleanName.isEmpty() || cleanName.length() < 2) {
            return null;
        }
        
        // Buscar jugador exacto
        Optional<Player> exactMatch = playerRepository.findByName(cleanName);
        if (exactMatch.isPresent()) {
            return exactMatch.get();
        }
        
        // Buscar jugador por coincidencia aproximada (fuzzy matching)
        List<Player> allPlayers = playerRepository.findAll();
        for (Player player : allPlayers) {
            if (isSimilarName(player.getName(), cleanName)) {
                return player;
            }
        }
        
        // Si no existe, crear nuevo jugador con nivel de habilidad por defecto (5)
        try {
            PlayerDTO newPlayerDTO = new PlayerDTO();
            newPlayerDTO.setName(cleanName);
            newPlayerDTO.setSkillLevel(5);
            
            playerService.addPlayer(newPlayerDTO);
            Optional<Player> newPlayer = playerRepository.findByNameIgnoreCase(cleanName);
            return newPlayer.orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Limpia el nombre del jugador eliminando caracteres especiales
     */
    private String cleanPlayerName(String name) {
        // Eliminar caracteres especiales comunes en nombres de contacto
        String cleaned = name.replaceAll("[~+*]", "").trim();
        
        // Eliminar números de teléfono al inicio
        cleaned = cleaned.replaceAll("^\\+?\\d+\\s+", "");
        
        // Capitalizar primera letra de cada palabra
        String[] words = cleaned.split("\\s+");
        StringBuilder result = new StringBuilder();
        
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1).toLowerCase())
                      .append(" ");
            }
        }
        
        return result.toString().trim();
    }
    
    /**
     * Verifica si dos nombres son similares (para manejar apodos o errores)
     */
    private boolean isSimilarName(String name1, String name2) {
        String n1 = name1.toLowerCase().trim();
        String n2 = name2.toLowerCase().trim();
        
        // Coincidencia exacta
        if (n1.equals(n2)) {
            return true;
        }
        
        // Coincidencia si uno contiene al otro (para apodos)
        if (n1.contains(n2) || n2.contains(n1)) {
            return true;
        }
        
        // Coincidencia por palabras (para nombres compuestos)
        String[] words1 = n1.split("\\s+");
        String[] words2 = n2.split("\\s+");
        
        for (String w1 : words1) {
            for (String w2 : words2) {
                if (w1.length() >= 3 && w2.length() >= 3 && w1.equals(w2)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Verifica si el mensaje es una confirmación de asistencia
     */
    private boolean isConfirmation(String message) {
        return CONFIRMATION_PATTERN.matcher(message).find();
    }
    
    /**
     * Verifica si el mensaje es una confirmación de pago
     */
    private boolean isPayment(String message) {
        return PAYMENT_PATTERN.matcher(message).find();
    }
    
    /**
     * Clase interna para almacenar nombre y mensaje parseados
     */
    private static class ParsedMessage {
        String name;
        String message;
        
        ParsedMessage(String name, String message) {
            this.name = name;
            this.message = message;
        }
    }
}
