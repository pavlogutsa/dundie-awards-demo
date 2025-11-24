package com.ninjaone.dundie_awards.mapper;

import com.ninjaone.dundie_awards.dto.EmployeeDto;
import com.ninjaone.dundie_awards.dto.CreateEmployeeRequest;
import com.ninjaone.dundie_awards.dto.UpdateEmployeeRequest;
import com.ninjaone.dundie_awards.model.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;   

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "organizationName", source = "organization.name")
    EmployeeDto toDto(Employee employee);

    List<EmployeeDto> toDtoList(List<Employee> employees);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "dundieAwards", constant = "0")
    Employee fromCreateRequest(CreateEmployeeRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "dundieAwards", ignore = true)
    void updateEmployeeFromRequest(UpdateEmployeeRequest request,
                                   @MappingTarget Employee employee);
}
