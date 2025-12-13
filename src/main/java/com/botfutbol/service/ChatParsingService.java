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
    
    // Patrones para pagos (mejorados para detectar frases como 'Flavio pago', 'pago de Flavio', 'Flavio pago 7000')
    private static final Pattern PAYMENT_PATTERN = Pattern.compile(
        "(?i)(pagu[éeo]|pagado|transferencia|ya está|listo el pago|enviado|depositado|pago)"
    );
    // Nuevo: patrones para detectar pagos con nombre en el mensaje
    private static final Pattern PAYMENT_WITH_NAME_PATTERN_1 = Pattern.compile(
        "(?i)^([A-Za-zÀ-ÿ\\s]+)\\s*(pago|pagó|pagué|pagado|deposit[oó]|transferencia|enviado|listo el pago)(\\s*\\d+)?"
    ); // Ej: Flavio pago, Flavio pagó 7000
    private static final Pattern PAYMENT_WITH_NAME_PATTERN_2 = Pattern.compile(
        "(?i)^(pago|pagó|pagué|pagado|deposit[oó]|transferencia|enviado|listo el pago) de ([A-Za-zÀ-ÿ\\s]+)"
    ); // Ej: pago de Flavio

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
        "^\\d+[.)\\s]+\\s*([\\p{L}0-9ÁÉÍÓÚÑáéíóúñ'’\\- ]+)\\s*$"
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
            if (line.trim().isEmpty()) continue;
            ParsedMessage parsed = extractNameAndMessage(line);
            String name = parsed != null ? parsed.name : null;
            String message = parsed != null ? parsed.message : line;
            boolean recognized = false;

            // 1. Buscar pagos con nombre explícito en el mensaje
            Matcher m1 = PAYMENT_WITH_NAME_PATTERN_1.matcher(message);
            if (m1.find()) {
                String detectedName = m1.group(1).trim();
                Player player = findOrCreatePlayer(detectedName, result);
                result.paidPlayers.add(player.getName());
                result.paymentsRegistered++;
                recognized = true;
                continue;
            }
            Matcher m2 = PAYMENT_WITH_NAME_PATTERN_2.matcher(message);
            if (m2.find()) {
                String detectedName = m2.group(2).trim();
                Player player = findOrCreatePlayer(detectedName, result);
                result.paidPlayers.add(player.getName());
                result.paymentsRegistered++;
                recognized = true;
                continue;
            }

            // 2. Confirmaciones de asistencia
            if (name != null && isConfirmation(message)) {
                Player player = findOrCreatePlayer(name, result);
                result.confirmedPlayers.add(player.getName());
                result.playersConfirmed++;
                recognized = true;
                continue;
            }

            // 3. Pagos tradicionales (palabra clave, nombre del remitente)
            if (name != null && isPayment(message)) {
                Player player = findOrCreatePlayer(name, result);
                result.paidPlayers.add(player.getName());
                result.paymentsRegistered++;
                recognized = true;
                continue;
            }

            // 4. No reconocido
            if (!recognized) {
                result.unrecognizedMessages.add(line);
            }
        }
        return result;
    }
    
    /**
     * Extrae nombre y mensaje de una línea de chat
     */
    private ParsedMessage extractNameAndMessage(String line) {
        // Limpiar caracteres invisibles Unicode (como U+2060, U+200B, U+FEFF, etc.)
        String cleanLine = line.replaceAll("[\\u200B-\\u200D\\uFEFF\\u2060]", "").trim();

        // Ignorar encabezados tipo *LUNES 20 HS* o similares
        if (cleanLine.matches("^\\*?[A-ZÁÉÍÓÚÑ ]+\\*?\\s*\\d{1,2}\\s*HS\\*?$")) {
            return null;
        }

        // Intentar formato de WhatsApp con timestamp
        Matcher whatsappMatcher = WHATSAPP_NAME_PATTERN.matcher(cleanLine);
        if (whatsappMatcher.find()) {
            return new ParsedMessage(whatsappMatcher.group(1).trim(), whatsappMatcher.group(2).trim());
        }
        // Intentar formato simple
        Matcher simpleMatcher = SIMPLE_NAME_PATTERN.matcher(cleanLine);
        if (simpleMatcher.find()) {
            return new ParsedMessage(simpleMatcher.group(1).trim(), simpleMatcher.group(2).trim());
        }
        // Intentar formato de lista numerada (1. Nombre o 1) Nombre)
        Matcher numberedMatcher = NUMBERED_LIST_PATTERN.matcher(cleanLine);
        if (numberedMatcher.find()) {
            return new ParsedMessage(numberedMatcher.group(1).trim(), "");
        }
        return null;
    }
    
    /**
     * Busca un jugador existente o crea uno nuevo si no existe
     */
    private Player findOrCreatePlayer(String name, ChatParsingResult result) {
        String cleanName = cleanPlayerName(name);
        Optional<Player> playerOpt = playerRepository.findByNameIgnoreCaseAndActivoTrue(cleanName);
        if (playerOpt.isPresent()) {
            return playerOpt.get();
        } else {
            PlayerDTO dto = new PlayerDTO(cleanName, 5, "MED");
            Player newPlayer = playerService.addPlayer(dto);
            result.getNewPlayersAdded().add(cleanName);
            return newPlayer;
        }
    }
    
    /**
     * Limpia el nombre del jugador eliminando caracteres especiales
     */
    private String cleanPlayerName(String name) {
        if (name == null) return "";
        return name.replaceAll("[^A-Za-zÀ-ÿ\\s]", "").replaceAll("\\s+", " ").trim();
    }
    
    /**
     * Verifica si dos nombres son similares (para manejar apodos o errores)
     */
    private boolean isSimilarName(String name1, String name2) {
        if (name1 == null || name2 == null) return false;
        String n1 = cleanPlayerName(name1).toLowerCase();
        String n2 = cleanPlayerName(name2).toLowerCase();
        return n1.equals(n2) || n1.contains(n2) || n2.contains(n1);
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
