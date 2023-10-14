package com.masamonoke.simplemessenger.repo;

import com.masamonoke.simplemessenger.entities.token.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AuthTokenRepo extends JpaRepository<AuthToken, Long> {
    @Query(value = """
        SELECT t \s
        FROM AuthToken t \s
        INNER JOIN User u ON t.user.id = u.id \s
        WHERE u.id = :id and (t.expired = false or t.revoked = false) \s
    """)
    List<AuthToken> findAllValidTokenByUser(Long id);

    Optional<AuthToken> findByToken(String token);
}
