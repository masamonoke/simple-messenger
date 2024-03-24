package com.masamonoke.simplemessenger.api.dto;

import com.masamonoke.simplemessenger.entities.user.Role;
import com.masamonoke.simplemessenger.entities.user.User;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

// TODO: probably there is better way to solve display of bidirectional connection of friends field problem
// TODO: there new problem: updating user entity leads to required updating this class
@Getter
@Setter
public class UserDisplay {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private boolean isEmailConfirmed;
    private String password;
    private Role role;
    private boolean isEnabled;
    private Set<String> friendsUsername;

    public UserDisplay(User user) {
        id = user.getId();
        username = user.getUsername();
        firstName = user.getFirstName();
        lastName = user.getLastName();
        email = user.getEmail();
        isEmailConfirmed = user.isEmailConfirmed();
        password = user.getPassword();
        role = user.getRole();
        isEnabled = user.isEnabled();
        friendsUsername = new HashSet<>();
        for (var f : user.getFriends()) {
            friendsUsername.add(f.getUsername());
        }
    }
}
