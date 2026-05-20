package org.neoflex.deal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.neoflex.deal.dto.EmploymentDto;
import org.neoflex.deal.dto.FinishRegistrationRequestDto;
import org.neoflex.deal.dto.LoanStatementRequestDto;
import org.neoflex.deal.dto.ScoringDataDto;
import org.neoflex.deal.model.Client;
import org.neoflex.deal.model.Statement;
import org.neoflex.deal.model.jsonb.Employment;
import org.neoflex.deal.model.jsonb.Passport;


@Mapper(componentModel = "spring")
public interface ClientMapper {


    @Mapping(target = "clientId", ignore = true)
    @Mapping(target = "gender", ignore = true)
    @Mapping(target = "maritalStatus", ignore = true)
    @Mapping(target = "dependentAmount", ignore = true)
    @Mapping(target = "employment", ignore = true)
    @Mapping(target = "accountNumber", ignore = true)
    @Mapping(target = "passport", expression = "java(toPassport(requestDto))")
    Client toClient(LoanStatementRequestDto requestDto);

    @Mapping(target = "series", source = "passportSeries")
    @Mapping(target = "number", source = "passportNumber")
    @Mapping(target = "issueBranch", ignore = true)
    @Mapping(target = "issueDate", ignore = true)
    Passport toPassport(LoanStatementRequestDto request);

    @Mapping(target = "amount", source = "statement.appliedOffer.requestedAmount")
    @Mapping(target = "term", source = "statement.appliedOffer.term")
    @Mapping(target = "firstName", source = "statement.client.firstName")
    @Mapping(target = "lastName", source = "statement.client.lastName")
    @Mapping(target = "middleName", source = "statement.client.middleName")
    @Mapping(target = "birthDate", source = "statement.client.birthDate")
    @Mapping(target = "gender", source = "request.gender")
    @Mapping(target = "passportSeries", source = "statement.client.passport.series")
    @Mapping(target = "passportNumber", source = "statement.client.passport.number")
    @Mapping(target = "passportIssueDate", source = "request.passportIssueDate")
    @Mapping(target = "passportIssueBranch", source = "request.passportIssueBranch")
    @Mapping(target = "maritalStatus", source = "request.maritalStatus")
    @Mapping(target = "dependentAmount", source = "request.dependentAmount")
    @Mapping(target = "employment", source = "request.employment")
    @Mapping(target = "accountNumber", source = "request.accountNumber")
    @Mapping(target = "isInsuranceEnabled", source = "statement.appliedOffer.isInsuranceEnabled")
    @Mapping(target = "isSalaryClient", source = "statement.appliedOffer.isSalaryClient")
    ScoringDataDto toScoringDataDto(FinishRegistrationRequestDto request, Statement statement);


    @Mapping(target = "clientId", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "middleName", ignore = true)
    @Mapping(target = "birthDate", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "accountNumber", source = "request.accountNumber")
    @Mapping(target = "passport", expression = "java(updatePassport(request, client.getPassport()))")
    @Mapping(target = "employment", expression = "java(toEmployment(request.getEmployment()))")
    @Mapping(target = "gender", source = "request.gender")
    @Mapping(target = "maritalStatus", source = "request.maritalStatus")
    @Mapping(target = "dependentAmount", source = "request.dependentAmount")
    Client updateClient(FinishRegistrationRequestDto request, @MappingTarget Client client);

    @Mapping(target = "series", ignore = true)
    @Mapping(target = "number", ignore = true)
    @Mapping(target = "issueBranch", source = "request.passportIssueBranch")
    @Mapping(target = "issueDate", source = "request.passportIssueDate")
    Passport updatePassport(FinishRegistrationRequestDto request, @MappingTarget Passport passport);

    @Mapping(target = "status", source = "employmentStatus")
    Employment toEmployment(EmploymentDto employmentDto);
}
