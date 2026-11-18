package com.example.fikrie.slice.DataModification;

import com.example.fikrie.Config.JwtFilter;
import com.example.fikrie.Config.SecurityConfig;
import com.example.fikrie.Controller.InventoryController;
import com.example.fikrie.DataTransferObject.RequestRespondInventory;
import com.example.fikrie.Service.InventoryService;
import com.example.fikrie.Service.MyUserDetailService;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryController.class)
@Import({SecurityConfig.class, AuthorizationTest.MethodSecurityConfig.class})
public class InventoryDataModificationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtFilter jwtFilter;

    @MockitoBean
    private MyUserDetailService myUserDetailService;

    @MockitoBean
    private InventoryService inventoryService;

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
    @DisplayName("POST Inventory: Should successfully call register inventory")
    @WithMockUser(username = "John", authorities = {"MANAGER"})
    void addInventoryReturnsOk() throws Exception {

        RequestRespondInventory requestPayload = new RequestRespondInventory();
        requestPayload.setName("Camera");
        requestPayload.setCategory("Equipment");
        requestPayload.setSku("POF112345AASD");

        RequestRespondInventory respond = new RequestRespondInventory();
        respond.setName("Camera");
        respond.setCategory("Equipment");
        respond.setSku("POF112345AASD");

        when(inventoryService.singleRegister(any())).thenReturn(respond);

        mockMvc.perform(post("/inventory/register-inventory/single-upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Camera"))
                .andExpect(jsonPath("$.category").value("Equipment"))
                .andExpect(jsonPath("$.sku").value("POF112345AASD"));

        verify(inventoryService, times(1)).singleRegister(any());
    }

    @Test
    @DisplayName("PUT Inventory: Should successfully pass update payload to service layer")
    @WithMockUser(username = "John", authorities = {"MANAGER"})
    void updateInventoryReturnsOk() throws Exception {
        RequestRespondInventory requestPayload = new RequestRespondInventory();
        requestPayload.setName("Digital Camera");
        requestPayload.setCategory("Digital Equipment");
        requestPayload.setSku("P0F112345AASD");

        RequestRespondInventory respond = new RequestRespondInventory();
        respond.setName("Digital Camera");
        respond.setCategory("Digital Equipment");
        respond.setSku("P0F112345AASD");

        when(inventoryService.updateInventory(any(), any())).thenReturn(respond);

        mockMvc.perform(put("/inventory/update-inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Digital Camera"))
                .andExpect(jsonPath("$.category").value("Digital Equipment"))
                .andExpect(jsonPath("$.sku").value("P0F112345AASD"));

        verify(inventoryService, times(1)).updateInventory(any(), any());
    }
}
