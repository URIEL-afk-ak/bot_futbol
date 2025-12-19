package com.botfutbol.service;

import com.botfutbol.dto.PaymentDTO;
import com.botfutbol.entity.Payment;
import com.botfutbol.entity.Player;
import com.botfutbol.entity.User;
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
    public Payment registerPayment(PaymentDTO paymentDTO, User user) {
        // Validar datos
        if (paymentDTO.getPlayerName() == null || paymentDTO.getPlayerName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del jugador no puede estar vacío");
        }
        
        if (paymentDTO.getAmount() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a 0");
        }
        
        // Buscar el jugador
        Optional<Player> playerOpt = playerRepository.findByNameIgnoreCaseAndUser(paymentDTO.getPlayerName(), user);
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
        payment.setUser(user);
        
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
    public List<Payment> getPaymentsByPlayer(String playerName, User user) {
        return paymentRepository.findByPlayerNameIgnoreCaseAndUser(playerName, user);
    }
    
    /**
     * Calcula el balance (deuda - pagado) de un jugador.
     */
    public double getPlayerBalance(String playerName, User user) {
        Optional<Player> playerOpt = playerRepository.findByNameIgnoreCaseAndUser(playerName, user);
        if (playerOpt.isEmpty()) {
            throw new IllegalArgumentException("Jugador no encontrado");
        }
        
        Player player = playerOpt.get();
        return player.getTotalPaid() - player.getTotalDebt();
    }
    
    /**
     * Obtiene la deuda pendiente de un jugador.
     */
    public double getPlayerDebt(String playerName, User user) {
        double balance = getPlayerBalance(playerName, user);
        return balance < 0 ? Math.abs(balance) : 0;
    }
    
    /**
     * Obtiene todos los pagos registrados.
     */
    public List<Payment> getAllPayments(User user) {
        return paymentRepository.findByUser(user);
    }

    /**
     * Edita un pago existente.
     */
    public Payment updatePayment(String id, PaymentDTO dto, User user) {
        Optional<Payment> optionalPayment = paymentRepository.findById(id);
        if (optionalPayment.isPresent()) {
            Payment payment = optionalPayment.get();
            // Verificar que el pago pertenece al usuario
            if (payment.getUser() == null || !payment.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Pago no encontrado o no autorizado");
            }
            
            double oldAmount = payment.getAmount();
            payment.setAmount(dto.getAmount());
            payment.setConcept(dto.getConcept());

            // Actualizar total pagado del jugador si cambió el monto
            Optional<Player> playerOpt = playerRepository.findByNameIgnoreCaseAndUser(payment.getPlayerName(), user);
            if (playerOpt.isPresent()) {
                Player player = playerOpt.get();
                player.setTotalPaid(player.getTotalPaid() - oldAmount + dto.getAmount());
                playerRepository.save(player);
            }

            return paymentRepository.save(payment);
        } else {
            throw new RuntimeException("Pago no encontrado");
        }
    }

    /**
     * Elimina un pago existente.
     */
    public void deletePayment(String id, User user) {
        Optional<Payment> paymentOpt = paymentRepository.findById(id);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            // Verificar que el pago pertenece al usuario
            if (payment.getUser() == null || !payment.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Pago no encontrado o no autorizado");
            }
            paymentRepository.deleteById(id);
        } else {
            throw new RuntimeException("Pago no encontrado");
        }
    }

    /**
     * Elimina todos los pagos registrados del usuario.
     */
    public void deleteAllPayments(User user) {
        List<Payment> payments = paymentRepository.findByUser(user);
        paymentRepository.deleteAll(payments);
    }
}
