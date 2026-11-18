package com.example.fikrie.slice.DataModification;

import com.example.fikrie.Config.JwtFilter;
import com.example.fikrie.Config.SecurityConfig;
import com.example.fikrie.Controller.InboundController;
import com.example.fikrie.Controller.InventoryController;
import com.example.fikrie.DataTransferObject.RequestRespondInbound;
import com.example.fikrie.DataTransferObject.RequestRespondInventory;
import com.example.fikrie.Service.InboundService;
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

@WebMvcTest(InboundController.class)
@Import({SecurityConfig.class, AuthorizationTest.MethodSecurityConfig.class})
public class InboundDataModificationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtFilter jwtFilter;

    @MockitoBean
    private MyUserDetailService myUserDetailService;

    @MockitoBean
    private InboundService inboundService;

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
    @DisplayName("POST Inbound: Should successfully call register inbound")
    @WithMockUser(username = "John", authorities = {"MANAGER"})
    void addInboundReturnsOk() throws Exception {
        RequestRespondInbound requestPayload = new RequestRespondInbound();
        requestPayload.setProductSku("POF112345AASD");
        requestPayload.setLocation("Store A");
        requestPayload.setQuantity(100);
        RequestRespondInbound respond = new RequestRespondInbound();
        respond.setProductSku("POF112345AASD");
        respond.setLocation("Store A");
        respond.setQuantity(100);

        when(inboundService.registerInbound(any())).thenReturn(respond);
        mockMvc.perform(post("/inbound/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productSku").value("POF112345AASD"))
                .andExpect(jsonPath("$.location").value("Store A"))
                .andExpect(jsonPath("$.quantity").value(100));
        verify(inboundService, times(1)).registerInbound(any());
    }

    @Test
    @DisplayName("PUT Inbound: Should successfully pass update payload to service layer")
    @WithMockUser(username = "John", authorities = {"USER"})
    void updateInboundReturnsOk() throws Exception {
        RequestRespondInbound requestPayload = new RequestRespondInbound();
        requestPayload.setProductSku("P0F112345AASD");
        requestPayload.setLocation("Store B");
        requestPayload.setQuantity(1000);
        RequestRespondInbound respond = new RequestRespondInbound();
        respond.setProductSku("P0F112345AASD");
        respond.setLocation("Store B");
        respond.setQuantity(1000);

        when(inboundService.updateInbound(any(), any())).thenReturn(respond);
        mockMvc.perform(put("/inbound/update/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productSku").value("P0F112345AASD"))
                .andExpect(jsonPath("$.location").value("Store B"))
                .andExpect(jsonPath("$.quantity").value(1000));
        verify(inboundService, times(1)).updateInbound(any(), any());
    }
}
