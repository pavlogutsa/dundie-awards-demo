package com.ninjaone.dundie_awards.mapper;

import com.ninjaone.dundie_awards.dto.OrganizationDto;
import com.ninjaone.dundie_awards.model.Organization;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrganizationMapper {
    OrganizationDto toDto(Organization organization);
    List<OrganizationDto> toDtoList(List<Organization> organizations);
}
