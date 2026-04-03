package org.neoflex.deal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.neoflex.deal.dto.CreditDto;
import org.neoflex.deal.model.Credit;
import org.neoflex.deal.model.enums.CreditStatus;

@Mapper(componentModel = "spring")
public interface CreditMapper {

    @Mapping(target = "creditStatus", source = "status")
    Credit toCredit(CreditDto creditDto, CreditStatus status);
}
