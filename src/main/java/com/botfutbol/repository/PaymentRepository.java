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

    // Busca pagos por usuario
    List<Payment> findByUser(com.botfutbol.entity.User user);

    // Busca pagos por jugador (ID) y usuario
    List<Payment> findByPlayerIdAndUser(String playerId, com.botfutbol.entity.User user);

    // Busca pagos por nombre de jugador (case insensitive) y usuario
    List<Payment> findByPlayerNameIgnoreCaseAndUser(String playerName, com.botfutbol.entity.User user);

    // Calcula el total pagado por un jugador y usuario
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.playerId = :playerId AND p.user = :user")
    Double getTotalPaidByPlayerAndUser(String playerId, com.botfutbol.entity.User user);

    // Métodos originales (si necesitas compatibilidad)
    List<Payment> findByPlayerId(String playerId);
    List<Payment> findByPlayerNameIgnoreCase(String playerName);
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.playerId = :playerId")
    Double getTotalPaidByPlayer(String playerId);
}
