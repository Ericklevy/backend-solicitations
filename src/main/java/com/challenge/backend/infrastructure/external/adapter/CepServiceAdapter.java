package com.challenge.backend.infrastructure.external.adapter;

import com.challenge.backend.application.dto.AddressInfo;
import com.challenge.backend.application.port.out.CepServicePort;
import com.challenge.backend.infrastructure.external.client.ViaCepClient;
import com.challenge.backend.infrastructure.external.client.ViaCepResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CepServiceAdapter implements CepServicePort {

    private final ViaCepClient viaCepClient;

    @Override
    @Cacheable(value = "cepCache", key = "#cep", unless = "#result == null")
    @CircuitBreaker(name = "viaCep", fallbackMethod = "cepFallback")
    @Retry(name = "viaCep")
    public AddressInfo getAddressByCep(String cep) {
        String cleanCep = cep.replaceAll("\\D", "");

        try {
            log.debug("Fetching address for CEP: {}", cleanCep);
            ViaCepResponse response = viaCepClient.getAddress(cleanCep);

            if (response.isError()) {
                log.warn("Invalid CEP: {}", cleanCep);
                return null;
            }

            return AddressInfo.builder()
                    .cep(response.getCep())
                    .street(response.getLogradouro())
                    .neighborhood(response.getBairro())
                    .city(response.getLocalidade())
                    .state(response.getUf())
                    .build();
        } catch (Exception e) {
            log.error("Error fetching CEP: {}", cleanCep, e);
            throw new RuntimeException("Failed to fetch address from CEP service", e);
        }
    }

    // Fallback method - usado pelo CircuitBreaker
    @SuppressWarnings("unused")
    public AddressInfo cepFallback(String cep, Throwable t) {
        log.error("CEP service unavailable for CEP: {}, error: {}", cep, t.getMessage());
        throw new RuntimeException("CEP service temporarily unavailable. Please try again later.");
    }

    @Override
    public boolean isValidCep(String cep) {
        String cleanCep = cep.replaceAll("\\D", "");
        if (cleanCep.length() != 8) {
            return false;
        }
        try {
            // Call the external method (not self-invocation) to respect @Cacheable
            AddressInfo address = getAddressByCep(cep);
            return address != null;
        } catch (Exception e) {
            return false;
        }
    }
}