package com.masamonoke.simplemessenger.repo;

import com.masamonoke.simplemessenger.entities.token.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TokenRepo extends JpaRepository<Token, Long> {
    @Query(value = """
        SELECT t \s
        FROM Token t \s
        INNER JOIN User u ON t.user.id = u.id \s
        WHERE u.id = :id and (t.expired = false or t.revoked = false) \s
    """)
    List<Token> findAllValidTokenByUser(Long id);

    Optional<Token> findByToken(String token);
}
