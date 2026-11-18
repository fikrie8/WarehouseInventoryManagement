package com.example.fikrie.slice.DataModification;

import com.example.fikrie.Config.SecurityConfig;
import com.example.fikrie.Controller.UserController;
import com.example.fikrie.Config.JwtFilter;
import com.example.fikrie.DataTransferObject.RequestRespondUser;
import com.example.fikrie.Model.Users;
import com.example.fikrie.Service.UserService;
import com.example.fikrie.slice.AuthorizationAndSecurity.AuthorizationTest;
import com.fasterxml.jackson.databind.ObjectMapper; // For Object-to-JSON conversion
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, AuthorizationTest.MethodSecurityConfig.class})
public class UserDataModificationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtFilter jwtFilter;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    @BeforeEach
    void setUp() throws Exception {
        // Our reliable restaurant pass-through logic
        doAnswer(invocation -> {
            HttpServletRequest request = invocation.getArgument(0);
            HttpServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("PUT User: Should successfully pass update payload to service layer")
    @WithMockUser(username = "John", authorities = {"USER"})
    void updateUserReturnsOk() throws Exception {
        // 1. Arrange: Build your Request DTO payload
        // Replace 'RequestRespondUser' with your exact DTO class constructor details
        RequestRespondUser requestPayload = new RequestRespondUser();
        requestPayload.setUsername("John2");
        requestPayload.setEmail("John2@email.com");
        requestPayload.setRole("MANAGER");
        // requestPayload.setUser(... set inner fields if necessary)

        // Stub your service to return a mocked updated response if your controller outputs it
        // when(userService.updateUser(any(), any())).thenReturn(new ResponseObject());

        RequestRespondUser respond = new RequestRespondUser();

        Users updatedUserResponse = new Users();
        updatedUserResponse.setUsername("John2");
        updatedUserResponse.setEmail("John2@email.com");
        updatedUserResponse.setRole("MANAGER");
        respond.setUsername(updatedUserResponse.getUsername());
        respond.setEmail(updatedUserResponse.getEmail());
        respond.setRole(updatedUserResponse.getRole());

        when(userService.updateUser(any(), any())).thenReturn(respond);
        // 2. Act & Assert
        mockMvc.perform(put("/user/update-user")
                        .contentType(MediaType.APPLICATION_JSON) // Tells Spring: "Hey, JSON data is coming!"
                        .content(objectMapper.writeValueAsString(requestPayload))) // Converts Java Object -> String JSON
                        .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("John2"))
                .andExpect(jsonPath("$.email").value("John2@email.com"))
                .andExpect(jsonPath("$.role").value("MANAGER"));

        // 3. Verify: Did the controller actually execute the service function?
        verify(userService, times(1)).updateUser(eq("John2"), any());
    }
}
