package com.masamonoke.simplemessenger.api.service.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.masamonoke.simplemessenger.api.dto.UserDisplay;
import com.masamonoke.simplemessenger.entities.user.User;

import java.util.Set;

public interface UserProfileService {

    User getUserById(Long id, String token) throws JsonProcessingException;

    User updateFirstName(Long id, String firstName, String token) throws JsonProcessingException;

    User updateLastName(Long id, String lastName, String token) throws JsonProcessingException;

    User updatePassword(User user, String token) throws JsonProcessingException;

    User updateEmail(Long id, String email, String token) throws JsonProcessingException;

    void deleteAccount(Long id, String token) throws JsonProcessingException;

    User restoreAccount(User user);

    Set<User> getFriends(String username);

    Set<User> getAnotherUserFriendsList(Long id);

    User hideFriends(String username, boolean hide);

    User allowPrivateMessageOnlyToFriends(String username, boolean restrict);

    UserDisplay addFriend(String username, String friendUsername);
}
