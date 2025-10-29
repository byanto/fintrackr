package com.budiyanto.fintrackr.userservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.budiyanto.fintrackr.userservice.domain.Role;
import com.budiyanto.fintrackr.userservice.domain.User;
import com.budiyanto.fintrackr.userservice.dto.LoginResponse;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "roles", source = "roles")
    LoginResponse toLoginResponse(User user);

    default String roleToString(Role role) {
        return role.getName();
    }
}
