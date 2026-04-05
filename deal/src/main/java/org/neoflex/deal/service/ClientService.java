package org.neoflex.deal.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neoflex.deal.dto.FinishRegistrationRequestDto;
import org.neoflex.deal.mapper.EmploymentMapper;
import org.neoflex.deal.model.Client;
import org.neoflex.deal.model.jsonb.Passport;
import org.neoflex.deal.repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    private final EmploymentMapper employmentMapper;

    public void updateClient(UUID clientId, FinishRegistrationRequestDto request) {

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Не найдена заявка с id указанным в заявке: %s", clientId))
                );

        Passport passport = client.getPassport();
        passport.setIssueBranch(request.getPassportIssueBranch());
        passport.setIssueDate(request.getPassportIssueDate());
        client.setPassport(passport);

        client.setEmployment(employmentMapper.toEmployment(request.getEmployment()));
        client.setGender(request.getGender());
        client.setMaritalStatus(request.getMaritalStatus());

        clientRepository.save(client);

        log.info("Обновлен паспорт клиента: id={}, issueBranch={}, issueDate={}",
                client.getClientId(), request.getPassportIssueBranch(), request.getPassportIssueDate());
    }
}
