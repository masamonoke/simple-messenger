package com.masamonoke.simplemessenger.repo;

import com.masamonoke.simplemessenger.entities.token.ConfirmationToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;


public interface ConfirmationTokenRepo extends JpaRepository<ConfirmationToken, Long> {
    Optional<ConfirmationToken> findByToken(String token);

    @Transactional
    @Modifying
    @Query("""
        UPDATE ConfirmationToken c
        SET c.confirmedAt = :confirmedAt
        WHERE c.token = :token
    """)
    void updateConfirmedAt(String token, LocalDateTime confirmedAt);
}
