package com.example.fikrie.exception;

import com.example.fikrie.DataTransferObject.RequestRespondUser;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice // Global interceptor for all controller layer failures
public class GlobalExceptionHandler {

    // =========================================================================
    // 🛡️ 1. AUTHENTICATION & SECURITY EXCEPTIONS (SPRING SECURITY FRAMEWORK)
    // =========================================================================

    // Triggers when a user provides an incorrect username or password password matches
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<RequestRespondUser> handleBadCredentials(BadCredentialsException e) {
        RequestRespondUser response = new RequestRespondUser();
        response.setStatusCode(HttpStatus.UNAUTHORIZED.value()); // 401
        response.setError("Unauthorized");
        response.setMessage("Authentication failed: Invalid username or password.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // Triggers when a user is valid but does not possess the correct authority role (e.g., USER accessing /admin/**)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<RequestRespondUser> handleAccessDenied(AccessDeniedException e) {
        RequestRespondUser response = new RequestRespondUser();
        response.setStatusCode(HttpStatus.FORBIDDEN.value()); // 403
        response.setError("Forbidden");
        response.setMessage("Access Denied: You do not have the required permissions to access this endpoint.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // Triggers when trying to access a secure endpoint completely unauthenticated (Missing/Expired Bearer token)
    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<RequestRespondUser> handleInsufficientAuthentication(InsufficientAuthenticationException e) {
        RequestRespondUser response = new RequestRespondUser();
        response.setStatusCode(HttpStatus.FORBIDDEN.value()); // 403
        response.setError("Forbidden");
        response.setMessage("Authentication required: Please provide a valid Bearer token in the Authorization header.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // =========================================================================
    // 🗄️ 2. DATABASE PERSISTENCE & CONSTRAINT EXCEPTIONS (SPRING DATA / JDBC)
    // =========================================================================

    // Triggers when a explicit unique database constraint fails (e.g., inserting a duplicate username or email)
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<RequestRespondUser> handleDuplicateKey(DuplicateKeyException e) {
        RequestRespondUser response = new RequestRespondUser();
        response.setStatusCode(HttpStatus.CONFLICT.value()); // 409
        response.setError("Conflict");
        response.setMessage("Registration failed: The chosen username or email address is already in use.");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // Triggers for general data persistence errors (e.g., null constraints, data truncate, unique index breaches)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<RequestRespondUser> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        RequestRespondUser response = new RequestRespondUser();
        response.setStatusCode(HttpStatus.CONFLICT.value()); // 409
        response.setError("Conflict");
        response.setMessage("Database integrity breach: Data collision or invalid relation reference detected.");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // Triggers when explicitly performing an operation on a user identifier that does not exist
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<RequestRespondUser> handleUsernameNotFound(UsernameNotFoundException e) {
        RequestRespondUser response = new RequestRespondUser();
        response.setStatusCode(HttpStatus.NOT_FOUND.value()); // 404
        response.setError("Not Found");
        response.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // =========================================================================
    // 🌐 3. CLIENT WEB REQUEST MALFORMATIONS (SPRING MVC FRAMEWORK)
    // =========================================================================

    // Triggers when field validation constraints fail (e.g., if you add @Valid/@NotBlank to input params)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RequestRespondUser> handleValidationFailures(MethodArgumentNotValidException e) {
        RequestRespondUser response = new RequestRespondUser();
        response.setStatusCode(HttpStatus.BAD_REQUEST.value()); // 400
        response.setError("Bad Request");

        // Extract the exact field error message defined on the DTO properties
        String extractedMsg = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("Request payload verification failed.");

        response.setMessage(extractedMsg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Triggers when passing an empty body request payload or malformed unreadable JSON syntax format
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RequestRespondUser> handleUnreadableJson(HttpMessageNotReadableException e) {
        RequestRespondUser response = new RequestRespondUser();
        response.setStatusCode(HttpStatus.BAD_REQUEST.value()); // 400
        response.setError("Bad Request");
        response.setMessage("Malformed JSON request payload body. Verification unparseable.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Triggers when an endpoint exists but the client hits it using the wrong HTTP verb method (e.g., GET instead of POST)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<RequestRespondUser> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        RequestRespondUser response = new RequestRespondUser();
        response.setStatusCode(HttpStatus.METHOD_NOT_ALLOWED.value()); // 405
        response.setError("Method Not Allowed");
        response.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    // Triggers when a request attempts to hit a resource route path that does not map to any controller endpoint
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<RequestRespondUser> handleRouteNotFound(NoResourceFoundException e) {
        RequestRespondUser response = new RequestRespondUser();
        response.setStatusCode(HttpStatus.NOT_FOUND.value()); // 404
        response.setError("Not Found");
        response.setMessage("Requested API route endpoint resource could not be found.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // =========================================================================
    // 💥 4. GLOBAL CATCH-ALL CRITICAL ENGINE RECOVERY (GLOBAL BACKUP)
    // =========================================================================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RequestRespondUser> handleGlobalCatchAll(Exception e) {
        RequestRespondUser response = new RequestRespondUser();
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value()); // 500
        response.setError("Internal Server Error");
        response.setMessage("An unexpected system exception occurred: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
