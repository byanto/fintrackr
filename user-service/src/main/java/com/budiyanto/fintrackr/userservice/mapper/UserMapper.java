package com.budiyanto.fintrackr.userservice.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.budiyanto.fintrackr.userservice.dto.UserLoginResponse;
import com.budiyanto.fintrackr.userservice.dto.UserRegistrationRequest;
import com.budiyanto.fintrackr.userservice.dto.UserResponse;
import com.budiyanto.fintrackr.userservice.dto.UserUpdateRequest;
import com.budiyanto.fintrackr.userservice.entity.User;

@Mapper(componentModel = "spring", uses = PasswordEncodingMapper.class)
public interface UserMapper {

    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "password", source = "password", qualifiedBy = EncodedMapping.class)
    User toUser(UserRegistrationRequest registerRequest);

    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", expression = "java(user.getRoles().stream().map(com.budiyanto.fintrackr.userservice.entity.Role::getName).toList())")
    @Mapping(target = "tokenType", constant = "Bearer")
    UserLoginResponse toLoginResponse(User user, String accessToken, String refreshToken);

    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromDto(UserUpdateRequest request, @MappingTarget User user);

}
