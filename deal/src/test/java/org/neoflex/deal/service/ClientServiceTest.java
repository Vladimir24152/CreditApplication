package org.neoflex.deal.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.neoflex.deal.model.Client;
import org.neoflex.deal.model.enums.Gender;
import org.neoflex.deal.model.enums.MaritalStatus;
import org.neoflex.deal.model.jsonb.Passport;
import org.neoflex.deal.repository.ClientRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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

    @InjectMocks
    private ClientService clientService;

    private UUID clientId;
    private Client client;

    @BeforeEach
    void setUp() {

        clientId = UUID.randomUUID();

        client = Client.builder()
                .clientId(clientId)
                .lastName("Ivanov")
                .firstName("Ivan")
                .middleName("Ivanovich")
                .birthDate(LocalDate.of(1990, 1, 1))
                .email("ivan@example.com")
                .gender(Gender.MALE)
                .maritalStatus(MaritalStatus.MARRIED)
                .dependentAmount(2)
                .passport(Passport.builder()
                        .series("1234")
                        .number("567890")
                        .issueBranch(null)
                        .issueDate(null)
                        .build())
                .accountNumber("40817810000000000001")
                .build();
    }

    @Test
    @DisplayName("Успешное обновление данных в паспорте клиента")
    void whenCreditCalculatedThenClientPassportIsUpdated() {
        assertNull(client.getPassport().getIssueBranch());
        assertNull(client.getPassport().getIssueDate());

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(clientRepository.save(any())).thenReturn(client);

        clientService.updateClientPassport(clientId,
                "123-456",
                LocalDate.of(2010, 5, 15));

        verify(clientRepository).save(client);
        assertEquals("123-456", client.getPassport().getIssueBranch());
        assertEquals(LocalDate.of(2010, 5, 15), client.getPassport().getIssueDate());
    }

    @Test
    @DisplayName("При несуществующем clientId выбрасывается EntityNotFoundException")
    void whenClientNotFoundThenThrowEntityNotFoundException() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> clientService.updateClientPassport(clientId,
                        "123-456",
                        LocalDate.of(2010, 5, 15)));

        assertTrue(exception.getMessage().contains(clientId.toString()));
        verify(clientRepository).findById(clientId);
    }
}