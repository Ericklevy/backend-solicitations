package com.challenge.backend.infrastructure.external.adapter;

import com.challenge.backend.application.dto.AddressInfo;
import com.challenge.backend.infrastructure.external.client.ViaCepClient;
import com.challenge.backend.infrastructure.external.client.ViaCepResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CepServiceAdapterTest {

    @Mock
    private ViaCepClient viaCepClient;

    @InjectMocks
    private CepServiceAdapter adapter;

    @Test
    void shouldReturnAddressForValidCep() {
        ViaCepResponse response = new ViaCepResponse();
        response.setCep("01001-000");
        response.setLogradouro("Praça da Sé");
        response.setBairro("Sé");
        response.setLocalidade("São Paulo");
        response.setUf("SP");
        response.setErro(false);

        when(viaCepClient.getAddress("01001000")).thenReturn(response);

        AddressInfo address = adapter.getAddressByCep("01001-000");

        assertNotNull(address);
        assertEquals("01001-000", address.getCep());
        assertEquals("Praça da Sé", address.getStreet());
        assertEquals("SP", address.getState());
    }

    @Test
    void shouldReturnNullForInvalidCep() {
        ViaCepResponse response = new ViaCepResponse();
        response.setErro(true);

        when(viaCepClient.getAddress("11111111")).thenReturn(response);

        AddressInfo address = adapter.getAddressByCep("11111111");

        assertNull(address);
    }

    @Test
    void shouldCleanCepInput() {
        ViaCepResponse response = new ViaCepResponse();
        response.setErro(false);

        when(viaCepClient.getAddress("01001000")).thenReturn(response);

        adapter.getAddressByCep("01.001-000");

        verify(viaCepClient).getAddress("01001000");
    }

    @Test
    void shouldReturnTrueForValidCepInIsValidCep() {
        ViaCepResponse response = new ViaCepResponse();
        response.setErro(false);

        when(viaCepClient.getAddress("01001000")).thenReturn(response);

        assertTrue(adapter.isValidCep("01001-000"));
    }

    @Test
    void shouldReturnFalseForInvalidCepInIsValidCep() {
        // invalid size
        assertFalse(adapter.isValidCep("123"));

        // via cep returns error
        ViaCepResponse response = new ViaCepResponse();
        response.setErro(true);
        when(viaCepClient.getAddress("11111111")).thenReturn(response);

        assertFalse(adapter.isValidCep("11111111"));
    }
}
