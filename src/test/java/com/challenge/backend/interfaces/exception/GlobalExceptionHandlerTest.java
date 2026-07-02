package com.challenge.backend.interfaces.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Test
    void shouldHandleBusinessException() {
        BusinessException ex = new BusinessException("Business error", "ERR_CODE");
        when(request.getRequestURI()).thenReturn("/api/test");

        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Business Error", response.getBody().getError());
        assertEquals("Business error", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
        assertFalse(response.getBody().getErrors().isEmpty());
        assertEquals("global", response.getBody().getErrors().get(0).getField());
    }

    @Test
    void shouldHandleNotFoundException() {
        NotFoundException ex = new NotFoundException("Not found");
        when(request.getRequestURI()).thenReturn("/api/test");

        ResponseEntity<ErrorResponse> response = handler.handleNotFoundException(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not Found", response.getBody().getError());
        assertEquals("Not found", response.getBody().getMessage());
    }

    @Test
    void shouldHandleUnauthorizedException() {
        UnauthorizedException ex = new UnauthorizedException("Unauthorized");
        when(request.getRequestURI()).thenReturn("/api/test");

        ResponseEntity<ErrorResponse> response = handler.handleUnauthorizedException(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Unauthorized", response.getBody().getError());
        assertEquals("Unauthorized", response.getBody().getMessage());
    }

    @Test
    void shouldHandleAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");
        when(request.getRequestURI()).thenReturn("/api/test");

        ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access Denied", response.getBody().getError());
        assertEquals("You don't have permission to access this resource", response.getBody().getMessage());
    }

    @Test
    void shouldHandleBadCredentialsException() {
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");
        when(request.getRequestURI()).thenReturn("/api/test");

        ResponseEntity<ErrorResponse> response = handler.handleBadCredentials(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Unauthorized", response.getBody().getError());
        assertEquals("Invalid credentials", response.getBody().getMessage());
    }

    @Test
    void shouldHandleRuntimeException() {
        RuntimeException ex = new RuntimeException("Runtime error");
        when(request.getRequestURI()).thenReturn("/api/test");

        ResponseEntity<ErrorResponse> response = handler.handleRuntimeException(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }

    @Test
    void shouldHandleGenericException() {
        Exception ex = new Exception("Generic error");
        when(request.getRequestURI()).thenReturn("/api/test");

        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }

    @Test
    void shouldHandleMethodArgumentNotValidException() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "default message");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        when(request.getRequestURI()).thenReturn("/api/test");

        ResponseEntity<ErrorResponse> response = handler.handleValidationExceptions(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation Error", response.getBody().getError());
        assertEquals("field", response.getBody().getErrors().get(0).getField());
        assertEquals("default message", response.getBody().getErrors().get(0).getMessage());
    }
}
