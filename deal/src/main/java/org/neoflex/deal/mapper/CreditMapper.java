package org.neoflex.deal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.neoflex.deal.dto.CreditDto;
import org.neoflex.deal.model.Credit;

@Mapper(componentModel = "spring")
public interface CreditMapper {

    @Mapping(target = "creditStatus", expression = "java(org.neoflex.deal.model.enums.CreditStatus.CALCULATED)")
    Credit toCreditDto(CreditDto creditDto);
}
