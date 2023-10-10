package com.masamonoke.simplemessenger.api.auth;

import com.masamonoke.simplemessenger.api.service.AuthenticationRequest;
import com.masamonoke.simplemessenger.api.service.AuthenticationResponse;
import com.masamonoke.simplemessenger.api.service.AuthenticationService;
import com.masamonoke.simplemessenger.api.service.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/authenticate")
    ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PostMapping("/refresh")
    void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        authenticationService.refreshToken(request, response);
    }

    @GetMapping("/confirm")
    public String confirmEmail(@RequestParam("token") String token) {
        return authenticationService.confirmToken(token);
    }
}
