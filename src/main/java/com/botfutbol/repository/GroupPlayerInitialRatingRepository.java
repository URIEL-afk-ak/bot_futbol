package com.botfutbol.repository;

import com.botfutbol.entity.Group;
import com.botfutbol.entity.GroupPlayerInitialRating;
import com.botfutbol.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para administrar puntuaciones iniciales de jugadores en grupos.
 */
@Repository
public interface GroupPlayerInitialRatingRepository extends JpaRepository<GroupPlayerInitialRating, String> {
    
    // Busca la puntuación inicial de un jugador en un grupo
    Optional<GroupPlayerInitialRating> findByGroupAndPlayer(Group group, User player);
    
    // Busca todas las puntuaciones iniciales de un grupo
    List<GroupPlayerInitialRating> findByGroup(Group group);
    
    // Busca todas las puntuaciones iniciales de un jugador
    List<GroupPlayerInitialRating> findByPlayer(User player);
}

