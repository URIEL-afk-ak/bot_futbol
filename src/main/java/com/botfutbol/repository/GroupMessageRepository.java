package com.botfutbol.repository;

import com.botfutbol.entity.Group;
import com.botfutbol.entity.GroupMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para administrar mensajes de grupos.
 */
@Repository
public interface GroupMessageRepository extends JpaRepository<GroupMessage, String> {
    
    // Busca todos los mensajes de un grupo ordenados por fecha
    List<GroupMessage> findByGroupOrderByCreatedAtAsc(Group group);
    
    // Busca los últimos N mensajes de un grupo
    List<GroupMessage> findTop50ByGroupOrderByCreatedAtDesc(Group group);
}

