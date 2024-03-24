package com.masamonoke.simplemessenger.api.service.auth;

import com.masamonoke.simplemessenger.api.dto.AuthenticationRequest;
import com.masamonoke.simplemessenger.api.dto.AuthenticationResponse;
import com.masamonoke.simplemessenger.api.dto.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface AuthenticationService {
    AuthenticationResponse register(RegisterRequest request);

    AuthenticationResponse authenticate(AuthenticationRequest request);

    String confirmToken(String token);

    void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
