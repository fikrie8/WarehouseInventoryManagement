package com.example.fikrie.slice.DataModification;

import com.example.fikrie.Config.JwtFilter;
import com.example.fikrie.Config.SecurityConfig;
import com.example.fikrie.Controller.InboundController;
import com.example.fikrie.Controller.OutboundController;
import com.example.fikrie.DataTransferObject.RequestRespondInbound;
import com.example.fikrie.DataTransferObject.RequestRespondOutbound;
import com.example.fikrie.Service.InboundService;
import com.example.fikrie.Service.MyUserDetailService;
import com.example.fikrie.Service.OutboundService;
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

@WebMvcTest(OutboundController.class)
@Import({SecurityConfig.class, AuthorizationTest.MethodSecurityConfig.class})
public class OutboundDataModificationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtFilter jwtFilter;

    @MockitoBean
    private MyUserDetailService myUserDetailService;

    @MockitoBean
    private OutboundService outboundService;

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
    @DisplayName("POST Outbound: Should successfully call register outbound")
    @WithMockUser(username = "John", authorities = {"MANAGER"})
    void addOutboundReturnsOk() throws Exception {
        RequestRespondOutbound requestPayload = new RequestRespondOutbound();
        requestPayload.setProductSku("POF112345AASD");
        requestPayload.setDestination("Puncak Alam");
        requestPayload.setQuantity(100);
        RequestRespondOutbound respond = new RequestRespondOutbound();
        respond.setProductSku("POF112345AASD");
        respond.setDestination("Puncak Alam");
        respond.setQuantity(100);

        when(outboundService.registerOutbound(any())).thenReturn(respond);
        mockMvc.perform(post("/outbound/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productSku").value("POF112345AASD"))
                .andExpect(jsonPath("$.destination").value("Puncak Alam"))
                .andExpect(jsonPath("$.quantity").value(100));
        verify(outboundService, times(1)).registerOutbound(any());
    }

    @Test
    @DisplayName("PUT Outbound: Should successfully pass update payload to service layer")
    @WithMockUser(username = "John", authorities = {"USER"})
    void updateOutboundReturnsOk() throws Exception {
        RequestRespondOutbound requestPayload = new RequestRespondOutbound();
        requestPayload.setProductSku("P0F112345AASD");
        requestPayload.setDestination("Sungai Buloh");
        requestPayload.setQuantity(1000);
        RequestRespondOutbound respond = new RequestRespondOutbound();
        respond.setProductSku("P0F112345AASD");
        respond.setDestination("Sungai Buloh");
        respond.setQuantity(1000);

        when(outboundService.updateOutbound(any(), any())).thenReturn(respond);
        mockMvc.perform(put("/outbound/update/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productSku").value("P0F112345AASD"))
                .andExpect(jsonPath("$.destination").value("Sungai Buloh"))
                .andExpect(jsonPath("$.quantity").value(1000));
        verify(outboundService, times(1)).updateOutbound(any(), any());
    }
}
