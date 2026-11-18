package com.example.fikrie.slice.AuthorizationAndSecurity;

import com.example.fikrie.Config.JwtFilter;
import com.example.fikrie.Service.JwtService;
import com.example.fikrie.Service.MyUserDetailService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/* =========================================================================================
    //  JWT Filter Test
    - This test is written to test the JWT filtering. Unfortunately, while writing this, there is a lot of knowledge on
    Mockito architecture needed to be understood and ask through AI. I was confused why the test keep failing while
    The filter is actually working as supposed to. Finally, I find out that mockito create mock and pass around some
    of the data/object, then process it differently from how our application exactly do. It is time-consuming and I do not
    recommend other developer to proceed this unless for learning purpose.It's better to test the JWT Filter through
    Integration Testing.
   ========================================================================================== */

public class JwtFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private MyUserDetailService myUserDetailsService;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtFilter jwtFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
        jwtFilter = new JwtFilter(jwtService, myUserDetailsService);
    }

    @Test
    @DisplayName("JWT Filter: Should skip processing when Authorization header is missing")
    void filter_MissingHeader_ShouldContinueChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();

        jwtFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("JWT Filter: Should authenticate user when given a valid token string")
    void filter_ValidToken_ShouldAuthenticate() throws Exception {
        String mockToken = "eyJhbGciOiJIUzI1NiJ9.mockTokenString";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + mockToken);

        // CREATE A REAL SPRING USER DETAILS OBJECT INSTEAD OF A MOCK
        UserDetails realUserDetails = User.withUsername("John")
                .password("protected")
                .authorities("USER") // Gives it a proper, operational collection
                .build();

        // Stub out the mock services to interact with this real user model instance
        when(jwtService.extractUsername(anyString())).thenReturn("John");
        when(myUserDetailsService.loadUserByUsername(anyString())).thenReturn(realUserDetails);
        when(jwtService.validateToken(anyString(), any(UserDetails.class))).thenReturn(true);

        // Act
        jwtFilter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("John");
    }
}
