package org.neoflex.deal.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.neoflex.deal.dto.EmploymentDto;
import org.neoflex.deal.dto.FinishRegistrationRequestDto;
import org.neoflex.deal.mapper.ClientMapper;
import org.neoflex.deal.model.Client;
import org.neoflex.deal.model.enums.EmploymentStatus;
import org.neoflex.deal.model.enums.Gender;
import org.neoflex.deal.model.enums.MaritalStatus;
import org.neoflex.deal.model.enums.Position;
import org.neoflex.deal.model.jsonb.Employment;
import org.neoflex.deal.model.jsonb.Passport;
import org.neoflex.deal.repository.ClientRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Тесты сервиса ClientService")
@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    @InjectMocks
    private ClientService clientService;

    private FinishRegistrationRequestDto finishRequest;
    private UUID clientId;
    private Client client;
    private Client updatedClient;
    private Employment employment;
    private EmploymentDto employmentDto;

    @BeforeEach
    void setUp() {

        employmentDto = EmploymentDto.builder()
                .employmentStatus(EmploymentStatus.EMPLOYED)
                .employerInn("1234567890")
                .salary(new BigDecimal(100_000))
                .position(Position.SPECIALIST)
                .workExperienceTotal(60)
                .workExperienceCurrent(24)
                .build();

        finishRequest = FinishRegistrationRequestDto.builder()
                .gender(Gender.MALE)
                .maritalStatus(MaritalStatus.MARRIED)
                .dependentAmount(2)
                .passportIssueDate(LocalDate.of(2010, 5, 15))
                .passportIssueBranch("123-456")
                .employment(employmentDto)
                .accountNumber("40817810000000000001")
                .build();

        clientId = UUID.randomUUID();

        employment = Employment.builder()
                .status(EmploymentStatus.EMPLOYED)
                .employmentInn("1234567890")
                .salary(new BigDecimal(100_000))
                .position(Position.SPECIALIST)
                .workExperienceTotal(60)
                .workExperienceCurrent(24)
                .build();

        client = Client.builder()
                .clientId(clientId)
                .lastName("Ivanov")
                .firstName("Ivan")
                .middleName("Ivanovich")
                .birthDate(LocalDate.of(1990, 1, 1))
                .email("ivan@example.com")
                .passport(Passport.builder()
                        .series("1234")
                        .number("567890")
                        .issueBranch(null)
                        .issueDate(null)
                        .build())
                .accountNumber("40817810000000000001")
                .build();

        updatedClient = Client.builder()
                .clientId(clientId)
                .lastName("Ivanov")
                .firstName("Ivan")
                .middleName("Ivanovich")
                .birthDate(LocalDate.of(1990, 1, 1))
                .email("ivan@example.com")
                .gender(Gender.MALE)
                .maritalStatus(MaritalStatus.MARRIED)
                .dependentAmount(2)
                .employment(employment)
                .passport(Passport.builder()
                        .series("1234")
                        .number("567890")
                        .issueBranch("123-456")
                        .issueDate(LocalDate.of(2010, 5, 15))
                        .build())
                .accountNumber("40817810000000000001")
                .build();
    }

    @Test
    @DisplayName("Успешное обновление данных клиента")
    void whenCreditCalculatedThenClientPassportIsUpdated() {
        assertNull(client.getPassport().getIssueBranch());
        assertNull(client.getPassport().getIssueDate());
        assertNull(client.getEmployment());

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(clientMapper.updateClient(finishRequest, client)).thenReturn(updatedClient);
        when(clientRepository.save(any())).thenReturn(client);

        clientService.updateClient(clientId, finishRequest);

        assertEquals(finishRequest.getPassportIssueBranch(), updatedClient.getPassport().getIssueBranch());
        assertEquals(finishRequest.getPassportIssueDate(), updatedClient.getPassport().getIssueDate());
        assertEquals(finishRequest.getMaritalStatus(), updatedClient.getMaritalStatus());
        assertEquals(finishRequest.getGender(), updatedClient.getGender());
        assertEquals(employment.getStatus(), employmentDto.getEmploymentStatus());
        assertEquals(employment.getEmploymentInn(), employmentDto.getEmployerInn());
        assertThat(employment)
                .usingRecursiveComparison()
                .ignoringFields("status", "employmentInn")
                .isEqualTo(employmentDto);

        verify(clientRepository).save(client);
        verify(clientMapper).updateClient(finishRequest, client);
    }

    @Test
    @DisplayName("При несуществующем clientId выбрасывается EntityNotFoundException")
    void whenClientNotFoundThenThrowEntityNotFoundException() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> clientService.updateClient(clientId, finishRequest));

        assertTrue(exception.getMessage().contains(clientId.toString()));
        verify(clientRepository).findById(clientId);
    }
}