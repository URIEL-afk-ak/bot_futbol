package com.botfutbol.service;

import com.botfutbol.entity.Group;
import com.botfutbol.entity.GroupPlayerInitialRating;
import com.botfutbol.entity.User;
import com.botfutbol.repository.GroupPlayerInitialRatingRepository;
import com.botfutbol.repository.GroupRepository;
import com.botfutbol.repository.PlayerRatingRepository;
import com.botfutbol.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para administrar puntuaciones iniciales de jugadores en grupos
 * y calcular promedios que incluyen la puntuación inicial.
 */
@Service
@Transactional
public class GroupPlayerRatingService {
    
    private static final Logger logger = LoggerFactory.getLogger(GroupPlayerRatingService.class);
    
    private final GroupPlayerInitialRatingRepository initialRatingRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final PlayerRatingRepository playerRatingRepository;
    private final GroupService groupService;
    
    public GroupPlayerRatingService(
            GroupPlayerInitialRatingRepository initialRatingRepository,
            GroupRepository groupRepository,
            UserRepository userRepository,
            PlayerRatingRepository playerRatingRepository,
            GroupService groupService) {
        this.initialRatingRepository = initialRatingRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.playerRatingRepository = playerRatingRepository;
        this.groupService = groupService;
    }
    
    /**
     * Asigna o actualiza la puntuación inicial de un jugador en un grupo (solo administradores).
     */
    public GroupPlayerInitialRating setInitialRating(String groupId, Long playerUserId, 
                                                     Long adminUserId, double initialRating, String comment) {
        logger.info("Admin {} asignando puntuación inicial {} a jugador {} en grupo {}", 
                   adminUserId, initialRating, playerUserId, groupId);
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        
        // Verificar que el usuario es administrador del grupo
        if (!groupService.isUserAdminOfGroup(groupId, adminUserId)) {
            throw new IllegalStateException("Solo los administradores pueden asignar puntuaciones iniciales");
        }
        
        User player = userRepository.findById(playerUserId)
                .orElseThrow(() -> new IllegalArgumentException("Jugador no encontrado"));
        
        // Verificar que el jugador es miembro del grupo
        if (!groupService.isUserMemberOfGroup(groupId, playerUserId)) {
            throw new IllegalStateException("El jugador no es miembro del grupo");
        }
        
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("Administrador no encontrado"));
        
        // Validar rango
        if (initialRating < 0 || initialRating > 10) {
            throw new IllegalArgumentException("La puntuación inicial debe estar entre 0 y 10");
        }
        
        // Buscar si ya existe una puntuación inicial
        Optional<GroupPlayerInitialRating> existing = 
                initialRatingRepository.findByGroupAndPlayer(group, player);
        
        if (existing.isPresent()) {
            // Actualizar existente
            GroupPlayerInitialRating rating = existing.get();
            rating.setInitialRating(initialRating);
            rating.setComment(comment);
            rating.setAssignedBy(admin);
            rating = initialRatingRepository.save(rating);
            logger.info("Puntuación inicial actualizada para jugador {} en grupo {}", playerUserId, groupId);
            return rating;
        } else {
            // Crear nueva
            GroupPlayerInitialRating newRating = new GroupPlayerInitialRating(
                    group, player, admin, initialRating, comment);
            newRating = initialRatingRepository.save(newRating);
            logger.info("Nueva puntuación inicial creada para jugador {} en grupo {}", playerUserId, groupId);
            return newRating;
        }
    }
    
    /**
     * Obtiene la puntuación inicial de un jugador en un grupo.
     */
    @Transactional(readOnly = true)
    public Optional<GroupPlayerInitialRating> getInitialRating(String groupId, Long playerUserId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        
        User player = userRepository.findById(playerUserId)
                .orElseThrow(() -> new IllegalArgumentException("Jugador no encontrado"));
        
        return initialRatingRepository.findByGroupAndPlayer(group, player);
    }
    
    /**
     * Obtiene todas las puntuaciones iniciales de un grupo.
     */
    @Transactional(readOnly = true)
    public List<GroupPlayerInitialRating> getGroupInitialRatings(String groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        
        return initialRatingRepository.findByGroup(group);
    }
    
    /**
     * Calcula el promedio de un jugador en un grupo, incluyendo la puntuación inicial
     * y las calificaciones de los partidos.
     */
    @Transactional(readOnly = true)
    public double calculatePlayerAverageRating(String groupId, Long playerUserId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        
        User player = userRepository.findById(playerUserId)
                .orElseThrow(() -> new IllegalArgumentException("Jugador no encontrado"));
        
        // Obtener puntuación inicial
        Optional<GroupPlayerInitialRating> initialRatingOpt = 
                initialRatingRepository.findByGroupAndPlayer(group, player);
        
        // Obtener promedio de calificaciones de partidos en este grupo
        Double gameRatingsAverage = playerRatingRepository
                .calculateAverageRatingByPlayerAndGroup(player, group);
        
        // Calcular promedio combinado
        if (initialRatingOpt.isPresent() && gameRatingsAverage != null) {
            // Promedio entre puntuación inicial y promedio de partidos
            double initial = initialRatingOpt.get().getInitialRating();
            double gameAvg = gameRatingsAverage;
            return (initial + gameAvg) / 2.0;
        } else if (initialRatingOpt.isPresent()) {
            // Solo tiene puntuación inicial, sin partidos aún
            return initialRatingOpt.get().getInitialRating();
        } else if (gameRatingsAverage != null) {
            // Solo tiene calificaciones de partidos, sin puntuación inicial
            return gameRatingsAverage;
        } else {
            // No tiene ni puntuación inicial ni calificaciones
            return 0.0;
        }
    }
    
    /**
     * Elimina la puntuación inicial de un jugador (solo administradores).
     */
    public void removeInitialRating(String groupId, Long playerUserId, Long adminUserId) {
        logger.info("Admin {} eliminando puntuación inicial de jugador {} en grupo {}", 
                   adminUserId, playerUserId, groupId);
        
        // Verificar que el usuario es administrador del grupo
        if (!groupService.isUserAdminOfGroup(groupId, adminUserId)) {
            throw new IllegalStateException("Solo los administradores pueden eliminar puntuaciones iniciales");
        }
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        
        User player = userRepository.findById(playerUserId)
                .orElseThrow(() -> new IllegalArgumentException("Jugador no encontrado"));
        
        Optional<GroupPlayerInitialRating> rating = 
                initialRatingRepository.findByGroupAndPlayer(group, player);
        
        if (rating.isPresent()) {
            initialRatingRepository.delete(rating.get());
            logger.info("Puntuación inicial eliminada para jugador {} en grupo {}", playerUserId, groupId);
        }
    }
}

