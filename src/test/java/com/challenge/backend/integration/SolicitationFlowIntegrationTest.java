package com.challenge.backend.integration;

import com.challenge.backend.domain.model.enums.Priority;
import com.challenge.backend.domain.model.enums.ServiceType;
import com.challenge.backend.domain.model.enums.Status;
import com.challenge.backend.domain.repository.SolicitationRepositoryPort;
import com.challenge.backend.interfaces.dto.request.*;
import com.challenge.backend.interfaces.dto.response.AuthResponse;
import com.challenge.backend.interfaces.dto.response.SolicitationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class SolicitationFlowIntegrationTest {

    @SuppressWarnings("resource")
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @SuppressWarnings("resource")
    @Container
    static org.testcontainers.elasticsearch.ElasticsearchContainer elasticsearch = 
            new org.testcontainers.elasticsearch.ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:8.11.1")
            .withEnv("xpack.security.enabled", "false")
            .withEnv("discovery.type", "single-node")
            .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.hbm2ddl.create_namespaces", () -> "true");
        registry.add("spring.elasticsearch.uris", () -> "http://" + elasticsearch.getHttpHostAddress());
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SolicitationRepositoryPort solicitationRepository;

    private String clientToken;

    @BeforeEach
    void setUp() throws Exception {
        // Register client
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("Test Client");
        registerRequest.setEmail("client@test.com");
        registerRequest.setPassword("password123");

        MvcResult registerResponse = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse authResponse = objectMapper.readValue(
                registerResponse.getResponse().getContentAsString(),
                AuthResponse.class
        );
        clientToken = authResponse.getToken();
    }

    @Test
    void shouldCompleteFullFlow() throws Exception {
        // 1. Create solicitation
        MvcResult createResponse = mockMvc.perform(post("/api/solicitations")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andReturn();

        SolicitationResponse created = objectMapper.readValue(
                createResponse.getResponse().getContentAsString(),
                SolicitationResponse.class
        );
        Long solicitationId = created.getId();
        assertEquals(Status.DRAFT, created.getStatus());

        // 2. Save Step 1
        Step1Request step1 = new Step1Request();
        step1.setServiceType(ServiceType.INSTALLATION);
        step1.setTitle("Test Solicitation");
        step1.setDescription("This is a test solicitation with more than 20 characters");

        mockMvc.perform(patch("/api/solicitations/{id}/step1", solicitationId)
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(step1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStep").value(1));

        // 3. Save Step 2
        Step2Request step2 = new Step2Request();
        step2.setCep("01001000");
        step2.setStreet("Praça da Sé");
        step2.setNumber("123");
        step2.setComplement("Apto 45");
        step2.setNeighborhood("Sé");
        step2.setCity("São Paulo");
        step2.setState("SP");

        mockMvc.perform(patch("/api/solicitations/{id}/step2", solicitationId)
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(step2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStep").value(2));

        // 4. Save Step 3
        Step3Request step3 = new Step3Request();
        step3.setPriority(Priority.MEDIUM);
        step3.setPreferredDate(LocalDate.now().plusDays(1));
        step3.setEstimatedValue(BigDecimal.valueOf(500));
        step3.setTermsAccepted(true);

        mockMvc.perform(patch("/api/solicitations/{id}/step3", solicitationId)
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(step3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStep").value(3));

        // 5. Submit solicitation
        mockMvc.perform(post("/api/solicitations/{id}/submit", solicitationId)
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(Status.SUBMITTED.name()));

        // 6. Verify solicitation in database
        var solicitation = solicitationRepository.findById(solicitationId).orElseThrow();
        assertEquals(Status.SUBMITTED, solicitation.getStatus());
        assertNotNull(solicitation.getSubmittedAt());
    }

    @Test
    void shouldFailToSubmitIncompleteSolicitation() throws Exception {
        // 1. Create solicitation
        MvcResult createResponse = mockMvc.perform(post("/api/solicitations")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andReturn();

        SolicitationResponse created = objectMapper.readValue(
                createResponse.getResponse().getContentAsString(),
                SolicitationResponse.class
        );
        Long solicitationId = created.getId();

        // 2. Try to submit without completing steps
        mockMvc.perform(post("/api/solicitations/{id}/submit", solicitationId)
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotAllowClientToSubmitAnotherClientsSolicitation() throws Exception {
        // 1. Register another client and get token
        RegisterRequest anotherClient = new RegisterRequest();
        anotherClient.setName("Another Client");
        anotherClient.setEmail("another@test.com");
        anotherClient.setPassword("password123");

        MvcResult registerResponse = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(anotherClient)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse authResponse = objectMapper.readValue(
                registerResponse.getResponse().getContentAsString(),
                AuthResponse.class
        );
        String anotherClientToken = authResponse.getToken();

        // 2. Create solicitation as first client
        MvcResult createResponse = mockMvc.perform(post("/api/solicitations")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andReturn();

        SolicitationResponse created = objectMapper.readValue(
                createResponse.getResponse().getContentAsString(),
                SolicitationResponse.class
        );
        Long solicitationId = created.getId();

        // 3. Try to submit with the other client's token
        mockMvc.perform(post("/api/solicitations/{id}/submit", solicitationId)
                        .header("Authorization", "Bearer " + anotherClientToken))
                .andExpect(status().isUnauthorized()); // Throws UnauthorizedException ownership check
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() throws Exception {
        // Try to access without token
        mockMvc.perform(get("/api/solicitations"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldValidateStep1Fields() throws Exception {
        // 1. Create solicitation
        MvcResult createResponse = mockMvc.perform(post("/api/solicitations")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andReturn();

        SolicitationResponse created = objectMapper.readValue(
                createResponse.getResponse().getContentAsString(),
                SolicitationResponse.class
        );
        Long solicitationId = created.getId();

        // 2. Try to save Step 1 with invalid data
        Step1Request step1 = new Step1Request();
        step1.setServiceType(null); // Invalid
        step1.setTitle("AB"); // Too short
        step1.setDescription("Short"); // Too short

        mockMvc.perform(patch("/api/solicitations/{id}/step1", solicitationId)
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(step1)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldValidateStep2Fields() throws Exception {
        // 1. Create and complete Step 1
        MvcResult createResponse = mockMvc.perform(post("/api/solicitations")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andReturn();

        SolicitationResponse created = objectMapper.readValue(
                createResponse.getResponse().getContentAsString(),
                SolicitationResponse.class
        );
        Long solicitationId = created.getId();

        Step1Request step1 = new Step1Request();
        step1.setServiceType(ServiceType.INSTALLATION);
        step1.setTitle("Test Solicitation");
        step1.setDescription("This is a test solicitation with more than 20 characters");

        mockMvc.perform(patch("/api/solicitations/{id}/step1", solicitationId)
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(step1)))
                .andExpect(status().isOk());

        // 2. Try to save Step 2 with invalid CEP
        Step2Request step2 = new Step2Request();
        step2.setCep("123"); // Invalid
        step2.setStreet("Praça da Sé");
        step2.setNumber("123");
        step2.setNeighborhood("Sé");
        step2.setCity("São Paulo");
        step2.setState("SP");

        mockMvc.perform(patch("/api/solicitations/{id}/step2", solicitationId)
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(step2)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldValidateStep3Fields() throws Exception {
        // 1. Create and complete Steps 1 & 2
        MvcResult createResponse = mockMvc.perform(post("/api/solicitations")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andReturn();

        SolicitationResponse created = objectMapper.readValue(
                createResponse.getResponse().getContentAsString(),
                SolicitationResponse.class
        );
        Long solicitationId = created.getId();

        Step1Request step1 = new Step1Request();
        step1.setServiceType(ServiceType.INSTALLATION);
        step1.setTitle("Test Solicitation");
        step1.setDescription("This is a test solicitation with more than 20 characters");

        mockMvc.perform(patch("/api/solicitations/{id}/step1", solicitationId)
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(step1)))
                .andExpect(status().isOk());

        Step2Request step2 = new Step2Request();
        step2.setCep("01001000");
        step2.setStreet("Praça da Sé");
        step2.setNumber("123");
        step2.setNeighborhood("Sé");
        step2.setCity("São Paulo");
        step2.setState("SP");

        mockMvc.perform(patch("/api/solicitations/{id}/step2", solicitationId)
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(step2)))
                .andExpect(status().isOk());

        // 2. Try to save Step 3 with past date
        Step3Request step3 = new Step3Request();
        step3.setPriority(Priority.MEDIUM);
        step3.setPreferredDate(LocalDate.now().minusDays(1)); // Past date
        step3.setEstimatedValue(BigDecimal.valueOf(500));
        step3.setTermsAccepted(true);

        mockMvc.perform(patch("/api/solicitations/{id}/step3", solicitationId)
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(step3)))
                .andExpect(status().isBadRequest());
    }
}