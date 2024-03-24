package com.masamonoke.simplemessenger.api.service.auth;

public interface EmailConfirmService {
    String confirmToken(String token);

    String buildEmail(String name, String link);
}
