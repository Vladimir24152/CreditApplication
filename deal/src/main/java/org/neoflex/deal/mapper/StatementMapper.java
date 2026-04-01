package org.neoflex.deal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.neoflex.deal.model.Client;
import org.neoflex.deal.model.Statement;
import org.neoflex.deal.model.enums.ApplicationStatus;
import org.neoflex.deal.model.jsonb.StatusHistory;

@Mapper(componentModel = "spring")
public interface StatementMapper {

    @Mapping(target = "statementId", ignore = true)
    @Mapping(target = "client", source = "client")
    @Mapping(target = "credit", ignore = true)
    @Mapping(target = "status", expression = "java(org.neoflex.deal.model.enums.ApplicationStatus.PREAPPROVAL)")
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "appliedOffer", ignore = true)
    @Mapping(target = "signDate", ignore = true)
    @Mapping(target = "sesCode", ignore = true)
    @Mapping(target = "statusHistory", expression = "java(java.util.List.of(toStatusHistory(org.neoflex.deal.model.enums.ApplicationStatus.PREAPPROVAL)))")
    Statement toStatement(Client client);

    @Mapping(target = "status", source = "status")
    @Mapping(target = "time", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "changeType", expression = "java(org.neoflex.deal.model.enums.ChangeType.AUTOMATIC)")
    StatusHistory toStatusHistory(ApplicationStatus status);
}
