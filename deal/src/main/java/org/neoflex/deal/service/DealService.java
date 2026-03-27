package org.neoflex.deal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neoflex.deal.dto.FinishRegistrationRequestDto;
import org.neoflex.deal.dto.LoanOfferDto;
import org.neoflex.deal.dto.LoanStatementRequestDto;
import org.neoflex.deal.repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DealService {

    private final ClientRepository clientRepository;

    public List<LoanOfferDto> calculationOfPossibleLoanTerms(LoanStatementRequestDto request){

        if (request == null) {
            throw new NullPointerException("Отсутствует тело запроса");
        }

//        Employment employment = Employment.builder()
//                .status()
//                .employmentInn()
//                .salary()
//                .position()
//                .workExperienceTotal()
//                .workExperienceCurrent()
//                .build();
//
//        Client client = Client.builder()
//                .lastName()
//                .firstName()
//                .middleName()
//                .birthDate()
//                .email()
//                .gender()
//                .maritalStatus()
//                .dependentAmount()
//                .passportId()
//                .employmentId()
//                .accountNumber()
//                .build();


        return null;
    }

    public void choosingOneOfTheLoanOffers(LoanOfferDto request){

    }

    public void completionOfRegistrationAndFullCreditCalculation(FinishRegistrationRequestDto request, String statementId){

    }
}
