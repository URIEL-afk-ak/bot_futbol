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

    // Devuelve partidos por usuario
    List<Match> findByUser(com.botfutbol.entity.User user);

    // Devuelve el partido activo actual por usuario
    Optional<Match> findFirstByActiveTrueAndUser(com.botfutbol.entity.User user);

    // Devuelve todos los partidos activos por usuario
    List<Match> findByActiveTrueAndUser(com.botfutbol.entity.User user);

    // Devuelve todos los partidos inactivos por usuario
    List<Match> findByActiveFalseAndUser(com.botfutbol.entity.User user);

    // Cuenta partidos activos por usuario
    long countByActiveTrueAndUser(com.botfutbol.entity.User user);

    // Métodos originales (si necesitas compatibilidad)
    Optional<Match> findFirstByActiveTrue();
    List<Match> findByActiveTrue();
    List<Match> findByActiveFalse();
    long countByActiveTrue();
}
