package com.masamonoke.simplemessenger.api.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.masamonoke.simplemessenger.entities.user.Role;
import com.masamonoke.simplemessenger.entities.user.User;
import com.masamonoke.simplemessenger.repo.ConfirmationTokenRepo;
import com.masamonoke.simplemessenger.repo.AuthTokenRepo;
import com.masamonoke.simplemessenger.repo.UserRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
class AuthControllerTest {
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private ObjectMapper objectMapper;
    private static User user;
    @Autowired
    private AuthTokenRepo authTokenRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private ConfirmationTokenRepo confirmationTokenRepo;

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

    @AfterEach
    public void deleteUser() {
        authTokenRepo.deleteAll();
        confirmationTokenRepo.deleteAll();
        userRepo.deleteAll();
    }

    @BeforeEach
    public void setupContext() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    @WithMockUser
    public void testRegisterUser() throws Exception {
        var json = objectMapper.writeValueAsString(user);
        var res = mockMvc.perform(post("/api/v1/auth/register")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        assertEquals(200, res.getResponse().getStatus());
    }

    @Test
    public void testSecuredEndpointWithNonAuthorizedUser() throws Exception {
        var res = mockMvc.perform(get("/api/v1/secured_test")).andReturn();
        assertEquals(403, res.getResponse().getStatus());
    }

    @Test
    @WithMockUser
    public void testSecuredEndpointWithAuthorizedUser() throws Exception {
        var res = mockMvc.perform(get("/api/v1/secured_test")).andReturn();
        assertEquals(200, res.getResponse().getStatus());
    }

    @Test
    public void testLoginThenGetSecuredEndpoint() throws Exception {
        var json = objectMapper.writeValueAsString(user);
        var res = mockMvc.perform(post("/api/v1/auth/register")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)).andReturn();
        assertEquals(200, res.getResponse().getStatus());
        // tokens created based on milliseconds and between register and auth not enough time passed and tokens will be equal that is error
        // because of that sleep is needed
        Thread.sleep(1000);

        var credentials = """
                {
                    "username": "testuser",
                    "password": "1234"
                }
                """;
        res = mockMvc
                .perform(post("/api/v1/auth/authenticate").contentType(MediaType.APPLICATION_JSON).content(credentials))
                .andReturn();
        assertEquals(200, res.getResponse().getStatus());

        var mapper = new ObjectMapper();
        var jsonToken = res.getResponse().getContentAsString();
        var tokensMap = mapper.readValue(jsonToken, HashMap.class);
        var accessToken = tokensMap.get("access_token");
        res = mockMvc.perform(get("/api/v1/secured_test").header("Authorization", "Bearer " + accessToken)).andReturn();
        assertEquals(200, res.getResponse().getStatus());
    }

    @Test
    public void testLoginThenLogoutThenTryGetSecuredEndpoint() throws Exception {
        var json = objectMapper.writeValueAsString(user);
        var res = mockMvc.perform(post("/api/v1/auth/register")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
        assertEquals(200, res.getResponse().getStatus());

        var mapper = new ObjectMapper();
        var jsonToken = res.getResponse().getContentAsString();
        var tokensMap = mapper.readValue(jsonToken, HashMap.class);
        var accessToken = tokensMap.get("access_token");

        res = mockMvc.perform(get("/api/v1/secured_test").header("Authorization", "Bearer " + accessToken)).andReturn();
        assertEquals(200, res.getResponse().getStatus());


        res = mockMvc.perform(post("/api/v1/auth/logout")
                .content(json)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
        assertEquals(200, res.getResponse().getStatus());

        res = mockMvc.perform(get("/api/v1/secured_test").header("Authorization", "Bearer " + accessToken)).andReturn();
        assertEquals(403, res.getResponse().getStatus());
    }

    @Test
    public void testRefreshToken() throws Exception {
        var json = objectMapper.writeValueAsString(user);
        var res = mockMvc.perform(post("/api/v1/auth/register")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
        assertEquals(200, res.getResponse().getStatus());
        // tokens created based on milliseconds and between register and auth not enough time passed and tokens will be equal that is error
        // because of that sleep is needed
        Thread.sleep(1000);

        var mapper = new ObjectMapper();
        var jsonToken = res.getResponse().getContentAsString();
        var tokensMap = mapper.readValue(jsonToken, HashMap.class);
        var accessToken = tokensMap.get("access_token");
        var refreshToken = tokensMap.get("refresh_token");

        res = mockMvc.perform(get("/api/v1/secured_test").header("Authorization", "Bearer " + accessToken)).andReturn();
        assertEquals(200, res.getResponse().getStatus());

        res = mockMvc.perform(post("/api/v1/auth/refresh").header("Authorization", "Bearer " + refreshToken)).andReturn();
        assertEquals(200, res.getResponse().getStatus());

        jsonToken = res.getResponse().getContentAsString();
        tokensMap = mapper.readValue(jsonToken, HashMap.class);
        accessToken = tokensMap.get("access_token");
        res = mockMvc.perform(get("/api/v1/secured_test").header("Authorization", "Bearer " + accessToken)).andReturn();
        assertEquals(200, res.getResponse().getStatus());
    }

    @Test
    public void logoutTest() throws Exception {
        var json = objectMapper.writeValueAsString(user);
        var res = mockMvc.perform(post("/api/v1/auth/register")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
        assertEquals(200, res.getResponse().getStatus());
        // tokens created based on milliseconds and between register and auth not enough time passed and tokens will be equal that is error
        // because of that sleep is needed
        Thread.sleep(1000);

        var mapper = new ObjectMapper();
        var jsonToken = res.getResponse().getContentAsString();
        var tokensMap = mapper.readValue(jsonToken, HashMap.class);
        var accessToken = tokensMap.get("access_token");
        res = mockMvc.perform(post("/api/v1/auth/logout").header("Authorization", "Bearer " + accessToken)).andReturn();
        assertEquals(200, res.getResponse().getStatus());

        res = mockMvc.perform(get("/api/v1/secured_test").header("Authorization", "Bearer " + accessToken)).andReturn();
        assertEquals(403, res.getResponse().getStatus());
    }


}