package com.botfutbol.repository;

import com.botfutbol.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para administrar jugadores.
 * Responsabilidad: Guardar, leer, actualizar y eliminar jugadores usando Spring Data JPA.
 */
@Repository
public interface PlayerRepository extends JpaRepository<Player, String> {
    
    /**
     * Busca un jugador por nombre (case insensitive).
     */
    Optional<Player> findByNameIgnoreCase(String name);
    
    /**
     * Busca un jugador por nombre exacto.
     */
    Optional<Player> findByName(String name);
    
    /**
     * Elimina un jugador por nombre.
     */
    void deleteByNameIgnoreCase(String name);
    
    /**
     * Obtiene jugadores con deuda.
     */
    @Query("SELECT p FROM Player p WHERE p.totalDebt > p.totalPaid")
    List<Player> findPlayersWithDebt();
    
    /**
     * Obtiene los mejores goleadores.
     */
    List<Player> findTop10ByOrderByGoalsScoredDesc();
    
    /**
     * Busca un jugador por nombre (case insensitive) y activo.
     */
    Optional<Player> findByNameIgnoreCaseAndActivoTrue(String name);
}
