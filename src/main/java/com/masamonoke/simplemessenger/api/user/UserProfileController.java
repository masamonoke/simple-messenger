package com.masamonoke.simplemessenger.api.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.masamonoke.simplemessenger.entities.user.User;
import jakarta.persistence.PreUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile/user")
@RequiredArgsConstructor
public class UserProfileController {
    private final UserProfileService userProfileService;

    private String getTokenFromHeader(String header) {
        var tokenStartIdx = 7;
        return header.substring(tokenStartIdx);
    }

    @GetMapping()
    ResponseEntity<User> getUserById(@RequestParam("id") Long id, @RequestHeader("Authorization") String header) throws JsonProcessingException {
        var token = getTokenFromHeader(header);
        return ResponseEntity.ok(userProfileService.getUserById(id, token));
    }

    @PutMapping("/first_name")
    ResponseEntity<User> updateFirstName(@RequestParam("first_name") String firstName, @RequestParam("id") Long id,
                                         @RequestHeader("Authorization") String header) throws JsonProcessingException {
        var token = getTokenFromHeader(header);
        return ResponseEntity.ok(userProfileService.updateFirstName(id, firstName, token));
    }

    @PutMapping("/last_name")
    ResponseEntity<User> updateLastName(@RequestParam("last_name") String lastName, @RequestParam("id") Long id,
                                         @RequestHeader("Authorization") String header) throws JsonProcessingException {
        var token = getTokenFromHeader(header);
        return ResponseEntity.ok(userProfileService.updateLastName(id, lastName, token));
    }

    @PutMapping("/password")
    ResponseEntity<User> updatePassword(@RequestBody User user, @RequestHeader("Authorization") String header) throws JsonProcessingException {
        var token = getTokenFromHeader(header);
        return ResponseEntity.ok(userProfileService.updatePassword(user, token));
    }

    @PutMapping("/email")
    ResponseEntity<User> updateEmail(@RequestParam("email") String email, @RequestParam("id") Long id, @RequestHeader("Authorization") String header) throws JsonProcessingException {
        var token = getTokenFromHeader(header);
        return ResponseEntity.ok(userProfileService.updateEmail(id, email, token));
    }

    @DeleteMapping
    void deleteAccount(@RequestParam("id") Long id, @RequestHeader("Authorization") String header) throws JsonProcessingException {
        var token = getTokenFromHeader(header);
        userProfileService.deleteAccount(id, token);
    }

    @PutMapping("/restore")
    ResponseEntity<User> restoreAccount(@RequestBody User user) {
        return ResponseEntity.ok(userProfileService.restoreAccount(user));
    }
}
