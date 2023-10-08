package com.masamonoke.simplemessenger.api.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.masamonoke.simplemessenger.entities.user.Role;
import com.masamonoke.simplemessenger.entities.user.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AuthControllerTest {
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private ObjectMapper objectMapper;
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
}