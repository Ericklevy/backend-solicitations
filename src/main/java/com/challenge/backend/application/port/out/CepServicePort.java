package com.challenge.backend.application.port.out;

import com.challenge.backend.application.dto.AddressInfo;

public interface CepServicePort {
    AddressInfo getAddressByCep(String cep);
    boolean isValidCep(String cep);
}