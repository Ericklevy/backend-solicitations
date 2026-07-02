package com.challenge.backend.application.service;

import com.challenge.backend.application.port.in.PopulateSystemUseCase;
import com.challenge.backend.application.port.out.SecurityPort;
import com.challenge.backend.domain.model.Solicitation;
import com.challenge.backend.domain.model.Solicitation.Result;
import com.challenge.backend.domain.model.User;
import com.challenge.backend.domain.model.enums.Priority;
import com.challenge.backend.domain.model.enums.Role;
import com.challenge.backend.domain.model.enums.ServiceType;
import com.challenge.backend.domain.model.enums.Status;
import com.challenge.backend.domain.repository.UserRepositoryPort;
import com.challenge.backend.domain.service.SolicitationDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PopulateSystemService implements PopulateSystemUseCase {

    private final UserRepositoryPort userRepository;
    private final SolicitationDomainService domainService;
    private final SecurityPort securityPort;

    @Override
    @Transactional
    public Map<String, Object> execute() {
        log.info("🚀 Populating system with sample data...");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timestamp", Instant.now());

        try {
            // 1. Criar Analistas
            List<User> analysts = createAnalysts();
            result.put("analysts_created", analysts.size());

            // 2. Criar Clientes
            List<User> clients = createClients();
            result.put("clients_created", clients.size());

            // 3. Criar Solicitações
            List<Solicitation> solicitations = createSolicitations(clients, analysts);
            result.put("solicitations_created", solicitations.size());
            
            result.put("success", true);
            result.put("message", "✅ Sistema populado com sucesso!");
            log.info("✅ Population completed!");
        } catch (Exception e) {
            log.error("❌ Error populating system", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    private List<User> createAnalysts() {
        return List.of(
                createOrGetUser("analista.sp@system.com", "Analista São Paulo", Role.ANALYST, Set.of("SP", "RJ")),
                createOrGetUser("analista.sul@system.com", "Analista Sul", Role.ANALYST, Set.of("PR", "SC", "RS"))
        );
    }

    private List<User> createClients() {
        List<User> clients = new ArrayList<>();
        String[] names = {"João Silva", "Maria Santos", "Pedro Oliveira", "Ana Costa", 
                          "Lucas Ferreira", "Julia Rodrigues", "Marcos Alves", "Carla Gomes"};
        for (int i = 0; i < names.length; i++) {
            clients.add(createOrGetUser("cliente" + (i + 1) + "@test.com", names[i], Role.CLIENT, null));
        }
        return clients;
    }

    private User createOrGetUser(String email, String name, Role role, Set<String> coverageStates) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = User.builder()
                    .name(name)
                    .email(email)
                    .passwordHash(securityPort.encodePassword("password123"))
                    .role(role)
                    .enabled(true)
                    .createdAt(Instant.now())
                    .coverageStates(coverageStates)
                    .build();
            return userRepository.save(user);
        });
    }

    private List<Solicitation> createSolicitations(List<User> clients, List<User> analysts) {
        List<Solicitation> allSolicitations = new ArrayList<>();
        
        for (int i = 0; i < clients.size(); i++) {
            User client = clients.get(i);
            
            // DRAFT
            allSolicitations.add(createDraftSolicitation(client.getId(), i));
            
            // SUBMITTED
            allSolicitations.add(createSubmittedSolicitation(client.getId(), i));
            
            // IN_REVIEW or APPROVED
            if (i % 2 == 0) {
                User analyst = analysts.get(i % analysts.size());
                if (i % 4 == 0) {
                    allSolicitations.add(createApprovedSolicitation(client.getId(), analyst.getId(), i));
                } else {
                    allSolicitations.add(createInReviewSolicitation(client.getId(), analyst.getId(), i));
                }
            }
        }
        return allSolicitations;
    }

    private Solicitation createDraftSolicitation(Long clientId, int index) {
        Solicitation solicitation = buildBaseSolicitation(clientId);
        Solicitation saved = domainService.save(solicitation);
        saved.saveStep1(ServiceType.INSTALLATION, "Instalação Equipamento " + index, "Instalação de equipamento novo.");
        return domainService.save(saved);
    }

    private Solicitation createSubmittedSolicitation(Long clientId, int index) {
        Solicitation saved = createDraftSolicitation(clientId, index);
        saved.saveStep1(ServiceType.MAINTENANCE, "Manutenção " + index, "Manutenção preventiva do ar condicionado.");
        domainService.save(saved);
        
        saved.saveStep2("01001000", "Rua Teste", "123", "Apto " + index, "Centro", "São Paulo", "SP");
        domainService.save(saved);
        
        saved.saveStep3(Priority.MEDIUM, LocalDate.now().plusDays(10), BigDecimal.valueOf(500), true);
        domainService.save(saved);
        
        Result<Void> result = saved.submit();
        if (result.isFailure()) {
            throw new RuntimeException("Failed to submit: " + result.getErrors());
        }
        return domainService.save(saved);
    }
    
    private Solicitation createInReviewSolicitation(Long clientId, Long analystId, int index) {
        Solicitation saved = createSubmittedSolicitation(clientId, index);
        Result<Void> result = saved.startAnalysis(analystId);
        if (result.isFailure()) {
            throw new RuntimeException("Failed to start analysis: " + result.getErrors());
        }
        return domainService.save(saved);
    }

    private Solicitation createApprovedSolicitation(Long clientId, Long analystId, int index) {
        Solicitation saved = createInReviewSolicitation(clientId, analystId, index);
        Result<Void> result = saved.decide(true, analystId, "Aprovado conforme regras e viabilidade técnica da região.");
        if (result.isFailure()) {
            throw new RuntimeException("Failed to approve: " + result.getErrors());
        }
        return domainService.save(saved);
    }

    private Solicitation buildBaseSolicitation(Long clientId) {
        return Solicitation.builder()
                .clientId(clientId)
                .status(Status.DRAFT)
                .currentStep(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
