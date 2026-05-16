package com.botfutbol.repository;

import com.botfutbol.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para administrar jugadores.
 * Responsabilidad: Guardar, leer, actualizar y eliminar jugadores usando Spring Data JPA.
 */
@Repository
public interface PlayerRepository extends JpaRepository<Player, String> {

    // Busca jugadores por usuario
    List<Player> findByUser(com.botfutbol.entity.User user);

    // Busca un jugador por nombre (case insensitive) y usuario
    Optional<Player> findByNameIgnoreCaseAndUser(String name, com.botfutbol.entity.User user);

    // Busca un jugador por nombre exacto y usuario
    Optional<Player> findByNameAndUser(String name, com.botfutbol.entity.User user);

    // Elimina un jugador por nombre y usuario
    void deleteByNameIgnoreCaseAndUser(String name, com.botfutbol.entity.User user);

    // Obtiene jugadores con deuda por usuario
    @Query("SELECT p FROM Player p WHERE p.totalDebt > p.totalPaid AND p.user = :user")
    List<Player> findPlayersWithDebtByUser(com.botfutbol.entity.User user);

    // Obtiene los mejores goleadores por usuario
    List<Player> findTop10ByUserOrderByGoalsScoredDesc(com.botfutbol.entity.User user);

    // Busca un jugador por nombre (case insensitive), activo y usuario
    Optional<Player> findByNameIgnoreCaseAndActivoTrueAndUser(String name, com.botfutbol.entity.User user);

    // Obtiene todos los jugadores activos por usuario
    List<Player> findAllByActivoTrueAndUser(com.botfutbol.entity.User user);

    // Desmarca asistencia de todos los jugadores de un usuario (actualización masiva)
    @Modifying
    @Query("UPDATE Player p SET p.attended = false, p.attendanceStatus = 'NO_VA' WHERE p.user = :user")
    int unmarkAllAttendanceByUser(@Param("user") com.botfutbol.entity.User user);

    // Métodos originales (si necesitas compatibilidad)
    Optional<Player> findByNameIgnoreCase(String name);
    Optional<Player> findByName(String name);
    void deleteByNameIgnoreCase(String name);
    @Query("SELECT p FROM Player p WHERE p.totalDebt > p.totalPaid")
    List<Player> findPlayersWithDebt();
    List<Player> findTop10ByOrderByGoalsScoredDesc();
    Optional<Player> findByNameIgnoreCaseAndActivoTrue(String name);
    List<Player> findAllByActivoTrue();
}
