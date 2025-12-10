package com.botfutbol.repository;

import com.botfutbol.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para administrar pagos.
 * Responsabilidad: Guardar, leer y consultar pagos realizados usando Spring Data JPA.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    
    /**
     * Busca pagos por jugador (ID).
     */
    List<Payment> findByPlayerId(String playerId);
    
    /**
     * Busca pagos por nombre de jugador (case insensitive).
     */
    List<Payment> findByPlayerNameIgnoreCase(String playerName);
    
    /**
     * Calcula el total pagado por un jugador.
     */
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.playerId = :playerId")
    Double getTotalPaidByPlayer(String playerId);
}
