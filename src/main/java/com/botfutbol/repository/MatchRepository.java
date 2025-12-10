package com.botfutbol.repository;

import com.botfutbol.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para administrar partidos.
 * Responsabilidad: Guardar, leer y consultar partidos usando Spring Data JPA.
 */
@Repository
public interface MatchRepository extends JpaRepository<Match, String> {
    
    /**
     * Devuelve el partido activo actual.
     */
    Optional<Match> findFirstByActiveTrue();
    
    /**
     * Devuelve todos los partidos activos.
     */
    List<Match> findByActiveTrue();
    
    /**
     * Devuelve todos los partidos inactivos.
     */
    List<Match> findByActiveFalse();
    
    /**
     * Cuenta partidos activos.
     */
    long countByActiveTrue();
}
