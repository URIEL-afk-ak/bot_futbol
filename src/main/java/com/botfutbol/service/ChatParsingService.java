package com.botfutbol.service;

import com.botfutbol.dto.PlayerDTO;
import com.botfutbol.entity.Player;
import com.botfutbol.entity.User;
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
    
    // Patrones para comandos de asistencia masiva
    private static final Pattern MASS_ATTENDANCE_PATTERN = Pattern.compile(
        "(?i)(todos\\s+asisten|todos\\s+van|todos\\s+confirmados|marcar\\s+asistencia|asistencia\\s+masiva|todos\\s+presentes|confirmar\\s+todos)"
    );
    
    
    public ChatParsingService(PlayerRepository playerRepository, 
                            PlayerService playerService) {
        this.playerRepository = playerRepository;
        this.playerService = playerService;
    }
    
    /**
     * Resultado del procesamiento del chat
     */
    public static class ChatParsingResult {
        private int playersConfirmed = 0;
        private int paymentsRegistered = 0;
        private int attendanceMarked = 0; // Nuevo: cantidad de asistencias marcadas masivamente
        private List<String> confirmedPlayers = new ArrayList<>();
        private List<String> paidPlayers = new ArrayList<>();
        private List<String> attendanceMarkedPlayers = new ArrayList<>(); // Nuevo: jugadores con asistencia marcada
        private List<String> unrecognizedMessages = new ArrayList<>();
        private List<String> newPlayersAdded = new ArrayList<>();
        
        // Getters y setters
        public int getPlayersConfirmed() { return playersConfirmed; }
        public void setPlayersConfirmed(int playersConfirmed) { this.playersConfirmed = playersConfirmed; }
        public int getPaymentsRegistered() { return paymentsRegistered; }
        public void setPaymentsRegistered(int paymentsRegistered) { this.paymentsRegistered = paymentsRegistered; }
        public int getAttendanceMarked() { return attendanceMarked; }
        public void setAttendanceMarked(int attendanceMarked) { this.attendanceMarked = attendanceMarked; }
        public List<String> getConfirmedPlayers() { return confirmedPlayers; }
        public void setConfirmedPlayers(List<String> confirmedPlayers) { this.confirmedPlayers = confirmedPlayers; }
        public List<String> getPaidPlayers() { return paidPlayers; }
        public void setPaidPlayers(List<String> paidPlayers) { this.paidPlayers = paidPlayers; }
        public List<String> getAttendanceMarkedPlayers() { return attendanceMarkedPlayers; }
        public void setAttendanceMarkedPlayers(List<String> attendanceMarkedPlayers) { this.attendanceMarkedPlayers = attendanceMarkedPlayers; }
        public List<String> getUnrecognizedMessages() { return unrecognizedMessages; }
        public void setUnrecognizedMessages(List<String> unrecognizedMessages) { this.unrecognizedMessages = unrecognizedMessages; }
        public List<String> getNewPlayersAdded() { return newPlayersAdded; }
        public void setNewPlayersAdded(List<String> newPlayersAdded) { this.newPlayersAdded = newPlayersAdded; }
    }
    
    /**
     * Procesa el texto del chat y extrae información de jugadores, pagos y asistencias
     */
    @Transactional
    public ChatParsingResult processChatText(String chatText, User user) {
        ChatParsingResult result = new ChatParsingResult();
        if (chatText == null || chatText.trim().isEmpty()) {
            return result;
        }
        
        // Primero, verificar si hay un comando de asistencia masiva en todo el texto
        String normalizedText = chatText.toLowerCase().trim();
        boolean hasMassAttendanceCommand = MASS_ATTENDANCE_PATTERN.matcher(normalizedText).find();
        
        // Si hay comando de asistencia masiva, procesar lista de jugadores
        if (hasMassAttendanceCommand) {
            List<String> playerNames = extractPlayerNamesFromList(chatText);
            if (!playerNames.isEmpty()) {
                markMassAttendance(playerNames, result, user);
            }
        }
        
        // Dividir el texto en líneas para procesamiento individual
        String[] lines = chatText.split("\\r?\\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            
            // Si esta línea ya fue procesada en asistencia masiva, saltarla
            if (hasMassAttendanceCommand && isPlayerNameLine(line)) {
                continue;
            }
            
            ParsedMessage parsed = extractNameAndMessage(line);
            String name = parsed != null ? parsed.name : null;
            String message = parsed != null ? parsed.message : line;
            boolean recognized = false;

            // 1. Buscar pagos con nombre explícito en el mensaje
            Matcher m1 = PAYMENT_WITH_NAME_PATTERN_1.matcher(message);
            if (m1.find()) {
                String detectedName = m1.group(1).trim();
                Player player = findOrCreatePlayer(detectedName, result, user);
                result.paidPlayers.add(player.getName());
                result.paymentsRegistered++;
                recognized = true;
                continue;
            }
            Matcher m2 = PAYMENT_WITH_NAME_PATTERN_2.matcher(message);
            if (m2.find()) {
                String detectedName = m2.group(2).trim();
                Player player = findOrCreatePlayer(detectedName, result, user);
                result.paidPlayers.add(player.getName());
                result.paymentsRegistered++;
                recognized = true;
                continue;
            }

            // 2. Detectar listas de jugadores sin comando (solo agregar jugadores, NO marcar asistencia)
            // Esto debe ir ANTES de las confirmaciones individuales para evitar marcar asistencia por error
            if (isPlayerNameLine(line) && !hasMassAttendanceCommand) {
                String playerName = extractPlayerNameFromLine(line);
                if (playerName != null && !playerName.trim().isEmpty()) {
                    // Solo agregar el jugador, NO marcar asistencia
                    findOrCreatePlayer(playerName, result, user);
                    recognized = true;
                    continue;
                }
            }

            // 3. Confirmaciones de asistencia individual (solo si hay mensaje de confirmación explícito)
            // NO procesar si el mensaje está vacío (eso es solo una lista, no una confirmación)
            if (name != null && isConfirmation(message) && !message.isEmpty()) {
                Player player = findOrCreatePlayer(name, result, user);
                // Marcar asistencia solo si hay confirmación explícita
                try {
                    playerService.markAttendance(player.getName(), true, user);
                    result.confirmedPlayers.add(player.getName());
                    result.playersConfirmed++;
                } catch (Exception e) {
                    // Si falla, solo agregar a confirmados sin marcar asistencia
                    result.confirmedPlayers.add(player.getName());
                    result.playersConfirmed++;
                }
                recognized = true;
                continue;
            }

            // 4. Pagos tradicionales (palabra clave, nombre del remitente)
            if (name != null && isPayment(message)) {
                Player player = findOrCreatePlayer(name, result, user);
                result.paidPlayers.add(player.getName());
                result.paymentsRegistered++;
                recognized = true;
                continue;
            }

            // 5. No reconocido
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
    private Player findOrCreatePlayer(String name, ChatParsingResult result, User user) {
        String cleanName = cleanPlayerName(name);
        if (cleanName.isEmpty()) {
            throw new IllegalArgumentException("Nombre de jugador inválido");
        }
        
        // Buscar jugador existente (activo o inactivo) para este usuario
        Optional<Player> playerOpt = playerRepository.findByNameIgnoreCaseAndUser(cleanName, user);
        if (playerOpt.isPresent()) {
            Player player = playerOpt.get();
            // Si está inactivo, reactivarlo
            if (!player.isActivo()) {
                player.setActivo(true);
                playerRepository.save(player);
            }
            return player;
        } else {
            // Verificar una vez más antes de crear (para evitar race conditions)
            playerOpt = playerRepository.findByNameIgnoreCaseAndUser(cleanName, user);
            if (playerOpt.isPresent()) {
                return playerOpt.get();
            }
            
            // Crear nuevo jugador
            try {
                PlayerDTO dto = new PlayerDTO(cleanName, 5, "MED");
                Player newPlayer = playerService.addPlayer(dto, user);
                result.getNewPlayersAdded().add(cleanName);
                return newPlayer;
            } catch (Exception e) {
                // Si falla por duplicado, intentar buscar de nuevo
                playerOpt = playerRepository.findByNameIgnoreCaseAndUser(cleanName, user);
                if (playerOpt.isPresent()) {
                    return playerOpt.get();
                }
                throw e;
            }
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
     * Extrae nombres de jugadores de una lista (formato: nombres separados por comas, saltos de línea, guiones, etc.)
     */
    private List<String> extractPlayerNamesFromList(String text) {
        List<String> names = new ArrayList<>();
        
        // Dividir por líneas
        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            
            // Ignorar líneas con comandos de asistencia masiva
            if (MASS_ATTENDANCE_PATTERN.matcher(line.toLowerCase()).find()) {
                continue;
            }
            
            // Intentar extraer nombres de diferentes formatos
            
            // Formato lista numerada: 1. Nombre, 2. Nombre
            Matcher numberedMatcher = NUMBERED_LIST_PATTERN.matcher(line);
            if (numberedMatcher.find()) {
                String name = numberedMatcher.group(1).trim();
                if (!name.isEmpty() && !isCommand(name)) {
                    names.add(cleanPlayerName(name));
                }
                continue;
            }
            
            // Formato con separadores: Nombre1, Nombre2, Nombre3
            // o Nombre1 - Nombre2 - Nombre3
            // o Nombre1 • Nombre2 • Nombre3
            if (line.contains(",") || line.contains("-") || line.contains("•") || line.contains("*")) {
                String[] parts = line.split("[,•*\\-]");
                for (String part : parts) {
                    String name = cleanPlayerName(part.trim());
                    if (!name.isEmpty() && name.length() > 1 && !isCommand(name)) {
                        names.add(name);
                    }
                }
                continue;
            }
            
            // Si es una línea simple con un nombre (sin formato especial)
            String cleanName = cleanPlayerName(line);
            if (!cleanName.isEmpty() && cleanName.length() > 1 && !isCommand(cleanName)) {
                // Verificar que no sea un comando
                if (!MASS_ATTENDANCE_PATTERN.matcher(cleanName.toLowerCase()).find()) {
                    names.add(cleanName);
                }
            }
        }
        
        return names;
    }
    
    /**
     * Verifica si una línea contiene un nombre de jugador (no un comando)
     */
    private boolean isPlayerNameLine(String line) {
        String clean = line.trim().toLowerCase();
        // Ignorar comandos
        if (MASS_ATTENDANCE_PATTERN.matcher(clean).find()) {
            return false;
        }
        if (PAYMENT_PATTERN.matcher(clean).find()) {
            return false;
        }
        // Debe contener al menos una letra
        return clean.matches(".*[a-záéíóúñ].*");
    }
    
    /**
     * Extrae el nombre de jugador de una línea
     */
    private String extractPlayerNameFromLine(String line) {
        // Intentar formato numerado primero
        Matcher numberedMatcher = NUMBERED_LIST_PATTERN.matcher(line);
        if (numberedMatcher.find()) {
            return numberedMatcher.group(1).trim();
        }
        
        // Limpiar y devolver
        return cleanPlayerName(line);
    }
    
    /**
     * Verifica si un texto es un comando (no un nombre de jugador)
     */
    private boolean isCommand(String text) {
        String lower = text.toLowerCase().trim();
        return MASS_ATTENDANCE_PATTERN.matcher(lower).find() ||
               PAYMENT_PATTERN.matcher(lower).find() ||
               CONFIRMATION_PATTERN.matcher(lower).find() ||
               lower.length() < 2;
    }
    
    /**
     * Marca asistencia masiva para una lista de jugadores
     */
    private void markMassAttendance(List<String> playerNames, ChatParsingResult result, User user) {
        for (String playerName : playerNames) {
            if (playerName == null || playerName.trim().isEmpty()) {
                continue;
            }
            
            String cleanName = cleanPlayerName(playerName);
            if (cleanName.isEmpty()) {
                continue;
            }
            
            try {
                // Buscar jugador existente (activo o inactivo)
                Optional<Player> playerOpt = playerRepository.findByNameIgnoreCaseAndUser(cleanName, user);
                Player player;
                
                if (playerOpt.isPresent()) {
                    player = playerOpt.get();
                    // Si está inactivo, reactivarlo
                    if (!player.isActivo()) {
                        player.setActivo(true);
                        playerRepository.save(player);
                    }
                } else {
                    // Verificar una vez más antes de crear (para evitar race conditions)
                    playerOpt = playerRepository.findByNameIgnoreCaseAndUser(cleanName, user);
                    if (playerOpt.isPresent()) {
                        player = playerOpt.get();
                        if (!player.isActivo()) {
                            player.setActivo(true);
                            playerRepository.save(player);
                        }
                    } else {
                        // Crear nuevo jugador si no existe
                        try {
                            PlayerDTO dto = new PlayerDTO(cleanName, 5, "MED");
                            player = playerService.addPlayer(dto, user);
                            result.getNewPlayersAdded().add(cleanName);
                        } catch (Exception e) {
                            // Si falla por duplicado, intentar buscar de nuevo
                            playerOpt = playerRepository.findByNameIgnoreCaseAndUser(cleanName, user);
                            if (playerOpt.isPresent()) {
                                player = playerOpt.get();
                                if (!player.isActivo()) {
                                    player.setActivo(true);
                                    playerRepository.save(player);
                                }
                            } else {
                                throw e;
                            }
                        }
                    }
                }
                
                // Marcar asistencia
                playerService.markAttendance(player.getName(), true, user);
                result.attendanceMarkedPlayers.add(player.getName());
                result.attendanceMarked++;
                
            } catch (Exception e) {
                // Si falla, agregar a no reconocidos
                result.getUnrecognizedMessages().add("Error al marcar asistencia de: " + cleanName + " - " + e.getMessage());
            }
        }
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
