package com.confido.api.auth.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.confido.api.auth.dtos.UserDTO;
import com.confido.api.auth.models.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

  // DTO -> Entity
  @Mapping(source = "firstName", target = "profile.firstName")
  @Mapping(source = "lastName", target = "profile.lastName")
  @Mapping(source = "phoneNumber", target = "profile.phoneNumber")
  User toEntity(UserDTO dto);

  // Entity -> DTO
  @Mapping(source = "profile.firstName", target = "firstName")
  @Mapping(source = "profile.lastName", target = "lastName")
  @Mapping(source = "profile.phoneNumber", target = "phoneNumber")
  UserDTO toDTO(User user);
}
