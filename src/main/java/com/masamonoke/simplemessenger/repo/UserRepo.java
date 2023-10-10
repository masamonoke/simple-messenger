package com.masamonoke.simplemessenger.repo;

import com.masamonoke.simplemessenger.entities.user.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;


public interface UserRepo extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    @Modifying
    @Transactional
    @Query("""
        UPDATE User u
        SET u.isEmailConfirmed = TRUE
        WHERE u.email = :email
    """)
    void enableUser(String email);
}
