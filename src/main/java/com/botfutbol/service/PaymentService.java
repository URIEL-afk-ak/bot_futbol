package com.botfutbol.service;

import com.botfutbol.dto.PaymentDTO;
import com.botfutbol.entity.Payment;
import com.botfutbol.entity.Player;
import com.botfutbol.repository.PaymentRepository;
import com.botfutbol.repository.PlayerRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para administrar pagos.
 * Responsabilidad: Lógica de negocio relacionada con pagos y deudas.
 */
@Service
@Transactional
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final PlayerRepository playerRepository;
    
    public PaymentService(PaymentRepository paymentRepository, PlayerRepository playerRepository) {
        this.paymentRepository = paymentRepository;
        this.playerRepository = playerRepository;
    }
    
    /**
     * Registra un pago de un jugador.
     */
    public Payment registerPayment(PaymentDTO paymentDTO) {
        // Validar datos
        if (paymentDTO.getPlayerName() == null || paymentDTO.getPlayerName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del jugador no puede estar vacío");
        }
        
        if (paymentDTO.getAmount() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a 0");
        }
        
        // Buscar el jugador
        Optional<Player> playerOpt = playerRepository.findByNameIgnoreCase(paymentDTO.getPlayerName());
        if (playerOpt.isEmpty()) {
            throw new IllegalArgumentException("Jugador no encontrado: " + paymentDTO.getPlayerName());
        }
        
        Player player = playerOpt.get();
        
        // Crear el pago
        Payment payment = new Payment(
                player.getId(),
                player.getName(),
                paymentDTO.getAmount(),
                paymentDTO.getConcept()
        );
        
        // Actualizar total pagado del jugador
        player.setTotalPaid(player.getTotalPaid() + paymentDTO.getAmount());
        playerRepository.save(player);
        
        return paymentRepository.save(payment);
    }
    
    /**
     * Registra deuda para un jugador (por jugar un partido).
     */
    public void addDebtToPlayer(String playerId, double amount) {
        Optional<Player> playerOpt = playerRepository.findById(playerId);
        if (playerOpt.isPresent()) {
            Player player = playerOpt.get();
            player.setTotalDebt(player.getTotalDebt() + amount);
            playerRepository.save(player);
        }
    }
    
    /**
     * Obtiene todos los pagos de un jugador.
     */
    public List<Payment> getPaymentsByPlayer(String playerName) {
        return paymentRepository.findByPlayerNameIgnoreCase(playerName);
    }
    
    /**
     * Calcula el balance (deuda - pagado) de un jugador.
     */
    public double getPlayerBalance(String playerName) {
        Optional<Player> playerOpt = playerRepository.findByNameIgnoreCase(playerName);
        if (playerOpt.isEmpty()) {
            throw new IllegalArgumentException("Jugador no encontrado");
        }
        
        Player player = playerOpt.get();
        return player.getTotalPaid() - player.getTotalDebt();
    }
    
    /**
     * Obtiene la deuda pendiente de un jugador.
     */
    public double getPlayerDebt(String playerName) {
        double balance = getPlayerBalance(playerName);
        return balance < 0 ? Math.abs(balance) : 0;
    }
    
    /**
     * Obtiene todos los pagos registrados.
     */
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
}
