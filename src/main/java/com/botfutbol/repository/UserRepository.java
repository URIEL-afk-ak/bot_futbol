package com.botfutbol.repository;

import com.botfutbol.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    // Puedes agregar más métodos si necesitas búsquedas adicionales por usuario
}
