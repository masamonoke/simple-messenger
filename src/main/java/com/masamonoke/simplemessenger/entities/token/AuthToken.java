package com.masamonoke.simplemessenger.entities.token;

import com.masamonoke.simplemessenger.entities.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AuthToken {
    @Id
    @GeneratedValue
    private Long id;
    @Column(unique = true)
    private String token;
    private boolean revoked;
    private boolean expired;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
