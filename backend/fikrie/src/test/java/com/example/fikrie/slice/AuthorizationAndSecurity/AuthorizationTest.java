package com.example.fikrie.slice.AuthorizationAndSecurity;

import com.example.fikrie.Controller.*;
import com.example.fikrie.Config.SecurityConfig;
import com.example.fikrie.Config.JwtFilter;
import com.example.fikrie.Service.InboundService;
import com.example.fikrie.Service.InventoryService;
import com.example.fikrie.Service.OutboundService;
import com.example.fikrie.Service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AdminController.class,
            ManagerController.class,
            UserController.class,
            InventoryController.class,
            InboundController.class,
            OutboundController.class})
@Import({SecurityConfig.class, AuthorizationTest.MethodSecurityConfig.class})
public class AuthorizationTest {

    @EnableMethodSecurity
    public static class MethodSecurityConfig{}

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtFilter jwtFilter;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private InventoryService inventoryService;

    @MockitoBean
    private InboundService inboundService;

    @MockitoBean
    private OutboundService outboundService;

    @BeforeEach
    void setUp() throws Exception {
        // FORCE the mock JwtFilter to act as a pass-through filter so Spring Security runs the FilterChain rules!
        doAnswer(invocation -> {
            HttpServletRequest request = invocation.getArgument(0);
            HttpServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtFilter).doFilter(any(), any(), any());
    }

    /* =========================================================================================
    //  ADMIN authorization
    ========================================================================================= */

    @Test
    @DisplayName("TEST Admin access with Admin authorities")
    @WithMockUser(authorities = {"ADMIN"})
    public void adminShouldSucceed() throws Exception {
        mockMvc.perform(get("/admin/get-user-info/all"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TEST Admin access with Manager authorities")
    @WithMockUser(authorities = {"MANAGER"})
    public void adminShouldBeForbidden() throws Exception {
        mockMvc.perform(get("/admin/get-user-info/all"))
                .andExpect(status().isForbidden());
    }

    /* =========================================================================================
    //  MANAGER authorization
    ========================================================================================= */

    @Test
    @DisplayName("TEST Manager access with Admin authorities")
    @WithMockUser(authorities = {"MANAGER"})
    public void managerShouldSucceed() throws Exception {
        mockMvc.perform(get("/manager/get-user-info/all"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TEST Manager access with User authorities")
    @WithMockUser(authorities = {"USER"})
    public void managerShouldBeForbidden() throws Exception {
        mockMvc.perform(get("/manager/get-user-info/all"))
                .andExpect(status().isForbidden());
    }

    /* =========================================================================================
    //  USER authorization
    ========================================================================================= */

    @Test
    @DisplayName("TEST User access with username John and User authorities")
    @WithMockUser(username = "John", authorities = {"USER"})
    public void sameUsernameShouldSucceed() throws Exception {
        mockMvc.perform(get("/user/get-user-info/John"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TEST User access with username John and Manager authorities")
    @WithMockUser(username = "Jane", authorities = {"MANAGER"})
    public void differentUsernameButHigherAuthoritiesShouldSucceed() throws Exception {
        mockMvc.perform(get("/user/get-user-info/John"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TEST User access with username John and User authorities")
    @WithMockUser(username = "John", authorities = {"USER"})
    public void differentUsernameShouldBeForbidden() throws Exception {
        mockMvc.perform(get("/user/get-user-info/Jane"))
                .andExpect(status().isForbidden());
    }

    /* =========================================================================================
    //  INVENTORY authorization
    ========================================================================================= */

    @Test
    @DisplayName("TEST Inventory with Manager authorities")
    @WithMockUser(username = "John", authorities = {"MANAGER"})
    public void managerShouldSucceedInventory() throws Exception {
        mockMvc.perform(get("/inventory/get-all-inventory"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TEST Inventory with User authorities")
    @WithMockUser(username = "Jane", authorities = {"USER"})
    public void userShouldFailInventory() throws Exception {
        mockMvc.perform(get("/inventory/get-all-inventory"))
                .andExpect(status().isForbidden());
    }

    /* =========================================================================================
    //  INBOUND authorization
    ========================================================================================= */

    @Test
    @DisplayName("TEST Inbound with Manager authorities")
    @WithMockUser(username = "John", authorities = {"MANAGER"})
    public void managerShouldSucceedInbound() throws Exception {
        mockMvc.perform(get("/inbound/get-all"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TEST Inbound with User authorities")
    @WithMockUser(username = "John", authorities = {"USER"})
    public void userShouldSucceedInbound() throws Exception {
        mockMvc.perform(get("/inbound/get-all"))
                .andExpect(status().isOk());
    }

    /* =========================================================================================
    //  OUTBOUND authorization
    ========================================================================================= */

    @Test
    @DisplayName("TEST Outbound with Manager authorities")
    @WithMockUser(username = "John", authorities = {"MANAGER"})
    public void managerShouldSucceedOutbound() throws Exception {
        mockMvc.perform(get("/outbound/get-all"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TEST Outbound with User authorities")
    @WithMockUser(username = "John", authorities = {"USER"})
    public void userShouldSucceedOutbound() throws Exception {
        mockMvc.perform(get("/outbound/get-all"))
                .andExpect(status().isOk());
    }

}
