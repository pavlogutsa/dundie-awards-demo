package com.ninjaone.dundie_awards.mapper;

import com.ninjaone.dundie_awards.dto.ActivityDto;
import com.ninjaone.dundie_awards.model.Activity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ActivityMapper {

    @Mapping(target="employeeId", source="employee.id")
    ActivityDto toDto(Activity activity);

    List<ActivityDto> toDtoList(List<Activity> activities);
}
