package com.masamonoke.simplemessenger.api.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.masamonoke.simplemessenger.entities.user.Role;
import com.masamonoke.simplemessenger.entities.user.User;
import com.masamonoke.simplemessenger.repo.ConfirmationTokenRepo;
import com.masamonoke.simplemessenger.repo.AuthTokenRepo;
import com.masamonoke.simplemessenger.repo.UserRepo;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
class UserProfileControllerTest {
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private AuthTokenRepo authTokenRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private ConfirmationTokenRepo confirmationTokenRepo;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    private static User user;

    @BeforeAll
    public static void createUser() {
        user = User
                .builder()
                .username("testuser")
                .email("testemail@test.com")
                .password("1234")
                .role(Role.User)
                .build();
    }

    @BeforeEach
    public void setupContext() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @AfterEach
    public void deleteUser() {
        authTokenRepo.deleteAll();
        confirmationTokenRepo.deleteAll();
        userRepo.deleteAll();
    }

    private String registerUser(User user) throws Exception {
        var json = objectMapper.writeValueAsString(user);
        var res = mockMvc.perform(post("/api/v1/auth/register")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
        assertEquals(200, res.getResponse().getStatus());
        var jsonToken = res.getResponse().getContentAsString();
        return (String) objectMapper.readValue(jsonToken, HashMap.class).get("access_token");
    }

    @Test
    public void getUserById() throws Exception {
        var accessToken = registerUser(user);
        var userToGet = userRepo.findAll().get(0);
        var res = mockMvc
                .perform(get("/api/v1/profile/user")
                        .param("id", userToGet.getId().toString()).header("Authorization", "Bearer " + accessToken)).andReturn();
        assertEquals(200, res.getResponse().getStatus());
    }

    @Test
    public void updateFirstName() throws Exception {
        var accessToken = registerUser(user);
        var userToEdit = userRepo.findAll().get(0);
        var firstNameToSave = "Sas";
        var res = mockMvc
                .perform(put("/api/v1/profile/user/first_name")
                        .param("id", userToEdit.getId().toString())
                        .param("first_name", firstNameToSave)
                        .header("Authorization", "Bearer " + accessToken))
                .andReturn();
        assertEquals(200, res.getResponse().getStatus());
        var returnedJson = res.getResponse().getContentAsString();
        var savedFirstName = objectMapper.readValue(returnedJson, Map.class).get("firstName");
        assertEquals(firstNameToSave, savedFirstName);
    }

    @Test
    public void updateLastName() throws Exception {
        var accessToken = registerUser(user);
        var userToEdit = userRepo.findByUsername(user.getUsername()).orElseThrow();
        var lastNameToSave = "Somelastname";
        var res = mockMvc
                .perform(put("/api/v1/profile/user/last_name")
                        .param("id", userToEdit.getId().toString())
                        .param("last_name", lastNameToSave)
                        .header("Authorization", "Bearer " + accessToken))
                .andReturn();
        assertEquals(200, res.getResponse().getStatus());

        var returnedJson = res.getResponse().getContentAsString();
        var savedFirstName = objectMapper.readValue(returnedJson, Map.class).get("lastName");
        assertEquals(lastNameToSave, savedFirstName);
    }

    @Test
    public void updatePassword() throws Exception {
        var accessToken = registerUser(user);
        var userToEdit = userRepo.findAll().get(0);
        var passwordToSave = "12345678";
        var data = String.format("""
                {
                    "id": "%s",
                    "password": "%s"
                }
                """, userToEdit.getId().toString(), passwordToSave);
        var res = mockMvc
                .perform(put("/api/v1/profile/user/password")
                        .contentType(MediaType.APPLICATION_JSON).content(data)
                        .header("Authorization", "Bearer " + accessToken))
                .andReturn();
        assertEquals(200, res.getResponse().getStatus());
        var returnedJson = res.getResponse().getContentAsString();
        var savedPassword = (String) objectMapper.readValue(returnedJson, Map.class).get("password");
        var isMatch = passwordEncoder.matches(passwordToSave, savedPassword);
        assertTrue(isMatch);
    }

    @Test
    public void deleteAccountAndRestore() throws Exception {
        var accessToken = registerUser(user);
        var userToDelete = userRepo.findAll().get(0);
        assertTrue(userToDelete.isEnabled());

        var res = mockMvc
                .perform(delete("/api/v1/profile/user")
                        .param("id", userToDelete.getId().toString())
                        .header("Authorization", "Bearer " + accessToken))
                .andReturn();
        assertEquals(200, res.getResponse().getStatus());
        var deletedUser = userRepo.findById(userToDelete.getId()).orElseThrow();
        assertFalse(deletedUser.isEnabled());

        var data = String.format("""
                {
                    "username": "%s",
                    "email": "%s",
                    "password": "%s"
                }
                """, user.getUsername(), user.getEmail(), user.getPassword()); // using the initial user because after fetching from db passwords is encoded
        res = mockMvc
                .perform(put("/api/v1/profile/user/restore")
                        .contentType(MediaType.APPLICATION_JSON).content(data))
                .andReturn();
        assertEquals(200, res.getResponse().getStatus());
        var returnedJson = res.getResponse().getContentAsString();
        var isRestoredUserEnabled = (boolean) objectMapper.readValue(returnedJson, Map.class).get("enabled");
        assertTrue(isRestoredUserEnabled);
    }

    @Test
    public void addFriend() throws Exception {
        var accessToken = registerUser(user);
        var friendUser = User
                .builder()
                .username("pepuser")
                .email("pep@test.com")
                .password("12345678")
                .role(Role.User)
                .build();
        registerUser(friendUser);
        var res = mockMvc.perform(put("/api/v1/profile/user/add_friend")
                .param("friend", friendUser.getUsername())
                .header("Authorization", "Bearer " + accessToken)
        ).andReturn();
        assertEquals(200, res.getResponse().getStatus());

        var changedUser = userRepo.findByUsername(user.getUsername()).orElseThrow();
        assertTrue(changedUser.getFriends().contains(friendUser));
    }

    @Test
    public void getFriendsList() throws Exception {
        var token = registerUser(user);
        var res = mockMvc.perform(get("/api/v1/profile/user/friends").header("Authorization", "Bearer " + token)).andReturn();
        assertEquals(200, res.getResponse().getStatus());
    }

    @Test
    public void getAnotherFriendsListAndHideItThenGetAgain() throws Exception {
        var tokenUser = registerUser(user);
        var anotherUser = User
                .builder()
                .username("pepuser")
                .email("pep@test.com")
                .password("12345678")
                .role(Role.User)
                .build();
        var anotherUserToken = registerUser(anotherUser);

        var res = mockMvc.perform(put("/api/v1/profile/user/hide_friends")
                .param("hide", "true")
                .header("Authorization", "Bearer " + anotherUserToken)
        ).andReturn();
        assertEquals(200, res.getResponse().getStatus());


        var id = userRepo.findByUsername(anotherUser.getUsername()).orElseThrow().getId();
        assertThrows(ServletException.class, () ->
                mockMvc.perform(get("/api/v1/profile/user/friends/" + id)
                        .header("Authorization", "Bearer " + tokenUser)).andReturn()
        );

        res = mockMvc.perform(put("/api/v1/profile/user/hide_friends")
                .param("hide", "false")
                .header("Authorization", "Bearer " + anotherUserToken)
        ).andReturn();
        assertEquals(200, res.getResponse().getStatus());

        res = mockMvc.perform(get("/api/v1/profile/user/friends/" + id)
                .header("Authorization", "Bearer " + tokenUser)
        ).andReturn();
        assertEquals(200, res.getResponse().getStatus());
    }

    @Test
    public void restrictMessages() throws Exception {
        var accessToken = registerUser(user);
        var userToEdit = userRepo.findByUsername(user.getUsername()).orElseThrow();
        assertFalse(userToEdit.isPrivateMessageFromFriendsOnly());
        var res = mockMvc
                .perform(put("/api/v1/profile/user/restrict_messages")
                        .param("restrict", "true")
                        .header("Authorization", "Bearer " + accessToken))
                .andReturn();
        assertEquals(200, res.getResponse().getStatus());

        var returnedJson = res.getResponse().getContentAsString();
        var isRestricted = (boolean) objectMapper.readValue(returnedJson, Map.class).get("privateMessageFromFriendsOnly");
        assertTrue(isRestricted);
    }

}