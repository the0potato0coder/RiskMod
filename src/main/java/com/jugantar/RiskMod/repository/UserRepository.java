package com.jugantar.RiskMod.repository;

import com.jugantar.RiskMod.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data JPA will automatically create a query for this method
    // "SELECT * FROM users WHERE username = ?"
    Optional<User> findByUsername(String username);
}
