package org.neoflex.deal.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neoflex.deal.model.Client;
import org.neoflex.deal.model.jsonb.Passport;
import org.neoflex.deal.repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    public void updateClientPassport(UUID clientId, String issueBranch, LocalDate issueDate) {

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Не найдена заявка с id указанным в заявке: %s", clientId))
                );

        Passport passport = client.getPassport();
        passport.setIssueBranch(issueBranch);
        passport.setIssueDate(issueDate);
        client.setPassport(passport);

        clientRepository.save(client);

        log.info("Обновлен паспорт клиента: id={}, issueBranch={}, issueDate={}",
                client.getClientId(), issueBranch, issueDate);
    }
}
