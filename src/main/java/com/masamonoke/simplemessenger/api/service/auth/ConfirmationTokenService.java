package com.masamonoke.simplemessenger.api.service.auth;

import com.masamonoke.simplemessenger.entities.token.ConfirmationToken;

import java.util.Optional;

public interface ConfirmationTokenService {
    void saveToken(ConfirmationToken token);

    Optional<ConfirmationToken> getToken(String token);

    void setConfirmedAt(String token);
}
