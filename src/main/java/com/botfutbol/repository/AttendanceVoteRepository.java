package com.botfutbol.repository;

import com.botfutbol.entity.AttendanceVote;
import com.botfutbol.entity.GameEvent;
import com.botfutbol.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para administrar votaciones de asistencia.
 */
@Repository
public interface AttendanceVoteRepository extends JpaRepository<AttendanceVote, String> {
    
    // Busca todas las votaciones de un evento
    List<AttendanceVote> findByEvent(GameEvent event);
    
    // Busca votaciones de asistencia (sí) de un evento
    List<AttendanceVote> findByEventAndAttendingTrue(GameEvent event);
    
    // Busca votaciones de no asistencia de un evento
    List<AttendanceVote> findByEventAndAttendingFalse(GameEvent event);
    
    // Busca la votación de un usuario para un evento específico
    Optional<AttendanceVote> findByEventAndUser(GameEvent event, User user);
    
    // Cuenta cuántos usuarios confirmaron asistencia
    long countByEventAndAttendingTrue(GameEvent event);
    
    // Busca todas las votaciones de un usuario
    List<AttendanceVote> findByUser(User user);
}



