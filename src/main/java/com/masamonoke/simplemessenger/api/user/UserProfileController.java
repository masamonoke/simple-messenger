package com.masamonoke.simplemessenger.api.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.masamonoke.simplemessenger.entities.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

import static com.masamonoke.simplemessenger.api.Utils.decodeToken;
import static com.masamonoke.simplemessenger.api.Utils.getTokenFromHeader;

@RestController
@RequestMapping("/api/v1/profile/user")
@RequiredArgsConstructor
public class UserProfileController {
    private final UserProfileService userProfileService;

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

    @PutMapping("/add_friend")
    ResponseEntity<UserDisplay> addFriend(
            @RequestParam("friend") String friendUsername, @RequestHeader("Authorization") String header) throws JsonProcessingException {
        var token = getTokenFromHeader(header);
        var username = decodeToken(token).get("sub");
        return ResponseEntity.ok(userProfileService.addFriend(username, friendUsername));
    }

    @GetMapping("/friends")
    ResponseEntity<Set<User>> getFriendsList(@RequestHeader("Authorization") String header) throws JsonProcessingException {
        var token = getTokenFromHeader(header);
        var username = decodeToken(token).get("sub");
        return ResponseEntity.ok(userProfileService.getFriends(username));
    }

    @GetMapping("friends/{user_id}")
    ResponseEntity<Set<User>> getAnotherUserFriendsList(@PathVariable Long user_id) {
        return ResponseEntity.ok(userProfileService.getAnotherUserFriendsList(user_id));
    }

    @PutMapping("hide_friends")
    ResponseEntity<User> hideFriends(@RequestHeader("Authorization") String header, @RequestParam("hide") boolean hide) throws JsonProcessingException {
        var token = getTokenFromHeader(header);
        var username = decodeToken(token).get("sub");
        return ResponseEntity.ok(userProfileService.hideFriends(username, hide));
    }

    @PutMapping("restrict_messages")
    ResponseEntity<User> restrictMessages(
            @RequestParam("restrict") boolean restrict, @RequestHeader("Authorization") String header) throws JsonProcessingException {
        var token = getTokenFromHeader(header);
        var username = decodeToken(token).get("sub");
        return ResponseEntity.ok(userProfileService.allowPrivateMessageOnlyToFriends(username, restrict));
    }
}
