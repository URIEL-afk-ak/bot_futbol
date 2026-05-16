package com.botfutbol.repository;

import com.botfutbol.entity.DeletedMessage;
import com.botfutbol.entity.GroupMessage;
import com.botfutbol.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para mensajes eliminados por usuarios.
 */
@Repository
public interface DeletedMessageRepository extends JpaRepository<DeletedMessage, String> {
    
    Optional<DeletedMessage> findByMessageAndUser(GroupMessage message, User user);
    
    boolean existsByMessageAndUser(GroupMessage message, User user);
}












