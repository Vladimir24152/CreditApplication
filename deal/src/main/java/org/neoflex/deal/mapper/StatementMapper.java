package org.neoflex.deal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.neoflex.deal.dto.DealDocumentDto;
import org.neoflex.deal.model.Statement;

import java.time.LocalDate;

@Mapper(componentModel = "spring")
public interface StatementMapper {

    @Mapping(target = "signDate", source = "signDate")
    @Mapping(target = "firstName", source = "statement.client.firstName")
    @Mapping(target = "lastName", source = "statement.client.lastName")
    @Mapping(target = "middleName", source = "statement.client.middleName")
    @Mapping(target = "birthDate", source = "statement.client.birthDate")
    @Mapping(target = "email", source = "statement.client.email")
    @Mapping(target = "gender", source = "statement.client.gender")
    @Mapping(target = "maritalStatus", source = "statement.client.maritalStatus")
    @Mapping(target = "dependentAmount", source = "statement.client.dependentAmount")
    @Mapping(target = "accountNumber", source = "statement.client.accountNumber")
    @Mapping(target = "passportSeries", source = "statement.client.passport.series")
    @Mapping(target = "passportNumber", source = "statement.client.passport.number")
    @Mapping(target = "passportIssueDate", source = "statement.client.passport.issueDate")
    @Mapping(target = "passportIssueBranch", source = "statement.client.passport.issueBranch")
    @Mapping(target = "amount", source = "statement.credit.amount")
    @Mapping(target = "term", source = "statement.credit.term")
    @Mapping(target = "monthlyPayment", source = "statement.credit.monthlyPayment")
    @Mapping(target = "rate", source = "statement.credit.rate")
    @Mapping(target = "psk", source = "statement.credit.psk")
    @Mapping(target = "isInsuranceEnabled", source = "statement.appliedOffer.isInsuranceEnabled")
    @Mapping(target = "isSalaryClient", source = "statement.appliedOffer.isSalaryClient")
    @Mapping(target = "paymentSchedule", source = "statement.credit.paymentSchedule")
    DealDocumentDto toDealDocumentDto(Statement statement, LocalDate signDate);
}
