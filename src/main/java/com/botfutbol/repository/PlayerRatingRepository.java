package com.botfutbol.repository;

import com.botfutbol.entity.GameEvent;
import com.botfutbol.entity.PlayerRating;
import com.botfutbol.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para administrar calificaciones de jugadores.
 */
@Repository
public interface PlayerRatingRepository extends JpaRepository<PlayerRating, String> {
    
    // Busca todas las calificaciones de un evento
    List<PlayerRating> findByEvent(GameEvent event);
    
    // Busca calificaciones de un jugador específico en un evento
    List<PlayerRating> findByEventAndPlayer(GameEvent event, User player);
    
    // Busca todas las calificaciones que recibió un jugador
    List<PlayerRating> findByPlayer(User player);
    
    // Busca todas las calificaciones que dio un usuario
    List<PlayerRating> findByRatedBy(User ratedBy);
    
    // Busca si un usuario ya calificó a un jugador en un evento
    Optional<PlayerRating> findByEventAndPlayerAndRatedBy(GameEvent event, User player, User ratedBy);
    
    // Calcula el promedio de calificaciones de un jugador
    @Query("SELECT AVG(pr.rating) FROM PlayerRating pr WHERE pr.player = :player")
    Double calculateAverageRatingByPlayer(@Param("player") User player);
    
    // Calcula el promedio de calificaciones de un jugador en eventos de un grupo
    @Query("SELECT AVG(pr.rating) FROM PlayerRating pr WHERE pr.player = :player AND pr.event.group = :group")
    Double calculateAverageRatingByPlayerAndGroup(@Param("player") User player, @Param("group") com.botfutbol.entity.Group group);
}

