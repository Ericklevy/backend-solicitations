package com.challenge.backend.interfaces.api;

import com.challenge.backend.application.port.in.PopulateSystemUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PopulateControllerTest {

    @Mock
    private PopulateSystemUseCase populateSystemUseCase;

    @InjectMocks
    private PopulateController controller;

    @Test
    void shouldReturnOkWhenPopulateSucceeds() {
        // Arrange
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);
        when(populateSystemUseCase.execute()).thenReturn(mockResponse);

        // Act
        ResponseEntity<Map<String, Object>> response = controller.populate();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    void shouldReturnInternalServerErrorWhenPopulateFails() {
        // Arrange
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", false);
        when(populateSystemUseCase.execute()).thenReturn(mockResponse);

        // Act
        ResponseEntity<Map<String, Object>> response = controller.populate();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }
}
