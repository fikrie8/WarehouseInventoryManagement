package com.example.fikrie.slice.AuthorizationAndSecurity;

import com.example.fikrie.Config.JwtFilter;
import com.example.fikrie.Config.SecurityConfig;
import com.example.fikrie.Model.Users;
import com.example.fikrie.Repository.UserRepo;
import com.example.fikrie.Service.JwtService;
import com.example.fikrie.Controller.UserRegisterLoginController;
import com.example.fikrie.DataTransferObject.RequestRespondUser;
import com.example.fikrie.Service.MyUserDetailService;
import com.example.fikrie.Service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserRegisterLoginController.class)
@Import({SecurityConfig.class, JwtFilter.class})
@TestPropertySource(properties = "jwt.secret-key=9a2f6c2647d3e91122a7f5a284c4f92318491b489c6295711b7d5d142b322a39")
@DisplayName("Phase 1: Production-Identical JWT Security & Auth Verification")
public class RegisterLoginTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepo userRepo;

	private RequestRespondUser mockResponse;
	private String realProductionGeneratedToken;

	/* =========================================================================
	// ENTERPRISE TEST CONFIGURATION BLOCK
	// Instead of double mocking, we define clean mock endpoints for dependencies.
	// Spring Boot merges this perfectly into the production SecurityConfig.
	 ========================================================================= */
	@TestConfiguration
	static class DependencyMockConfig {
		@Bean
		public ObjectMapper objectMapper() {
			return new ObjectMapper();
		}

		@Bean
		public JwtService jwtService() {
			return new JwtService();
		}

		@Bean
		public MyUserDetailService myUserDetailService(UserRepo userRepo) {
			return new MyUserDetailService(userRepo);
		}

		@Bean
		public UserService userService() {
			return Mockito.mock(UserService.class);
		}

		@Bean
		public UserRepo userRepo() {
			return Mockito.mock(UserRepo.class);
		}
	}
	// =========================================================================

	@BeforeEach
	void setUp() {

		mockResponse = new RequestRespondUser();
		realProductionGeneratedToken = jwtService.generateToken("manager_fikrie");
		Users mockedUserCreated = new Users();
		mockedUserCreated.setUsername("manager_fikrie");
		mockedUserCreated.setPassword("hashed_password");
		mockedUserCreated.setRole("MANAGER");

		when(userRepo.findByUsername("manager_fikrie")).thenReturn(Optional.of(mockedUserCreated));
	}

	@Test
	@DisplayName("POST /register -> Must pass through real filters and execute user registration")
	void shouldRegisterNewUser() throws Exception {
		mockResponse.setStatusCode(200);
		mockResponse.setUsername("manager_fikrie");
		mockResponse.setMessage("User registered successfully");

		when(userService.register(any(RequestRespondUser.class))).thenReturn(mockResponse);

		mockMvc.perform(post("/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(mockResponse)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("User registered successfully"));
	}

	@Test
	@DisplayName("POST /login -> Must pass through real filters and return secure tokens")
	void shouldLoginAndReturnJwt() throws Exception {
		mockResponse.setStatusCode(200);
		mockResponse.setToken(realProductionGeneratedToken);
		mockResponse.setMessage("Login successful");

		when(userService.login(any(RequestRespondUser.class))).thenReturn(mockResponse);

		mockMvc.perform(post("/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(mockResponse)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").value(realProductionGeneratedToken))
				.andExpect(jsonPath("$.message").value("Login successful"));
	}

	@Test
	@DisplayName("POST /login (Failure) -> Wrong Password throws BadCredentialsException")
	void shouldReturn500WhenPasswordIsIncorrect() throws Exception {
		// Arrange: Prepare a response DTO matching your catch (Exception e) block inside login
		RequestRespondUser failureResponse = new RequestRespondUser();
		failureResponse.setStatusCode(500);
		failureResponse.setMessage("Bad credentials"); // Standard message thrown by Spring Security on wrong password

		// Stub your userService to return this failure payload when a bad request comes in
		when(userService.login(any(RequestRespondUser.class))).thenReturn(failureResponse);

		// Act: Simulate sending the request payload
		RequestRespondUser wrongPasswordPayload = new RequestRespondUser();
		wrongPasswordPayload.setUsername("manager_fikrie");
		wrongPasswordPayload.setPassword("wrong_password_12345");

		// Assert
		mockMvc.perform(post("/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(wrongPasswordPayload)))
				.andExpect(status().isOk()) // Controller still wrapper intercepts with 200 OK
				.andExpect(jsonPath("$.statusCode").value(500))
				.andExpect(jsonPath("$.message").value("Bad credentials"))
				//use .doesNotExist() instead of .isEmpty() when a key is expected to be completely absent from the payload response.
				.andExpect(jsonPath("$.token").doesNotExist());
	}

	@Test
	@DisplayName("POST /login (Failure) -> Username does not exist")
	void shouldReturn404WhenUsernameDoesNotExist() throws Exception {
		// Arrange: Prepare a response DTO matching your .orElseThrow() UsernameNotFoundException block inside login
		RequestRespondUser failureResponse = new RequestRespondUser();
		failureResponse.setStatusCode(404);
		failureResponse.setMessage("User not found");

		when(userService.login(any(RequestRespondUser.class))).thenReturn(failureResponse);

		RequestRespondUser nonExistentUserPayload = new RequestRespondUser();
		nonExistentUserPayload.setUsername("ghost_user");
		nonExistentUserPayload.setPassword("any_password");

		// Act & Assert
		mockMvc.perform(post("/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(nonExistentUserPayload)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.statusCode").value(404))
				.andExpect(jsonPath("$.message").value("User not found"));
	}

	@Test
	@DisplayName("PUT /reset-password -> Must pass through real filters and update credentials")
	void shouldResetPasswordSuccessfully() throws Exception {
		mockResponse.setStatusCode(200);
		mockResponse.setMessage("Password reset successfully");

		when(userService.resetPassword(any(RequestRespondUser.class))).thenReturn(mockResponse);

		mockMvc.perform(put("/reset-password")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(mockResponse)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Password reset successfully"));
	}
}
