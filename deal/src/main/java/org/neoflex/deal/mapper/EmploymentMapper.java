package org.neoflex.deal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.neoflex.deal.dto.EmploymentDto;
import org.neoflex.deal.model.jsonb.Employment;

@Mapper(componentModel = "spring")
public interface EmploymentMapper {

    @Mapping(target = "status", source = "employmentStatus")
    Employment toEmployment(EmploymentDto employmentDto);
}
