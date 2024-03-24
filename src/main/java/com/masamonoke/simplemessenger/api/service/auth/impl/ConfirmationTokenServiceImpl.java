package com.masamonoke.simplemessenger.api.service.auth.impl;

import com.masamonoke.simplemessenger.api.service.auth.ConfirmationTokenService;
import com.masamonoke.simplemessenger.entities.token.ConfirmationToken;
import com.masamonoke.simplemessenger.repo.ConfirmationTokenRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ConfirmationTokenServiceImpl implements ConfirmationTokenService {
    private final ConfirmationTokenRepo confirmationTokenRepo;

    public void saveToken(ConfirmationToken token) {
        confirmationTokenRepo.save(token);
    }

    public Optional<ConfirmationToken> getToken(String token) {
        return confirmationTokenRepo.findByToken(token);
    }

    public void setConfirmedAt(String token) {
        confirmationTokenRepo.updateConfirmedAt(token, LocalDateTime.now());
    }
}
