package com.example.fikrie.slice.DataModification;

import com.example.fikrie.Config.JwtFilter;
import com.example.fikrie.Config.SecurityConfig;
import com.example.fikrie.Controller.ManagerController; // 1. TARGET THE RIGHT CONTROLLER
import com.example.fikrie.DataTransferObject.RequestRespondUser;
import com.example.fikrie.Model.Users;
import com.example.fikrie.Service.UserService;
import com.example.fikrie.slice.AuthorizationAndSecurity.AuthorizationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ManagerController.class) // Focus target changed here
@Import({SecurityConfig.class, AuthorizationTest.MethodSecurityConfig.class})
public class ManagerDataModificationTest {

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
        doAnswer(invocation -> {
            HttpServletRequest request = invocation.getArgument(0);
            HttpServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("PUT Manager: Should successfully pass update payload to service layer with userId")
    @WithMockUser(username = "John", authorities = {"MANAGER"})
    void updateUserWithUserIdReturnsOk() throws Exception {
        // Arrange
        RequestRespondUser requestPayload = new RequestRespondUser();
        requestPayload.setUsername("John2");
        requestPayload.setEmail("John2@email.com");
        requestPayload.setRole("MANAGER");

        RequestRespondUser respond = new RequestRespondUser();
        respond.setUsername("John2");
        respond.setEmail("John2@email.com");
        respond.setRole("MANAGER");

        // 2. FIXED STUBBING: Use any() to cleanly bind against 'updateUserById'
        when(userService.updateUserById(any(), any())).thenReturn(respond);

        // 3. FIXED ID: Avoid overflow bounds error by using a clean Integer '12345'
        mockMvc.perform(put("/manager/update-user/12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("John2"))
                .andExpect(jsonPath("$.email").value("John2@email.com"))
                .andExpect(jsonPath("$.role").value("MANAGER"));

        // 4. FIXED VERIFICATION: Target the correct operational method endpoint execution trace
        verify(userService, times(1)).updateUserById(eq(12345), any());
    }

    @Test
    @DisplayName("PUT Manager: Should return 404 Not Found when update with non-existence userId")
    @WithMockUser(username = "John", authorities = {"MANAGER"})
    void updateUserWithUserIdReturnsNotFound() throws Exception {
        // Arrange
        RequestRespondUser requestPayload = new RequestRespondUser();
        requestPayload.setUsername("John2");
        requestPayload.setEmail("John2@email.com");
        requestPayload.setRole("MANAGER");

        RequestRespondUser respond = new RequestRespondUser();
        respond.setUsername("John2");
        respond.setEmail("John2@email.com");
        respond.setRole("MANAGER");

        // 2. FIXED STUBBING: Use any() to cleanly bind against 'updateUserById'
        when(userService.updateUserById(eq(999  ), any())).thenThrow(new UsernameNotFoundException("User not found for update : 999"));

        // 3. FIXED ID: Avoid overflow bounds error by using a clean Integer '12345'
        mockMvc.perform(put("/manager/update-user/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestPayload)))
                        .andExpect(status().isNotFound());

        // 4. FIXED VERIFICATION: Target the correct operational method endpoint execution trace
        verify(userService, times(1)).updateUserById(eq(999), any());
    }
}
