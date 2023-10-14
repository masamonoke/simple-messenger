package com.masamonoke.simplemessenger.api.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.masamonoke.simplemessenger.config.jwt.JwtService;
import com.masamonoke.simplemessenger.email.EmailSender;
import com.masamonoke.simplemessenger.entities.token.ConfirmationToken;
import com.masamonoke.simplemessenger.entities.token.AuthToken;
import com.masamonoke.simplemessenger.entities.user.Role;
import com.masamonoke.simplemessenger.entities.user.User;
import com.masamonoke.simplemessenger.repo.AuthTokenRepo;
import com.masamonoke.simplemessenger.repo.UserRepo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final UserRepo userRepo;
    private final AuthTokenRepo authTokenRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailSender emailSender;
    private final EmailConfirmService emailConfirmService;
    @Value("${server.port}")
    private String host;

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        var user = User
                .builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .isEnabled(true)
                .role(Role.User)
                .build();
        var savedUser = userRepo.save(user);
        var accessToken = jwtService.generateToken(user);
        log.info(accessToken);
        var refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(savedUser, accessToken);
        var uuid = UUID.randomUUID().toString();
        var confirmationToken = new ConfirmationToken(uuid, LocalDateTime.now(), LocalDateTime.now().plusMinutes(15), user);
        confirmationTokenService.saveToken(confirmationToken);
        var link = String.format("http://localhost:%s/api/v1/auth/confirm?token=%s", host, uuid);
        emailSender.send(request.email(), emailConfirmService.buildEmail(request.username(), link));
        return new AuthenticationResponse(accessToken, refreshToken);
    }


    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var authToken = new UsernamePasswordAuthenticationToken(request.username(), request.password());
        authenticationManager.authenticate(authToken);
        var user = userRepo.findByUsername(request.username()).orElseThrow();
        var accessToken = jwtService.generateToken(user);
        log.info(accessToken);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);
        return new AuthenticationResponse(accessToken, refreshToken);
    }

    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        var bearerStartIdx = 7;
        var refreshToken = authHeader.substring(bearerStartIdx);
        var username = jwtService.extractUsername(refreshToken);
        if (username != null) {
            var user = userRepo.findByUsername(username).orElseThrow();
            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);
                var authResponse = new AuthenticationResponse(accessToken, refreshToken);
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }

    private void saveUserToken(User user, String token) {
        var t = AuthToken
                .builder()
                .user(user)
                .token(token)
                .expired(false)
                .revoked(false)
                .build();
        authTokenRepo.save(t);
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = authTokenRepo.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty()) {
            return;
        }
        validUserTokens.forEach(t -> {
            t.setExpired(true);
            t.setRevoked(true);
        });
        authTokenRepo.saveAll(validUserTokens);
    }

    public String confirmToken(String token) {
        return emailConfirmService.confirmToken(token);
    }

}
