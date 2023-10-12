package com.masamonoke.simplemessenger.api.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.masamonoke.simplemessenger.api.auth.service.ConfirmationTokenService;
import com.masamonoke.simplemessenger.api.auth.service.EmailConfirmService;
import com.masamonoke.simplemessenger.email.EmailSender;
import com.masamonoke.simplemessenger.entities.token.ConfirmationToken;
import com.masamonoke.simplemessenger.entities.user.User;
import com.masamonoke.simplemessenger.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.masamonoke.simplemessenger.api.Utils.decodeToken;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailConfirmService emailConfirmService;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailSender emailSender;
    @Value("${server.port}")
    private String host;

    private boolean isValidUser(String token, User user) throws JsonProcessingException {
        var map = decodeToken(token);
        var username = map.get("sub");
        return username.equals(user.getUsername());
    }

    private IllegalStateException produceUserException(User user, Long id) {
        if (user == null) {
            return new IllegalStateException(String.format("User with id %d not found", id));
        } else {
            return new IllegalStateException(String.format("Current request user cannot update user with id=%d", id));
        }
    }

    // TODO: check user role. If it admin role then that user can get any user profile
    User getUserById(Long id, String token) throws JsonProcessingException {
        var user = userRepo.findById(id).orElse(null);
        if (user != null && isValidUser(token, user)) {
            return user;
        }
        throw produceUserException(user, id);
    }

    User updateFirstName(Long id, String firstName, String token) throws JsonProcessingException {
        var user = userRepo.findById(id).orElseThrow(() -> new InvalidParameterException(String.format("User with id=%d not found", id)));
        if (isValidUser(token, user)) {
            user.setFirstName(firstName);
            return userRepo.save(user);
        }
        throw produceUserException(user, id);
    }

    User updateLastName(Long id, String lastName, String token) throws JsonProcessingException {
        var user = userRepo.findById(id).orElseThrow(() -> new InvalidParameterException(String.format("User with id=%d not found", id)));
        if (isValidUser(token, user)) {
            user.setLastName(lastName);
            return userRepo.save(user);
        }
        throw produceUserException(user, id);
    }

    public User updatePassword(User user, String token) throws JsonProcessingException {
        var savedUser = userRepo.findById(user.getId())
                .orElseThrow(() -> new InvalidParameterException(String.format("User with id=%d not found", user.getId())));
        if (!isValidUser(token, savedUser)) {
            throw produceUserException(savedUser, user.getId());
        }
        savedUser.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepo.save(savedUser);
    }

    public User updateEmail(Long id, String email, String token) throws JsonProcessingException {
        var user = userRepo.findById(id).orElseThrow(() -> new InvalidParameterException(String.format("User with id=%d not found", id)));
        if (!isValidUser(token, user)) {
            throw produceUserException(user, id);
        }
        user.setEmailConfirmed(false);
        user.setEmail(email);
        var uuid = UUID.randomUUID().toString();
        var confirmationToken = new ConfirmationToken(uuid, LocalDateTime.now(), LocalDateTime.now().plusMinutes(15), user);
        confirmationTokenService.saveToken(confirmationToken);
        var link = String.format("http://localhost:%s/api/v1/auth/confirm?token=%s", host, uuid);
        emailSender.send(email, emailConfirmService.buildEmail(user.getUsername(), link));
        return user;
    }

    public void deleteAccount(Long id, String token) throws JsonProcessingException {
        var user = userRepo.findById(id).orElseThrow(() -> new InvalidParameterException(String.format("User with id=%d not found", id)));
        if (!isValidUser(token, user)) {
            throw produceUserException(user, id);
        }
        user.setEnabled(false);
        userRepo.save(user);
    }

    // TODO: enable account after email sent link confirmation
    public User restoreAccount(User user) {
        var savedUser = userRepo
                .findByUsername(user.getUsername())
                .orElseThrow(() -> new InvalidParameterException(String.format("User with username=%s not found", user.getUsername())));
        var isUsernameEq = savedUser.getUsername().equals(user.getUsername());
        var isEmailEq = savedUser.getEmail().equals(user.getEmail());
        if (isEmailEq && isUsernameEq) {
            savedUser.setEnabled(true);
            // TODO: check if password was used
            savedUser.setPassword(user.getPassword());
            return userRepo.save(savedUser);
        }
        throw new InvalidParameterException(String.format("There is no user with username=%s or email=$s", user.getEmail(), user.getEmail()));
    }
}
